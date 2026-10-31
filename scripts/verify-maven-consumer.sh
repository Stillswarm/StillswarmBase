#!/usr/bin/env bash
# No project dependencies or Maven Local: every foundation dependency comes from the supplied URL.
set -euo pipefail
version=${1:?Pass the published version}
[[ "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || { echo 'Expected x.y.z'; exit 1; }
: "${FOUNDATION_MAVEN_URL:?Set the Maven repository URL}"
repo_root=$(cd "$(dirname "$0")/.." && pwd)
consumer_dir=$(mktemp -d)
trap 'rm -rf "$consumer_dir"' EXIT
mkdir -p "$consumer_dir/gradle" "$consumer_dir/src/main/java/verification"
cp "$repo_root/gradle/libs.versions.toml" "$consumer_dir/gradle/"
if [[ -f "$repo_root/local.properties" ]]; then
  # Copy only the SDK path, never local billing/Firebase configuration.
  sed -n '/^sdk.dir=/p' "$repo_root/local.properties" > "$consumer_dir/local.properties"
fi
cat > "$consumer_dir/settings.gradle.kts" <<'SETTINGS'
pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        exclusiveContent {
            forRepository {
                maven {
                    url = uri(providers.environmentVariable("FOUNDATION_MAVEN_URL").get())
                    if (url.scheme == "https") {
                        credentials {
                            username = providers.environmentVariable("GITHUB_ACTOR").orNull
                            password = providers.environmentVariable("GITHUB_TOKEN").orNull
                        }
                    }
                }
            }
            filter { includeGroup("com.stillswarm.base") }
        }
    }
}
rootProject.name = "foundation-consumer-check"
SETTINGS
cat > "$consumer_dir/build.gradle.kts" <<'BUILD'
plugins { alias(libs.plugins.android.library) }
android {
    namespace = "verification.consumer"
    compileSdk = 37
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
val foundationVersion = providers.gradleProperty("foundationVersion").get()
dependencies {
    listOf("ui", "navigation", "app-shell", "config", "telemetry", "storage", "storage-room",
        "billing", "firebase-config", "firebase-telemetry", "play-billing", "testing").forEach {
        implementation("com.stillswarm.base:foundation-$it:$foundationVersion")
    }
}
BUILD
cat > "$consumer_dir/src/main/AndroidManifest.xml" <<'MANIFEST'
<manifest />
MANIFEST
cat > "$consumer_dir/src/main/java/verification/Smoke.java" <<'JAVA'
package verification;
public final class Smoke {
    public static Class<?>[] types() {
        return new Class<?>[] {
            com.stillswarm.base.ui.StillswarmColors.class,
            com.stillswarm.base.navigation.NavigationCoordinator.class,
            com.stillswarm.base.storage.PreferenceStore.class,
            com.stillswarm.base.config.ConfigSession.class,
            com.stillswarm.base.telemetry.Telemetry.class,
            com.stillswarm.base.billing.BillingSession.class,
            com.stillswarm.base.firebase.config.FirebaseRemoteConfigSource.class,
            com.stillswarm.base.firebase.telemetry.FirebaseTelemetry.class,
            com.stillswarm.base.play.billing.PlayBilling.class,
            com.stillswarm.base.testing.FakeBilling.class
        };
    }
}
JAVA
"$repo_root/gradlew" -p "$consumer_dir" assembleRelease \
  "-PfoundationVersion=$version" --no-configuration-cache --refresh-dependencies --console=plain

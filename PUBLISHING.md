# Publishing and consuming foundation artifacts

Registry: `https://maven.pkg.github.com/Stillswarm/StillswarmBase`
Coordinates: `com.stillswarm.base:foundation-<module>:0.1.3`

## Use from a shell app

GitHub's Maven registry requires authentication even for public packages. Create a
personal access token **classic** with `read:packages` on the GitHub account that
will download dependencies. Keep it in `~/.gradle/gradle.properties`, not a project:

```properties
githubPackagesUsername=YOUR_GITHUB_USERNAME
githubPackagesToken=YOUR_CLASSIC_READ_PACKAGES_TOKEN
```

In the shell app's `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        exclusiveContent {
            forRepository {
                maven {
                    name = "Stillswarm"
                    url = uri("https://maven.pkg.github.com/Stillswarm/StillswarmBase")
                    credentials {
                        username = providers.gradleProperty("githubPackagesUsername")
                            .orElse(providers.environmentVariable("GITHUB_ACTOR")).orNull
                        password = providers.gradleProperty("githubPackagesToken")
                            .orElse(providers.environmentVariable("GITHUB_TOKEN")).orNull
                    }
                }
            }
            filter { includeGroup("com.stillswarm.base") }
        }
    }
}
```

Select only the modules you need in the app's `build.gradle.kts`:

```kotlin
implementation("com.stillswarm.base:foundation-app-shell:0.1.3")
implementation("com.stillswarm.base:foundation-storage:0.1.3")
implementation("com.stillswarm.base:foundation-firebase-config:0.1.3")
implementation("com.stillswarm.base:foundation-firebase-telemetry:0.1.3")
implementation("com.stillswarm.base:foundation-play-billing:0.1.3")
testImplementation("com.stillswarm.base:foundation-testing:0.1.3")
```

Transitive foundation dependencies use the same version. There are 12 artifacts:
ui, navigation, app-shell, config, telemetry, storage, storage-room, billing,
firebase-config, firebase-telemetry, play-billing, testing. The demo, starter, and
build-logic are not published. Consumers still configure their own Android/Compose
plugins, Firebase app configuration, Crashlytics build plugin (if used), signing,
and billing products. Current artifacts use compile SDK 37 and min SDK 24; use a
compatible Android/Kotlin toolchain (the repository catalog is the reference).

For shell-app CI, store read credentials in repository secrets. Its `GITHUB_TOKEN`
does not automatically get access to a different repository's packages; configure
appropriate access or use a classic read token. Never commit a token or paste one
into a chat, build log, URL, or command-line property.

## Cut a release

1. Set `foundationVersion` in `gradle.properties` to a new stable `x.y.z` version.
2. Commit and push the reviewed source. Do not include local credentials or
   `google-services.json`.
3. Push the matching annotated tag, for example `v0.1.3`.
4. The **Publish foundation** workflow validates the version, runs unit
   tests and release lint for all libraries, stages all Maven artifacts, and compiles an
   independent consumer against them. It then publishes with the repository's
   scoped `GITHUB_TOKEN` and repeats the consumer check against GitHub Packages.
5. Check the entire workflow succeeds, including the hosted-consumer step, before
   switching shell apps to that version. Remote publication is not atomic across
   modules: a failed run may leave a partial version; inspect it before retrying.

Keep release versions immutable; do not move a published tag or replace its jars.
Use the workflow's manual dispatch **on an existing matching tag** for a diagnosed
retry. No long-lived publishing token is needed in GitHub Actions. Repository
Actions must be enabled and allowed to write packages.

## Local verification / optional manual publishing

```sh
./gradlew publishAllPublicationsToVerificationRepository
FOUNDATION_MAVEN_URL="file://$PWD/build/maven-repository" \
  bash scripts/verify-maven-consumer.sh 0.1.3
```

The consumer is a temporary, independent Android project with no project
substitutions or Maven Local fallback. It resolves every artifact from the given
registry and compiles representative APIs. It does not install or run an app.

For manual remote publishing, use a classic `write:packages` token via the same
user-level Gradle properties (or GITHUB_ACTOR/GITHUB_TOKEN environment variables):

```sh
./gradlew publishAllPublicationsToGitHubPackagesRepository --no-configuration-cache
```

Avoid the generic `publish` task: it targets all configured publication repositories.
No secrets are stored in this repository. Publishing uses `--no-configuration-cache`
to avoid persisting credential-bearing configuration.

Official registry documentation:
https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry

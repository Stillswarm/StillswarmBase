pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Stillswarm Base"

include(":libraries:ui")
include(":libraries:navigation")
include(":libraries:app-shell")
include(":libraries:config")
include(":libraries:telemetry")
include(":libraries:storage")
include(":libraries:storage-room")
include(":libraries:billing")
include(":integrations:firebase-config")
include(":integrations:firebase-telemetry")
include(":testing")
include(":demo-app")
include(":starter-template")

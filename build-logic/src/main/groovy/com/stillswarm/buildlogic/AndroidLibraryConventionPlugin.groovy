package com.stillswarm.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.JavaVersion
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.artifacts.repositories.PasswordCredentials

/** Shared Android library and Maven publication defaults. Apply after com.android.library. */
class AndroidLibraryConventionPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.pluginManager.apply('maven-publish')
        project.group = project.providers.gradleProperty('foundationGroup').get()
        project.version = project.providers.gradleProperty('foundationVersion').get()

        project.pluginManager.withPlugin('com.android.library') {
            def android = project.extensions.getByName('android')
            android.compileSdk = 37
            android.defaultConfig {
                minSdk = 24
                testInstrumentationRunner = 'androidx.test.runner.AndroidJUnitRunner'
                consumerProguardFiles('consumer-rules.pro')
            }
            android.compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
            android.publishing {
                singleVariant('release') { withSourcesJar() }
            }
            project.afterEvaluate {
                project.extensions.getByName('publishing').publications.create('release', MavenPublication) {
                    from(project.components.getByName('release'))
                    artifactId = 'foundation-' + project.name
                    pom {
                        name = artifactId
                        description = 'Stillswarm reusable Android foundation: ' + project.name
                        url = 'https://github.com/Stillswarm/StillswarmBase'
                        scm {
                            url = 'https://github.com/Stillswarm/StillswarmBase'
                            connection = 'scm:git:https://github.com/Stillswarm/StillswarmBase.git'
                        }
                    }
                }
            }
        }
        project.extensions.getByName('publishing').repositories {
            maven {
                name = 'verification'
                url = project.rootProject.layout.buildDirectory.dir('maven-repository')
            }
            maven {
                name = 'GitHubPackages'
                url = 'https://maven.pkg.github.com/Stillswarm/StillswarmBase'
                credentials(PasswordCredentials) {
                    username = project.providers.gradleProperty('githubPackagesUsername')
                        .orElse(project.providers.environmentVariable('GITHUB_ACTOR')).orNull
                    password = project.providers.gradleProperty('githubPackagesToken')
                        .orElse(project.providers.environmentVariable('GITHUB_TOKEN')).orNull
                }
            }
        }
    }
}

import java.util.Properties

plugins {
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.google.services)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val billingProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
fun billingString(name: String): String {
    val value = billingProperties.getProperty(name, "")
    require(value.all { it.isLetterOrDigit() || it in "_./+=-" }) { "Invalid billing property: $name" }
    return "\"$value\""
}

android {
    namespace = "com.example.stillswarmbase"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.stillswarmbase"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "BILLING_PRODUCT_ID", billingString("billing.productId"))
        buildConfigField("String", "BILLING_PUBLIC_KEY", billingString("billing.publicKey"))

        testInstrumentationRunner = "com.example.stillswarmbase.DemoTestRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(project(":integrations:firebase-telemetry"))
    implementation(project(":testing"))
    implementation(project(":integrations:play-billing"))
    implementation(project(":integrations:firebase-config"))
    implementation(project(":libraries:config"))
    implementation(project(":libraries:storage-room"))
    ksp(libs.androidx.room.compiler)
    implementation(project(":libraries:storage"))
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(project(":libraries:ui"))
    implementation(project(":libraries:app-shell"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
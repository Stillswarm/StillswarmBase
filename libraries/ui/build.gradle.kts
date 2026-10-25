plugins {
    alias(libs.plugins.android.library)
    id("stillswarm.android.library")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.stillswarm.base.ui"
    buildFeatures { compose = true }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

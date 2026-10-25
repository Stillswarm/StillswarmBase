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
    implementation(libs.androidx.compose.material.icons.core)
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.ui)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

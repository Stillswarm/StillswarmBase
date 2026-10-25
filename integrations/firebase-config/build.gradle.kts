plugins {
    alias(libs.plugins.android.library)
    id("stillswarm.android.library")
}

android {
    namespace = "com.stillswarm.base.firebase.config"
}

dependencies {
    api(project(":libraries:config"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

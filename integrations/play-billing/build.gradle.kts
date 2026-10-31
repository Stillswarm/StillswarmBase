plugins {
    alias(libs.plugins.android.library)
    id("stillswarm.android.library")
}
android { namespace = "com.stillswarm.base.play.billing" }
dependencies {
    api(project(":libraries:billing"))
    implementation(libs.play.billing)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

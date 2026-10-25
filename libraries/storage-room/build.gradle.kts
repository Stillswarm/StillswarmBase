plugins {
    alias(libs.plugins.android.library)
    id("stillswarm.android.library")
}

android {
    namespace = "com.stillswarm.base.storage.room"
}

dependencies {
    api(libs.androidx.room.runtime)
}

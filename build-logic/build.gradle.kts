plugins { `groovy-gradle-plugin` }

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "stillswarm.android.library"
            implementationClass = "com.stillswarm.buildlogic.AndroidLibraryConventionPlugin"
        }
    }
}

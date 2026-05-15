plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.android.tools.build:gradle:8.5.2")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
    implementation("org.jetbrains.kotlin.plugin.compose:org.jetbrains.kotlin.plugin.compose.gradle.plugin:2.0.21")
}

gradlePlugin {
    plugins {
        register("sharedConvention") {
            id = "tvshowtracker.shared.kmp"
            implementationClass = "TvShowTrackerSharedConventionPlugin"
        }
        register("androidAppConvention") {
            id = "tvshowtracker.android.application"
            implementationClass = "TvShowTrackerAndroidAppConventionPlugin"
        }
    }
}

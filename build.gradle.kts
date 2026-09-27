plugins {
    id("com.android.application") apply false
    id("com.android.library") apply false
    id("org.jetbrains.kotlin.android") apply false
    alias(libs.plugins.kotlinCompose) apply false
    id("org.jetbrains.kotlin.plugin.parcelize") apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.navigationSafeArgs) apply false
}

subprojects {
    afterEvaluate {
        plugins.withId("com.android.library") {
            extensions.findByType(com.android.build.gradle.LibraryExtension::class.java)?.apply {
                compileSdk = 36
            }
        }
    }
}

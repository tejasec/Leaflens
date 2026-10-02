import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.kotlinCompose)
    id("org.jetbrains.kotlin.plugin.parcelize")
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.navigationSafeArgs)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

android {
    namespace = "com.example.smartagriculture"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.smartagriculture"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Load API key from local.properties
        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { properties.load(it) }
        }
        val geminiApiKey = properties.getProperty("gemini.api.key") ?: ""
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")

        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86_64"))
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
        mlModelBinding = true
    }

    androidResources {
        // Ensure tflite model files are not compressed inside APK
        noCompress += "tflite"
    }
}

dependencies {
    // AndroidX Core & UI Support
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // Jetpack Compose Dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Room Persistence Library
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Firebase Authentication & Services
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)

    // Retrofit & Gson Converter (Cloud Fallback REST Client)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.shimmer)
    implementation(libs.play.services.location)
    implementation(libs.glide)
    implementation(libs.mpandroidchart)
    implementation(libs.workmanager)
    implementation(libs.play.services.maps)

    // Generative AI
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // TensorFlow Lite & Support Libraries for Edge AI
    val tfliteVersion = "2.16.1"
    val tfliteSupportVersion = "0.4.4"
    implementation("org.tensorflow:tensorflow-lite:$tfliteVersion")
    implementation("org.tensorflow:tensorflow-lite-support:$tfliteSupportVersion")
    implementation("org.tensorflow:tensorflow-lite-gpu:$tfliteVersion")
    implementation("org.tensorflow:tensorflow-lite-gpu-api:$tfliteVersion")

    // CameraX Suite for Frame Capture
    val cameraVersion = "1.4.1"
    implementation("androidx.camera:camera-core:$cameraVersion")
    implementation("androidx.camera:camera-camera2:$cameraVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraVersion")
    implementation("androidx.camera:camera-view:$cameraVersion")
    implementation("androidx.camera:camera-video:$cameraVersion")

    // Flutter Module
    implementation(project(":flutter"))
}

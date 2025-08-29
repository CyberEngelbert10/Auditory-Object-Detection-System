plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // The compose plugin is often aliased from org.jetbrains.kotlin.plugin.compose
    // Ensure your libs.versions.toml has the correct mapping.
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.auditoryobjectdetection"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.auditoryobjectdetection"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        // Add this to enable ML model binding
        buildConfig = true
    }
    // This block tells the Android build tools not to compress files
    // with the .tflite extension. This is crucial for TensorFlow Lite models,
    // as they need to be mapped directly into memory.
    androidResources {
        noCompress.add("tflite")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // --- Dependencies for Auditory Object Detection ---

    // TensorFlow Lite for on-device object detection
    implementation(libs.tensorflow.lite.task.vision)
    implementation(libs.tensorflow.lite.support)

    // CameraX for a modern, lifecycle-aware camera API
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)

    // --- New Dependencies for a Complete App ---

    // Jetpack Compose Navigation for handling multiple screens
    implementation(libs.androidx.navigation.compose)

    // ViewModel lifecycle support for Jetpack Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Accompanist Permissions for simplified permission handling in Compose
    implementation(libs.accompanist.permissions)
}
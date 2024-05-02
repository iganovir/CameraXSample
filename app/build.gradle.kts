plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.iganovir.cameraxsample"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.iganovir.cameraxsample"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    //core functionalities of CameraX
    implementation(libs.cameraCore)
    implementation(libs.camera2)

    //Support for integrating CameraX with Android Jetpack's lifecycle-aware architecture
    implementation(libs.cameraLifecycle)

    //Provides a basic user interface (UI) that can be used to display the camera preview
    implementation(libs.cameraView)

    implementation(libs.objectDetectionCommon)
    implementation(libs.objectDetection)
    implementation(libs.faceDetection)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
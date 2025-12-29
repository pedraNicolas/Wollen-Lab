plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.pedra.wollenlab.model"
    compileSdk = AndroidConfig.compileSdk

    defaultConfig {
        minSdk = AndroidConfig.minSdk
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    testImplementation(libs.junit)
}
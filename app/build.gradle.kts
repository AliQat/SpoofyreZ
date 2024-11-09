plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
}

fun getSecret(key: String): String {
    return providers.gradleProperty(key).orNull ?: System.getenv(key) ?: ""
}

android {
    namespace = "com.mobileapp.spoofyrez"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mobileapp.spoofyrez"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "SPOTIFY_CLIENT_ID", "\"${getSecret("spotify.client.id")}\"")
            buildConfigField("String", "SPOTIFY_CLIENT_SECRET", "\"${getSecret("spotify.client.secret")}\"")
        }
        release {
            buildConfigField("String", "SPOTIFY_CLIENT_ID", "\"${getSecret("spotify.client.id")}\"")
            buildConfigField("String", "SPOTIFY_CLIENT_SECRET", "\"${getSecret("spotify.client.secret")}\"")
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx.v1120)
    implementation(libs.androidx.appcompat.v161)
    implementation(libs.material.v1110)
    implementation(libs.androidx.constraintlayout.v214)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    implementation(libs.glide)
    implementation(libs.androidx.monitor)
    implementation(libs.androidx.junit.ktx)
    testImplementation(libs.junit.junit)
    kapt(libs.compiler)
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
}
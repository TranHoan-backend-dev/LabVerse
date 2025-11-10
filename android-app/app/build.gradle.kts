import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("io.freefair.lombok") version "9.0.0"
}

android {
    namespace = "com.se1853_jv.labverse"
    compileSdk = 36

    // Đọc Cloudinary credentials từ local.properties
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { localProperties.load(it) }
    }

    defaultConfig {
        applicationId = "com.se1853_jv.labverse"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Inject Cloudinary credentials vào BuildConfig từ local.properties
        buildConfigField(
            "String",
            "CLOUDINARY_CLOUD_NAME",
            "\"${localProperties.getProperty("cloudinary.cloud.name", "")}\""
        )
        buildConfigField(
            "String",
            "CLOUDINARY_API_KEY",
            "\"${localProperties.getProperty("cloudinary.api.key", "")}\""
        )
        buildConfigField(
            "String",
            "CLOUDINARY_API_SECRET",
            "\"${localProperties.getProperty("cloudinary.api.secret", "")}\""
        )
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.cardview)

    implementation(libs.room.runtime)
    implementation(libs.core.ktx)
    compileOnly(libs.lombok.v11830)
    compileOnly(libs.room.compiler.v250)
    annotationProcessor(libs.lombok.v11830)
    annotationProcessor(libs.room.compiler.v250)

    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    implementation(libs.googleSignIn)

    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    implementation(libs.cloudinary.android)

    implementation(libs.android.pdf.viewer)

    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    annotationProcessor(libs.jackson.annotation)

    implementation(libs.lottie)

    implementation(libs.snake.yaml)

    implementation(libs.work.manager)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
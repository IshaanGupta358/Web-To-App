import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
}

val BASE_WEB_URL= findProperty("BASE_WEB_URL") ?: "Empty Var !"



val includedUrlsRaw = findProperty("INCLUDED_URL")?.toString() ?: ""
val includedUrlsFormatted = includedUrlsRaw
    .split(",")
    .map { it.trim() }
    .joinToString(", ") { "\"$it\"" }

android {
    namespace = "app.example.web_to_app"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.example.web_to_app"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        android.buildFeatures.buildConfig = true

        buildConfigField("String", "BASE_WEB_URL", "\"$BASE_WEB_URL\"")
        buildConfigField("String[]", "INCLUDED_URL", "{ $includedUrlsFormatted }")


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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
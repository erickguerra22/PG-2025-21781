import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val localProps = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) load(localFile.inputStream())
}
val env: String = localProps.getProperty("ENVIRONMENT", "")
val apiURL: String = localProps.getProperty("API_URL", "")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Anotaciones
    id("kotlin-kapt")
    // Hilt
    id("dagger.hilt.android.plugin")
    // SafeArgs para navegacion
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.eguerra.ciudadanodigital"
    compileSdk = 36

    // Habilitar para el uso de binding
    dataBinding {
        enable = true
    }
    viewBinding {
        enable = true
    }

    defaultConfig {
        applicationId = "com.eguerra.ciudadanodigital"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Variables de entorno
        buildConfigField("String", "ENVIRONMENT", "\"$env\"")
        buildConfigField("String", "API_URL", "\"$apiURL\"")
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

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    // Habilitar para HILT
    hilt {
        enableAggregatingTask = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
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

    // HILT
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // AppCompat
    implementation(libs.androidx.appcompat)

    // MaterialDesign
    implementation(libs.material)

    // ConstraintLayout
    implementation(libs.androidx.constraintlayout)

    // NavigationFragment
    implementation(libs.androidx.navigation.fragment)

    // Splash Activity
    implementation(libs.androidx.core.splashscreen)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines.android)

    // Room
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    // Datastore
    implementation(libs.androidx.datastore.preferences)

    // ThreeTen
    implementation(libs.threetenabp)

    // Glide (previsualización de enlaces para documentos)
    implementation(libs.glide)
    ksp(libs.compiler)

    // Swipe Refresh (Recargar)
    implementation(libs.androidx.swiperefreshlayout)
}
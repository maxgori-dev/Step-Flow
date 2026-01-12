plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // ✅ Hilt
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)

    alias(libs.plugins.androidx.baselineprofile)

    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.step_flow"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.step_flow"
        minSdk = 24
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

        create("nonMinifiedRelease") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions { jvmTarget = "11" }

    buildFeatures { compose = true }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Compose BOM из libs.versions.toml
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material.icons.extended)

    debugImplementation(libs.compose.ui.tooling)

    // ✅ Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Baseline Profile
    implementation(libs.androidx.benchmark.macro)
    baselineProfile(project(":baselineprofile"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    // Coil (для загрузки фото карт)
    implementation(libs.coil.compose)

}

// ✅ ВАЖНО: kapt-блок должен быть СНАРУЖИ dependencies
kapt {
    correctErrorTypes = true
}

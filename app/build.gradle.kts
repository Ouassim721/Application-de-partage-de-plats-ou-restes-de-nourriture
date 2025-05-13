plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.foodshareapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.foodshareapp"
        minSdk = 25
        targetSdk = 35
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
        viewBinding = true // utile si tu utilises aussi des layouts XML
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            // Exclure les fichiers en conflit
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/versions/9/OSGI-INF/MANIFEST.MF"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/README.md"
        }
    }
}

dependencies {
    // AndroidX & UI
    implementation(libs.androidx.core.ktx.v1120)
    implementation(libs.androidx.appcompat.v170)
    implementation(libs.material.v190)
    implementation(libs.androidx.constraintlayout.v221)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3.icons.extended)
    implementation(libs.androidx.compose.material3.material3)
    implementation(libs.material.icons.extended)

    // Jetpack Compose (via BOM)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.play.services.cast.framework)
    implementation(libs.identity.jvm)
    implementation(libs.firebase.storage.ktx)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.compiler)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navigation (XML et Compose)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.compose)

    // Firebase via BoM
    implementation(platform(libs.firebase.bom.v3231))
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.google.firebase.auth.ktx)
    implementation(libs.google.firebase.firestore.ktx)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.espresso.core.v351)

    // Coil pour chargement d'images
    implementation(libs.coil.compose)
    //image picker
    implementation(libs.imagepicker)
    //boite dialogue
    implementation(libs.google.material.v190)
    //GLIDE
    implementation(libs.glide) // Version actuelle
    annotationProcessor(libs.compiler) // Pour le traitement des annotations
//ContextCompat
    implementation(libs.core.ktx.v1120)
    implementation(libs.androidx.core)

}

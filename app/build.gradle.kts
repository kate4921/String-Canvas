plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

//    id("com.google.devtools.ksp")
    // Добавьте строку:
//    id("kotlin-kapt")
    id("org.jetbrains.kotlin.kapt")

}

android {
    namespace = "com.example.stringcanvas"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.stringcanvas"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_17 //было 11
        targetCompatibility = JavaVersion.VERSION_17 //было 11
    }
    kotlinOptions {
        jvmTarget = "17" //было 11
    }
    buildFeatures {
        compose = true
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
    implementation(project(":opencv"))
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.lifecycle.service)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

// Splash screen
    implementation (libs.androidx.core.splashscreen)
// Jetpack Compose integration
    implementation(libs.androidx.navigation.compose)
// Testing Navigation
    androidTestImplementation(libs.androidx.navigation.testing)
    // JSON serialization library, works with the Kotlin serialization plugin
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")


//    Room database
    kapt("androidx.room:room-compiler:2.7.0")

//    implementation("androidx.room:room-runtime:2.7.0")
//    ksp("androidx.room:room-compiler:2.7.0")
//    implementation("androidx.room:room-ktx:2.7.0") // Для корутин

    // Дополнительно (по необходимости):
//    testImplementation("androidx.room:room-testing:2.7.0") // Для тестов

    implementation(libs.androidx.room.runtime)

// optional - Kotlin Extensions and Coroutines support for Room
    implementation(libs.androidx.room.ktx)

//// optional - RxJava2 support for Room
//    implementation("androidx.room:room-rxjava2:2.6.1")
//
//// optional - RxJava3 support for Room
//    implementation("androidx.room:room-rxjava3:2.6.1")

//// optional - Guava support for Room, including Optional and ListenableFuture
//    implementation("androidx.room:room-guava:2.6.1")

// optional - Test helpers
    testImplementation(libs.androidx.room.testing)

//// optional - Paging 3 Integration
//    implementation("androidx.room:room-paging:2.6.1")


//    Compose-gallery-picker https://github.com/nabla-run/Compose-gallery-picker
//    implementation(libs.gallery.picker)
//https://github.com/CanHub/Android-Image-Cropper
//    implementation (libs.android.image.cropper)

//    uCrop
//    implementation ("com.github.yalantis:ucrop:2.2.10")

//    Coil
    implementation(libs.coil.compose)
//    implementation("io.coil-kt.coil3:coil-network-okhttp:3.1.0")

    implementation("com.github.CanHub:Android-Image-Cropper:4.0.0")

//  Gson
    implementation (libs.gson)

    //DataStore
//    implementation(libs.androidx.datastore.preferences)
    implementation("androidx.datastore:datastore-preferences:1.1.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.2")
}
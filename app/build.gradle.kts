plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.example.weatherapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.weatherapp"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    /*Nav Controller*/
    val nav_version = "2.7.7"
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
    implementation("com.google.android.material:material:1.12.0")


    /*Retrofit*/
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")

    /*Room*/
    implementation("androidx.room:room-ktx:2.7.0")
    implementation("androidx.room:room-runtime:2.7.0")
    kapt("androidx.room:room-compiler:2.7.0")


    /*Glide*/
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    /*viewmodel & livedata*/
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")

    /* Coroutines */
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")

    /*Location Services*/
    implementation("com.google.android.gms:play-services-location:21.1.0")

    /*Open Street Map*/
    implementation("org.osmdroid:osmdroid-android:6.1.20"){
        exclude(group = "com.j256.ormlite", module = "ormlite-core"
        )
    }
    implementation("org.osmdroid:osmdroid-mapsforge:6.1.16"){
        exclude(group = "com.j256.ormlite", module = "ormlite-core"
        )
    }
    implementation("org.osmdroid:osmdroid-geopackage:6.1.16") {
        exclude(group = "com.j256.ormlite", module = "ormlite-core"
        )
    }
    /*GeoCoding*/
    implementation("com.github.MKergall:osmbonuspack:6.9.0")


    /*Gauge View*/
    implementation("com.github.anastr:speedviewlib:1.6.0")


    configurations.all {
        resolutionStrategy {
            force("androidx.core:core:1.16.0")
        }
    }

}


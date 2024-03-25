plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.hremp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        namespace = "com.example.hremp"

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
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Firebase dependencies
    implementation("com.google.firebase:firebase-auth:22.3.1") // Check for the latest version
    implementation("com.google.firebase:firebase-database:20.3.1") // Check for the latest version

    implementation("com.google.firebase:firebase-firestore:24.10.3") // Use the latest version available



    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    androidTestImplementation("com.android.support.test:runner:1.0.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("com.google.android.gms:play-services-location:21.1.0")

    implementation("com.itextpdf:itext7-core:7.1.16")
    implementation("androidx.core:core:1.12.0")
}

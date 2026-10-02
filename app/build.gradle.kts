plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.abhay.blockblastify"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.abhay.blockblastify"
        minSdk = 24
        targetSdk = 36
        versionCode = 9
        versionName = "3.2.1"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation("androidx.activity:activity:1.9.3")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("androidx.cardview:cardview:1.0.0")

    implementation("androidx.lifecycle:lifecycle-process:2.8.4")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.8.4")
    implementation("com.google.android.gms:play-services-ads:23.6.0")
}

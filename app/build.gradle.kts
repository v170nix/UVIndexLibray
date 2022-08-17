plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    kotlin("plugin.serialization") version "1.6.21"
    `maven-publish`
}

android {

    compileSdk = 32
    buildToolsVersion = "32.0.0"

    defaultConfig {
        minSdk = 23
        targetSdk = 32
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    namespace = "ui.index.lib"

    publishing {
        singleVariant("release") {
//            withSourcesJar()
//            withJavadocJar()
        }
    }

//    buildFeatures {
//        compose = true
//    }
//    composeOptions {
//        kotlinCompilerExtensionVersion = rootProject.extra["compose_version"] as String
////        kotlinCompilerVersion = "1.5.21"
//    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")

    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.appcompat:appcompat:1.5.0")
//    implementation("com.google.android.material:material:1.6.1")

    implementation("com.github.v170nix:arwix-common-library:0.4.0")

    implementation("androidx.room:room-runtime:2.5.0-alpha02")
    kapt("androidx.room:room-compiler:2.5.0-alpha02")
    implementation("androidx.room:room-ktx:2.5.0-alpha02")

//    implementation("androidx.work:work-runtime-ktx:2.7.1")

    implementation("io.ktor:ktor-client-core:2.0.3")
    implementation("io.ktor:ktor-client-okhttp:2.0.3")
    implementation("io.ktor:ktor-client-content-negotiation:2.0.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.0.3")
//    implementation("io.ktor:ktor-client-serialization-jvm:2.0.3")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}

group = "ui.index.lib"
version = "1.0.6"



afterEvaluate {
    publishing {

        publications {
            create<MavenPublication>("ReleasePublication") {
                from(components["release"])
                artifactId = "uv-index-library"
            }
        }

//    repositories {
//        maven {
//            url = uri("file://${System.getenv("HOME")}/.m2/repository")
//        }
//    }
    }
}
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    id("org.jetbrains.kotlin.plugin.compose") version libs.versions.kotlin.get()
    id("org.jetbrains.compose") version "1.6.10"
}

kotlin {

    // ---------- ANDROID ----------
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                }
            }
        }
    }

    // ---------- iOS XCFRAMEWORK ----------
    val xcf = XCFramework()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { target ->
        target.binaries.framework {
            baseName = "ChatSDK"
            isStatic = true
            xcf.add(this)

            // Better Swift interop
            freeCompilerArgs += "-Xobjc-generics"
        }
    }

    // ---------- SOURCE SETS ----------
    sourceSets {

        commonMain.dependencies {
            // Compose Multiplatform (shared UI)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material3)

            // Coroutines (async, Flow)
            implementation(libs.kotlinx.coroutines.core)

            // Serialization (JSON envelopes)
            implementation(libs.kotlinx.serialization.json)

            // Ktor core (shared)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }

        androidMain.dependencies {
            // Android HTTP engine
            implementation(libs.ktor.client.okhttp)
            // Activity + Compose for setContent / ComposeView from library entry point
            implementation(libs.androidx.activity.compose.v1122)
            // ViewTreeLifecycleOwner lives in lifecycle-runtime
            implementation("androidx.lifecycle:lifecycle-runtime-viewtree:2.8.6")
        }

        iosMain.dependencies {
            // iOS HTTP engine
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.example.chat_poc"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.lifecycle:lifecycle-runtime-viewtree:2.8.6")
}


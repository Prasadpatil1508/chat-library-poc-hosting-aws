import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
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
            // Coroutines (async, Flow)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

            // Serialization (JSON envelopes)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

            // Ktor core (shared)
            implementation("io.ktor:ktor-client-core:2.3.12")
            implementation("io.ktor:ktor-client-websockets:2.3.12")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
        }

        androidMain.dependencies {
            // Android HTTP engine
            implementation("io.ktor:ktor-client-okhttp:2.3.12")
        }

        iosMain.dependencies {
            // iOS HTTP engine
            implementation("io.ktor:ktor-client-darwin:2.3.12")
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

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.gradle.api.publish.maven.tasks.PublishToMavenLocal

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    id("org.jetbrains.kotlin.plugin.compose") version libs.versions.kotlin.get()
    id("org.jetbrains.compose") version "1.6.10"
    `maven-publish`
}

group = "com.example.chat_poc"
version = project.findProperty("LIB_VERSION")?.toString() ?: "1.0.0"

kotlin {

    // ---------- ANDROID ----------
    androidTarget {
        // Publish release variant to root publication so Android consumers can use com.example.chat_poc:shared:1.0.0
        publishLibraryVariants("release")
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
            freeCompilerArgs += "-Xobjc-generics"
            xcf.add(this)
        }
    }

    // ---------- SOURCE SETS ----------
    sourceSets {

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material3)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.activity.compose.v1122)
            implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
        }

        iosMain.dependencies {
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

    // 🔴 REQUIRED: publish Android AAR
    publishing {
        singleVariant("release")
    }
}

// ---------- PUBLISHING ----------
// KMP creates root (shared) + target publications automatically.
// publishLibraryVariants("release") adds the Android variant to the root publication.
publishing {

    repositories {
        // Maven Local for local testing
        mavenLocal()
        
        // GitHub Packages for remote publishing
        maven {
            name = "GitHubPackages"
            url = uri(
                project.findProperty("GITHUB_PACKAGES_URL")?.toString()
                    ?: "https://maven.pkg.github.com/PrathameshAdate05/chat-library-poc"
            )
            credentials {
                username = project.findProperty("gpr.user")?.toString()
                    ?: System.getenv("GITHUB_ACTOR") ?: ""
                password = project.findProperty("gpr.token")?.toString()
                    ?: System.getenv("GITHUB_TOKEN") ?: ""
            }
        }
    }
}

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
    id("org.jetbrains.kotlin.plugin.compose") version libs.versions.kotlin.get()
    id("org.jetbrains.compose") version "1.10.0"
    `maven-publish`
}

group = "com.example.chat_poc"
version = project.findProperty("LIB_VERSION")?.toString() ?: "1.0.0"

//
// ---------- GENERATE CONNECT CONFIG ----------
//

val generateConnectConfig = tasks.register("generateConnectConfig") {
    val outputFile =
        file("src/commonMain/kotlin/com/example/chat_poc/config/LibraryConnectConfig.kt")
    val localPropsFile = rootProject.file("local.properties")

    outputs.file(outputFile)
    if (localPropsFile.exists()) inputs.file(localPropsFile)

    doLast {
        val props = Properties()
        if (localPropsFile.exists()) {
            localPropsFile.reader().use { props.load(it) }
        }

        val apiGateway = props.getProperty("API_GATEWAY") ?: ""
        val contactFlowId = props.getProperty("CONTACT_FLOW_ID") ?: ""
        val instanceId = props.getProperty("INSTANCE_ID") ?: ""
        val region = props.getProperty("REGION") ?: ""

        val hasAll =
            apiGateway.isNotBlank() &&
            contactFlowId.isNotBlank() &&
            instanceId.isNotBlank() &&
            region.isNotBlank()

        outputFile.parentFile.mkdirs()
        outputFile.writeText(
            """
            package com.example.chat_poc.config

            object LibraryConnectConfig {
                fun get(): ConnectConfig? =
                    if (hasConfig) ConnectConfig(
                        apiGatewayUrl = "$apiGateway",
                        contactFlowId = "$contactFlowId",
                        instanceId = "$instanceId",
                        region = "$region"
                    ) else null

                private const val hasConfig = $hasAll
            }
            """.trimIndent()
        )
    }
}

tasks.matching {
    it.name.contains("compile") && it.name.contains("Kotlin")
}.configureEach {
    dependsOn(generateConnectConfig)
}

//
// ---------- KOTLIN MULTIPLATFORM ----------
//

kotlin {

    // ---------- ANDROID ----------
    androidLibrary {
        namespace = "com.example.chat_poc"
        compileSdk = 35
        minSdk = 24

        // 🔑 REQUIRED: create Android AAR
        publishLibraryVariants("release")

        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    (this as KotlinJvmCompilerOptions).jvmTarget.set(JvmTarget.JVM_1_8)
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }
            }
        }
    }

    // ---------- iOS ----------
    val xcf = XCFramework()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { target ->
        target.compilations.all {
            compileTaskProvider.configure {
                compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
        target.binaries.framework {
            baseName = "ChatSDK"
            isStatic = true
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
            implementation("io.noties.markwon:core:4.6.2")
            implementation("io.noties.markwon:ext-tables:4.6.2")
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

//
// ---------- ANDROID MAVEN PUBLICATION ----------
//

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("sharedAndroid") {
                groupId = "com.example.chat_poc"
                artifactId = "shared-android"
                version = project.version.toString()

                // ✅ CORRECT for Android KMP
                from(components["release"])
            }
        }
    }
}

//
// ---------- REPOSITORIES ----------
//

publishing {
    repositories {

        mavenLocal()

        maven {
            name = "AWSCodeArtifact"
            val domain = System.getenv("AWS_DOMAIN") ?: ""
            val accountId = System.getenv("AWS_ACCOUNT_ID") ?: ""
            val region = System.getenv("AWS_REGION") ?: ""
            val repo = System.getenv("AWS_REPO") ?: ""

            url = uri(
                "https://${domain}-${accountId}.d.codeartifact.${region}.amazonaws.com/maven/${repo}/"
            )

            credentials {
                username = "aws"
                password = System.getenv("CODEARTIFACT_AUTH_TOKEN") ?: ""
            }
        }
    }
}

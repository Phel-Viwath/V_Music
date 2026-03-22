import org.jetbrains.kotlin.gradle.dsl.JvmTarget

private val isMacOs = System.getProperty("os.name").contains("Mac", ignoreCase = true)

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinMultiplatformAndroid)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)

    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.androidx.room3)
}

kotlin {
    androidLibrary {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }

        namespace = "com.v.music.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        androidResources.enable = true
    }

    if (isMacOs) {
        listOf(
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = "Shared"
                isStatic = true
            }
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.compose.material3)
            implementation(libs.compose.material3.adaptive)
            implementation(libs.compose.material3.adaptive.layout)
            implementation(libs.compose.material3.adaptive.navigation)
            implementation(libs.compose.navigation3.ui)
            implementation(libs.compose.material3.adaptive.nav3)


            implementation(libs.compose.material3.icon.extend)

            implementation(libs.compose.navigationevent)
            implementation(libs.androidx.savedstate)
            implementation(libs.androidx.window.core)

            implementation(libs.coil)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            implementation(libs.androidx.room3.runtime)
            implementation(libs.androidx.sqlite.bundled)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

room3{
    schemaDirectory("$projectDir/schemas")
}

configurations.configureEach {
    resolutionStrategy {
        force("org.jetbrains:annotations:23.0.0")
    }
    exclude(group = "com.intellij", module = "annotations")
}

dependencies {
    add("kspAndroid", libs.androidx.room3.compiler)
    if (isMacOs) {
        add("kspIosSimulatorArm64", libs.androidx.room3.compiler)
        add("kspIosX64", libs.androidx.room3.compiler)
        add("kspIosArm64", libs.androidx.room3.compiler)
    }
}


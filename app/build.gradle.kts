// Copyright (C) 2025 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.plugin.compose)
  alias(libs.plugins.kotlin.plugin.serialization)
  alias(libs.plugins.compose)
  alias(libs.plugins.metro)
}

kotlin {
  androidTarget()
  jvm {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    mainRun { mainClass = "foundation.software.kmp.app.MainKt" }
  }

  sourceSets {
    commonMain {
      dependencies {
        implementation(project(":screen-home"))
        implementation(project(":screen-details"))
        implementation(project(":screen-settings"))
        implementation(libs.metrox.viewmodel.compose)

        implementation(libs.jetbrains.navigation3.ui)
        implementation(libs.jetbrains.lifecycle.viewmodel.navigation3)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.compose.material3)
        implementation(libs.compose.runtime)
      }
    }
    commonTest { dependencies { implementation(libs.kotlin.test) } }
    androidMain {
      dependencies {
        implementation(libs.metrox.android)
        implementation(libs.androidx.activity)
        implementation(libs.androidx.appcompat)
        implementation(libs.androidx.core)
        implementation(libs.androidx.lifecycle.runtime.compose)
      }
    }
    jvmMain {
      dependencies {
        implementation(compose.desktop.currentOs)
        // To set main dispatcher on desktop app
        implementation(libs.coroutines.swing)
      }
    }
  }
}

dependencies {
  debugImplementation(libs.leakcanary.android)
  debugImplementation(libs.anrwatchdog)
}

android {
  namespace = "foundation.software.kmp"

  compileSdk = 36

  defaultConfig {
    applicationId = "foundation.software.kmp"
    minSdk = 28
    targetSdk = 35
    versionCode = 1
    versionName = "1.0"
  }

  signingConfigs {
    create("release") {
      storeFile = file("debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      signingConfig = signingConfigs.getByName("release")
    }
  }
}

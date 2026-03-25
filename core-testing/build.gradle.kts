// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.metro)
}

kotlin {
}

android {
  namespace = "foundation.software.kmp.core.testing"

  compileSdk = 36

  defaultConfig {
    minSdk = 28
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }

  buildTypes { release { isMinifyEnabled = false } }
}

dependencies {
  api(project(":core"))
  implementation(libs.metrox.android)
  implementation(libs.coroutines.test)
}

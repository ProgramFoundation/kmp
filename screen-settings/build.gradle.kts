// Copyright (C) 2025 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.plugin.compose)
  alias(libs.plugins.compose)
  alias(libs.plugins.metro)
}

kotlin {
  jvm()

  sourceSets {
    commonMain {
      dependencies {
        api(libs.metrox.viewmodel.compose)
        implementation(libs.compose.material3)
        implementation(libs.compose.runtime)
      }
    }
  }
}

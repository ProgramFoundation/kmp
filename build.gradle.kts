// Copyright (C) 2024
// SPDX-License-Identifier: Apache-2.0

plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.android.lint) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.spotless)
  alias(libs.plugins.compose) apply false
  alias(libs.plugins.kotlin.plugin.compose) apply false
  id("com.android.application") version libs.versions.agp.get() apply false
}

// Autoconfigure git to use project-specific config (hooks)
if (file(".git").exists()) {
  val expectedIncludePath = "../config/git/.gitconfig"
  val includePath =
    providers
      .exec { commandLine("git", "config", "--local", "--default", "", "--get", "include.path") }
      .standardOutput
      .asText
      .map { it.trim() }
      .getOrElse("")
  if (includePath != expectedIncludePath) {
    providers
      .exec { commandLine("git", "config", "--local", "include.path", expectedIncludePath) }
      .result
      .get()
  }
}

subprojects {
  group = project.property("GROUP") as String
  version = project.property("VERSION_NAME") as String
}

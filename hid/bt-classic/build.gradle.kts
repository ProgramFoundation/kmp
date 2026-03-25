plugins {
  id("com.android.library")
  kotlin("android")
}

android {
  namespace = "foundation.software.kmp.hid.btclassic"
  compileSdk = 35

  defaultConfig {
    minSdk = 24
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}

kotlin {
  compilerOptions {
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
  }
}

dependencies {
  implementation(project(":hid:domain"))
  implementation(libs.coroutines)
}

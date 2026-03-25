plugins {
  id("com.android.application")
  kotlin("android")
}

android {
  namespace = "foundation.software.kmp.hid.remote"
  compileSdk = 36

  defaultConfig {
    applicationId = "foundation.software.kmp.hid.remote"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"
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
  implementation(project(":hid:bt-classic"))
  implementation(project(":hid:gatt"))
  implementation(project(":hid:usb"))
  implementation(libs.coroutines)
  implementation(libs.androidx.core)
  implementation(libs.androidx.appcompat)
}

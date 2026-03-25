plugins {
  kotlin("multiplatform")
  id("com.android.library")
}

kotlin {
  jvm()
  androidTarget {
    publishLibraryVariants("release")
    compilerOptions {
      jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(libs.coroutines)
    }
  }
}

android {
  namespace = "foundation.software.kmp.hid.domain"
  compileSdk = 35

  defaultConfig {
    minSdk = 24
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}

// Copyright (C) 2025 Zac Sweers
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.app

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.StrictMode
import dev.zacsweers.metro.createGraph
import dev.zacsweers.metrox.android.MetroAppComponentProviders
import dev.zacsweers.metrox.android.MetroApplication

class MetroApp : Application(), MetroApplication {
  override val appComponentProviders: MetroAppComponentProviders by lazy { createGraph<AppGraph>() }

  override fun onCreate() {
    super.onCreate()

    val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    if (isDebuggable) {
      StrictMode.setThreadPolicy(
        StrictMode.ThreadPolicy.Builder()
          .detectAll()
          .penaltyLog()
          .penaltyDeath()
          .build()
      )

      StrictMode.setVmPolicy(
        StrictMode.VmPolicy.Builder()
          .detectAll()
          .penaltyLog()
          .penaltyDeath()
          .build()
      )

      try {
        val watchDogClass = Class.forName("com.github.anrwatchdog.ANRWatchDog")
        val watchDog = watchDogClass.getDeclaredConstructor().newInstance()
        watchDogClass.getMethod("start").invoke(watchDog)
      } catch (e: Exception) {
        // Ignored
      }
    }
  }
}

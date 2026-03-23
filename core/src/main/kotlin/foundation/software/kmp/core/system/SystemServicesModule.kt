// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.system

import android.hardware.SensorManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import foundation.software.kmp.core.context.ApplicationContext

@ContributesTo(AppScope::class)
public interface SystemServicesModule {
  @Provides
  @SingleIn(AppScope::class)
  public fun provideConnectivityManager(applicationContext: ApplicationContext): ConnectivityManager =
    applicationContext.context.getSystemService(ConnectivityManager::class.java)

  @Provides
  @SingleIn(AppScope::class)
  public fun provideSensorManager(applicationContext: ApplicationContext): SensorManager =
    applicationContext.context.getSystemService(SensorManager::class.java)

  @Provides
  @SingleIn(AppScope::class)
  public fun provideLocationManager(applicationContext: ApplicationContext): LocationManager =
    applicationContext.context.getSystemService(LocationManager::class.java)

  @Provides
  @SingleIn(AppScope::class)
  public fun provideTelephonyManager(applicationContext: ApplicationContext): TelephonyManager =
    applicationContext.context.getSystemService(TelephonyManager::class.java)
}

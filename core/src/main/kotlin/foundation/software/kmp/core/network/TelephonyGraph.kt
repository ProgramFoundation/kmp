// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.network

import android.net.NetworkCapabilities
import android.net.TelephonyNetworkSpecifier
import android.os.Build
import android.telephony.TelephonyManager
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import foundation.software.kmp.core.context.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow

public annotation class TelephonyScope

@GraphExtension(NetworkScope::class)
public interface TelephonyGraph {
  public val telephonyManager: TelephonyManager

  @Provides
  public fun provideTelephonyManager(
    applicationContext: ApplicationContext,
    networkCapabilities: MutableStateFlow<NetworkCapabilities?>,
  ): TelephonyManager {
    var tm = applicationContext.context.getSystemService(TelephonyManager::class.java)!!
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      val specifier = networkCapabilities.value?.networkSpecifier
      if (specifier is TelephonyNetworkSpecifier) {
        tm = tm.createForSubscriptionId(specifier.subscriptionId)
      }
    }
    return tm
  }

  @GraphExtension.Factory
  public interface Factory {
    public fun create(): TelephonyGraph
  }
}

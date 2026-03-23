// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.network

import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.os.Build
import android.telephony.TelephonyManager
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import foundation.software.kmp.core.coroutines.IoDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow

@JvmInline
public value class NetworkCoroutineScope(public val coroutineScope: CoroutineScope)

public annotation class NetworkScope

@GraphExtension(NetworkScope::class)
public interface NetworkGraph {
  public val network: Network
  public val coroutineScope: NetworkCoroutineScope
  public val networkCapabilities: StateFlow<NetworkCapabilities>
  public val linkProperties: StateFlow<LinkProperties?>
  public val telephonyGraphFactory: TelephonyGraph.Factory

  @Provides
  public fun provideNetworkCoroutineScope(
    network: Network,
    networkCapabilities: StateFlow<NetworkCapabilities>,
    telephonyManager: TelephonyManager,
    ioDispatcher: IoDispatcher,
  ): NetworkCoroutineScope {
    val caps = networkCapabilities.value
    val name = buildString {
      when {
        caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
          append("wifi")
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            (caps.transportInfo as? WifiInfo)?.ssid
              ?.removeSurrounding("\"")
              ?.takeIf { it.isNotEmpty() && it != "<unknown ssid>" }
              ?.let { append("-$it") }
          }
        }
        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
          append("cellular")
          telephonyManager.networkOperatorName
            .takeIf { it.isNotEmpty() }
            ?.let { append("-$it") }
        }
        caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> append("ethernet")
        caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> append("vpn")
        else -> append("network")
      }
      append("-$network")
    }
    return NetworkCoroutineScope(CoroutineScope(SupervisorJob() + ioDispatcher.dispatcher + CoroutineName(name)))
  }

  @GraphExtension.Factory
  public interface Factory {
    public fun create(
      @Provides network: Network,
      @Provides networkCapabilities: StateFlow<NetworkCapabilities>,
      @Provides linkProperties: StateFlow<LinkProperties?>,
    ): NetworkGraph
  }
}

// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.network

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.cancel

@SingleIn(AppScope::class)
public class ConnectivityObserver @dev.zacsweers.metro.Inject constructor(
  private val networkGraphFactory: NetworkGraph.Factory,
  private val connectivityManager: ConnectivityManager,
) {
  private val activeNetworks = mutableMapOf<Network, NetworkState>()

  @android.annotation.SuppressLint("MissingPermission")
  public fun startObserving() {
    val requestBuilder = NetworkRequest.Builder()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      requestBuilder.clearCapabilities()
    } else {
      // Prior to API 30, we have to manually clear out default capabilities if we want ALL networks
      requestBuilder.removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
      requestBuilder.removeCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)
      requestBuilder.removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
    }

    val request = requestBuilder.build()

    connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        activeNetworks[network] = NetworkState(networkGraphFactory.create(network))
      }

      override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        val state = activeNetworks[network] ?: return

        if (state.telephonyGraph == null && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
          state.telephonyGraph = state.graph.telephonyGraphFactory.create()
        }

        state.graph.networkCapabilities.value = networkCapabilities
      }

      override fun onLost(network: Network) {
        activeNetworks.remove(network)?.graph?.coroutineScope?.coroutineScope?.cancel("Network lost")
      }
    })
  }

  private class NetworkState(
    val graph: NetworkGraph,
    var telephonyGraph: TelephonyGraph? = null,
  )
}

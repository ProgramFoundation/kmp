// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.network

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@SingleIn(AppScope::class)
public class ConnectivityObserver @dev.zacsweers.metro.Inject constructor(
  private val networkGraphFactory: NetworkGraph.Factory,
  private val connectivityManager: ConnectivityManager,
) {
  private val _activeNetworks = MutableStateFlow<Map<Network, NetworkGraph>>(emptyMap())
  public val activeNetworks: StateFlow<Map<Network, NetworkGraph>> = _activeNetworks.asStateFlow()

  private val pendingNetworks = mutableMapOf<Network, PendingNetworkState>()
  private val activeNetworkStates = mutableMapOf<Network, ActiveNetworkState>()

  @android.annotation.SuppressLint("MissingPermission")
  public fun startObserving(requestBuilder: NetworkRequest.Builder = NetworkRequest.Builder()) {
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
        pendingNetworks[network] = PendingNetworkState()
      }

      override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        val activeState = activeNetworkStates[network]
        if (activeState != null) {
          activeState.networkCapabilitiesFlow.value = networkCapabilities
          if (activeState.telephonyGraph == null && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            activeState.telephonyGraph = activeState.graph.telephonyGraphFactory.create()
          }
        } else {
          val pending = pendingNetworks[network] ?: return
          pending.networkCapabilities = networkCapabilities
          tryPromotePendingNetwork(network, pending)
        }
      }

      override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
        val activeState = activeNetworkStates[network]
        if (activeState != null) {
          activeState.linkPropertiesFlow.value = linkProperties
        } else {
          val pending = pendingNetworks[network] ?: return
          pending.linkProperties = linkProperties
        }
      }

      override fun onLost(network: Network) {
        pendingNetworks.remove(network)
        activeNetworkStates.remove(network)?.let { state ->
          state.graph.coroutineScope.coroutineScope.cancel("Network lost")
          _activeNetworks.value = _activeNetworks.value - network
        }
      }
    })
  }

  private fun tryPromotePendingNetwork(network: Network, pending: PendingNetworkState) {
    val capabilities = pending.networkCapabilities ?: return
    val networkCapabilitiesFlow = MutableStateFlow(capabilities)
    val linkPropertiesFlow = MutableStateFlow(pending.linkProperties)
    val graph = networkGraphFactory.create(network, networkCapabilitiesFlow.asStateFlow(), linkPropertiesFlow.asStateFlow())
    val activeState = ActiveNetworkState(graph, networkCapabilitiesFlow, linkPropertiesFlow)
    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
      activeState.telephonyGraph = graph.telephonyGraphFactory.create()
    }
    pendingNetworks.remove(network)
    activeNetworkStates[network] = activeState
    _activeNetworks.value = _activeNetworks.value + (network to graph)
  }

  private class PendingNetworkState(
    var networkCapabilities: NetworkCapabilities? = null,
    var linkProperties: LinkProperties? = null,
  )

  private class ActiveNetworkState(
    val graph: NetworkGraph,
    val networkCapabilitiesFlow: MutableStateFlow<NetworkCapabilities>,
    val linkPropertiesFlow: MutableStateFlow<LinkProperties?>,
    var telephonyGraph: TelephonyGraph? = null,
  )
}

// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.network

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.telephony.TelephonyManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.createGraph
import foundation.software.kmp.core.context.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

public annotation class NetworkScope

public sealed interface ActiveNetwork {
  public val network: Network

  public data class Cellular(
    override val network: Network,
    public val telephonyManager: TelephonyManager
  ) : ActiveNetwork

  public data class Other(
    override val network: Network
  ) : ActiveNetwork
}

@DependencyGraph(NetworkScope::class)
public interface NetworkGraph {
  public val activeNetwork: ActiveNetwork
  public val coroutineScope: CoroutineScope
  public val networkCapabilities: SharedFlow<NetworkCapabilities>

  @DependencyGraph.Factory
  public interface Factory {
    public fun create(
      @Provides activeNetwork: ActiveNetwork,
      @Provides coroutineScope: CoroutineScope,
      @Provides networkCapabilities: SharedFlow<NetworkCapabilities>,
    ): NetworkGraph
  }
}

@SingleIn(AppScope::class)
public class ConnectivityObserver @dev.zacsweers.metro.Inject constructor(
  private val applicationContext: ApplicationContext,
  private val networkGraphFactory: NetworkGraph.Factory,
) {
  private val activeNetworks = mutableMapOf<Network, NetworkState>()

  public fun startObserving() {
    val connectivityManager = applicationContext.context.getSystemService(ConnectivityManager::class.java)
    val request = NetworkRequest.Builder()
      .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      .build()

    connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val capabilitiesFlow = MutableSharedFlow<NetworkCapabilities>(replay = 1)

        val capabilities = connectivityManager.getNetworkCapabilities(network)
        if (capabilities != null) {
          capabilitiesFlow.tryEmit(capabilities)
        }

        var activeNetwork: ActiveNetwork = ActiveNetwork.Other(network)
        if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true) {
          var tm = applicationContext.context.getSystemService(TelephonyManager::class.java)
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val specifier = capabilities.networkSpecifier
            if (specifier is android.net.TelephonyNetworkSpecifier) {
              tm = tm?.createForSubscriptionId(specifier.subscriptionId) ?: tm
            }
          }
          if (tm != null) {
            activeNetwork = ActiveNetwork.Cellular(network, tm)
          }
        }

        val graph = networkGraphFactory.create(
          activeNetwork = activeNetwork,
          coroutineScope = scope,
          networkCapabilities = capabilitiesFlow.asSharedFlow()
        )

        activeNetworks[network] = NetworkState(
          graph = graph,
          scope = scope,
          capabilitiesFlow = capabilitiesFlow
        )
      }

      override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        activeNetworks[network]?.capabilitiesFlow?.tryEmit(networkCapabilities)
      }

      override fun onLost(network: Network) {
        activeNetworks.remove(network)?.scope?.cancel("Network lost")
      }
    })
  }

  private class NetworkState(
    val graph: NetworkGraph,
    val scope: CoroutineScope,
    val capabilitiesFlow: MutableSharedFlow<NetworkCapabilities>
  )
}

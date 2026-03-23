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
import dev.zacsweers.metro.createGraphFactory
import foundation.software.kmp.core.context.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

public annotation class NetworkScope

@JvmInline
public value class NetworkCoroutineScope(public val coroutineScope: CoroutineScope)

@DependencyGraph(NetworkScope::class)
public interface NetworkGraph {
  public val network: Network
  public val coroutineScope: NetworkCoroutineScope
  public val networkCapabilities: StateFlow<NetworkCapabilities?>

  @DependencyGraph.Factory
  public interface Factory {
    public fun create(
      @Provides network: Network,
      @Provides coroutineScope: NetworkCoroutineScope,
      @Provides networkCapabilities: StateFlow<NetworkCapabilities?>,
    ): NetworkGraph
  }
}

public annotation class TelephonyScope

@DependencyGraph(TelephonyScope::class)
public interface TelephonyGraph {
  public val telephonyManager: TelephonyManager

  @DependencyGraph.Factory
  public interface Factory {
    public fun create(
      @Provides telephonyManager: TelephonyManager,
    ): TelephonyGraph
  }
}

@SingleIn(AppScope::class)
public class ConnectivityObserver @dev.zacsweers.metro.Inject constructor(
  private val applicationContext: ApplicationContext,
  private val networkGraphFactory: NetworkGraph.Factory,
  private val connectivityManager: ConnectivityManager,
) {
  private val activeNetworks = mutableMapOf<Network, NetworkState>()

  public fun startObserving() {
    val request = NetworkRequest.Builder()
      .clearCapabilities()
      .build()

    connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val capabilitiesFlow = MutableStateFlow<NetworkCapabilities?>(capabilities)

        val graph = networkGraphFactory.create(
          network = network,
          coroutineScope = NetworkCoroutineScope(scope),
          networkCapabilities = capabilitiesFlow.asStateFlow()
        )

        var telephonyGraph: TelephonyGraph? = null
        if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true) {
          var tm = applicationContext.context.getSystemService(TelephonyManager::class.java)
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val specifier = capabilities.networkSpecifier
            if (specifier is android.net.TelephonyNetworkSpecifier) {
              tm = tm?.createForSubscriptionId(specifier.subscriptionId) ?: tm
            }
          }
          if (tm != null) {
            telephonyGraph = dev.zacsweers.metro.createGraphFactory<TelephonyGraph.Factory>().create(tm)
          }
        }

        activeNetworks[network] = NetworkState(
          graph = graph,
          telephonyGraph = telephonyGraph,
          scope = scope,
          capabilitiesFlow = capabilitiesFlow
        )
      }

      override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        activeNetworks[network]?.updateCapabilities(networkCapabilities)
      }

      override fun onLost(network: Network) {
        activeNetworks.remove(network)?.scope?.cancel("Network lost")
      }
    })
  }

  private class NetworkState(
    val graph: NetworkGraph,
    val telephonyGraph: TelephonyGraph?,
    val scope: CoroutineScope,
    private val capabilitiesFlow: MutableStateFlow<NetworkCapabilities?>
  ) {
    fun updateCapabilities(capabilities: NetworkCapabilities) {
      capabilitiesFlow.value = capabilities
    }
  }
}

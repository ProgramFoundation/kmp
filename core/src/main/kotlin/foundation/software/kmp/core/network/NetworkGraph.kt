// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.network

import android.net.Network
import android.net.NetworkCapabilities
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import foundation.software.kmp.core.coroutines.IoDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow

@JvmInline
public value class NetworkCoroutineScope(public val coroutineScope: CoroutineScope)

public annotation class NetworkScope

@GraphExtension(NetworkScope::class)
public interface NetworkGraph {
  public val network: Network
  public val coroutineScope: NetworkCoroutineScope
  public val networkCapabilities: MutableStateFlow<NetworkCapabilities?>
  public val telephonyGraphFactory: TelephonyGraph.Factory

  @Provides
  public fun provideNetworkCoroutineScope(ioDispatcher: IoDispatcher): NetworkCoroutineScope =
    NetworkCoroutineScope(CoroutineScope(SupervisorJob() + ioDispatcher.dispatcher))

  @Provides
  public fun provideNetworkCapabilitiesFlow(): MutableStateFlow<NetworkCapabilities?> =
    MutableStateFlow(null)

  @GraphExtension.Factory
  public interface Factory {
    public fun create(@Provides network: Network): NetworkGraph
  }
}

// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.network

import android.net.Network
import android.net.NetworkCapabilities
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

@JvmInline
public value class NetworkCoroutineScope(public val coroutineScope: CoroutineScope)

public annotation class NetworkScope

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

// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.network

import android.net.Network
import android.net.NetworkCapabilities
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

@JvmInline
public value class NetworkCoroutineScope(public val coroutineScope: CoroutineScope)

public annotation class NetworkScope

@GraphExtension(NetworkScope::class)
public interface NetworkGraph {
  public val network: Network
  public val coroutineScope: NetworkCoroutineScope
  public val networkCapabilities: StateFlow<NetworkCapabilities?>

  @GraphExtension.Factory
  public interface Factory {
    public fun create(
      @Provides network: Network,
      @Provides coroutineScope: NetworkCoroutineScope,
      @Provides networkCapabilities: StateFlow<NetworkCapabilities?>,
    ): NetworkGraph
  }
}

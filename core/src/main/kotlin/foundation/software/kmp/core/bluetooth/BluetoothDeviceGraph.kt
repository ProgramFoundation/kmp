// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.bluetooth

import android.bluetooth.BluetoothDevice
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import foundation.software.kmp.core.coroutines.IoDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

public annotation class BluetoothDeviceScope

@GraphExtension(BluetoothDeviceScope::class)
public interface BluetoothDeviceGraph {
  public val device: BluetoothDevice
  public val coroutineScope: CoroutineScope
  public val a2dpGraphFactory: BluetoothA2dpGraph.Factory
  public val headsetGraphFactory: BluetoothHeadsetGraph.Factory

  @Provides
  @android.annotation.SuppressLint("MissingPermission")
  public fun provideCoroutineScope(device: BluetoothDevice, ioDispatcher: IoDispatcher): CoroutineScope =
    CoroutineScope(SupervisorJob() + ioDispatcher.dispatcher + CoroutineName("bluetooth-${device.name ?: device.address}"))

  @GraphExtension.Factory
  public interface Factory {
    public fun create(@Provides device: BluetoothDevice): BluetoothDeviceGraph
  }
}

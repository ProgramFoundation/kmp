// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.virtualdevice

import android.companion.virtual.VirtualDevice
import android.os.Build
import androidx.annotation.RequiresApi
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import foundation.software.kmp.core.coroutines.IoDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

public annotation class VirtualDeviceScope

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@GraphExtension(VirtualDeviceScope::class)
public interface VirtualDeviceGraph {
  public val device: VirtualDevice
  public val coroutineScope: CoroutineScope

  @Provides
  public fun provideCoroutineScope(device: VirtualDevice, ioDispatcher: IoDispatcher): CoroutineScope =
    CoroutineScope(SupervisorJob() + ioDispatcher.dispatcher + CoroutineName(device.name))

  @GraphExtension.Factory
  public interface Factory {
    public fun create(@Provides device: VirtualDevice): VirtualDeviceGraph
  }
}

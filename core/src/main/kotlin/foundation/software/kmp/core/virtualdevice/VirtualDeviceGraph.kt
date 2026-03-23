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

@JvmInline
public value class VirtualDeviceId(public val id: Int)

@JvmInline
public value class VirtualDeviceName(public val name: String)

@JvmInline
public value class VirtualDeviceCoroutineScope(public val coroutineScope: CoroutineScope)

public annotation class VirtualDeviceScope

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@GraphExtension(VirtualDeviceScope::class)
public interface VirtualDeviceGraph {
  public val virtualDeviceId: VirtualDeviceId
  public val virtualDeviceName: VirtualDeviceName
  public val coroutineScope: VirtualDeviceCoroutineScope

  @Provides
  public fun provideVirtualDeviceId(device: VirtualDevice): VirtualDeviceId = VirtualDeviceId(device.deviceId)

  @Provides
  public fun provideVirtualDeviceName(device: VirtualDevice): VirtualDeviceName = VirtualDeviceName(device.name)

  @Provides
  public fun provideVirtualDeviceCoroutineScope(
    virtualDeviceName: VirtualDeviceName,
    ioDispatcher: IoDispatcher,
  ): VirtualDeviceCoroutineScope =
    VirtualDeviceCoroutineScope(CoroutineScope(SupervisorJob() + ioDispatcher.dispatcher + CoroutineName(virtualDeviceName.name)))

  @GraphExtension.Factory
  public interface Factory {
    public fun create(@Provides device: VirtualDevice): VirtualDeviceGraph
  }
}

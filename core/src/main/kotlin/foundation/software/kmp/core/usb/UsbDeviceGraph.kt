// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.usb

import android.hardware.usb.UsbDevice
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import foundation.software.kmp.core.coroutines.IoDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

public annotation class UsbDeviceScope

@GraphExtension(UsbDeviceScope::class)
public interface UsbDeviceGraph {
  public val device: UsbDevice
  public val coroutineScope: CoroutineScope

  @Provides
  public fun provideCoroutineScope(device: UsbDevice, ioDispatcher: IoDispatcher): CoroutineScope =
    CoroutineScope(SupervisorJob() + ioDispatcher.dispatcher + CoroutineName("usb-${device.deviceName}"))

  @GraphExtension.Factory
  public interface Factory {
    public fun create(@Provides device: UsbDevice): UsbDeviceGraph
  }
}

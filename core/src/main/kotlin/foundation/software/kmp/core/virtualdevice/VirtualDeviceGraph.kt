// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.virtualdevice

import android.companion.virtual.VirtualDevice
import android.content.Context
import android.hardware.display.DisplayManager
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import foundation.software.kmp.core.context.ApplicationContext
import foundation.software.kmp.core.coroutines.IoDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@JvmInline
public value class VirtualDeviceContext(public val context: Context)

public annotation class VirtualDeviceScope

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@GraphExtension(VirtualDeviceScope::class)
public interface VirtualDeviceGraph {
  public val device: VirtualDevice
  public val deviceContext: VirtualDeviceContext
  public val audioManager: AudioManager
  public val displayManager: DisplayManager
  public val coroutineScope: CoroutineScope

  @Provides
  public fun provideVirtualDeviceContext(applicationContext: ApplicationContext, device: VirtualDevice): VirtualDeviceContext =
    VirtualDeviceContext(applicationContext.context.createDeviceContext(device.deviceId))

  @Provides
  public fun provideAudioManager(deviceContext: VirtualDeviceContext): AudioManager =
    deviceContext.context.getSystemService(AudioManager::class.java)

  @Provides
  public fun provideDisplayManager(deviceContext: VirtualDeviceContext): DisplayManager =
    deviceContext.context.getSystemService(DisplayManager::class.java)

  @Provides
  public fun provideCoroutineScope(device: VirtualDevice, ioDispatcher: IoDispatcher): CoroutineScope =
    CoroutineScope(SupervisorJob() + ioDispatcher.dispatcher + CoroutineName(device.name))

  @GraphExtension.Factory
  public interface Factory {
    public fun create(@Provides device: VirtualDevice): VirtualDeviceGraph
  }
}

// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.input

import android.view.InputDevice
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import foundation.software.kmp.core.coroutines.IoDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

public annotation class InputDeviceScope

@GraphExtension(InputDeviceScope::class)
public interface InputDeviceGraph {
  public val device: InputDevice
  public val coroutineScope: CoroutineScope

  @Provides
  public fun provideCoroutineScope(device: InputDevice, ioDispatcher: IoDispatcher): CoroutineScope =
    CoroutineScope(SupervisorJob() + ioDispatcher.dispatcher + CoroutineName("input-${device.name}"))

  @GraphExtension.Factory
  public interface Factory {
    public fun create(@Provides device: InputDevice): InputDeviceGraph
  }
}

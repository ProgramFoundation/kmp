// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.bluetooth

import android.bluetooth.BluetoothA2dp
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides

@GraphExtension(BluetoothDeviceScope::class)
public interface BluetoothA2dpGraph {
  public val a2dp: BluetoothA2dp

  @GraphExtension.Factory
  public interface Factory {
    public fun create(@Provides a2dp: BluetoothA2dp): BluetoothA2dpGraph
  }
}

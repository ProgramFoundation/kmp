// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.bluetooth

import android.bluetooth.BluetoothHeadset
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides

@GraphExtension(BluetoothDeviceScope::class)
public interface BluetoothHeadsetGraph {
  public val headset: BluetoothHeadset

  @GraphExtension.Factory
  public interface Factory {
    public fun create(@Provides headset: BluetoothHeadset): BluetoothHeadsetGraph
  }
}

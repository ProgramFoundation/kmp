// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.network

import android.telephony.TelephonyManager
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides

public annotation class TelephonyScope

@GraphExtension(NetworkScope::class)
public interface TelephonyGraph {
  public val telephonyManager: TelephonyManager

  @GraphExtension.Factory
  public interface Factory {
    public fun create(
      @Provides telephonyManager: TelephonyManager,
    ): TelephonyGraph
  }
}

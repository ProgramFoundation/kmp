// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.display

import android.content.Context
import android.view.Display
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.flow.StateFlow

@JvmInline
public value class DisplayContext(public val context: Context)

public annotation class DisplayScope

@JvmInline
public value class DisplayId(public val id: Int)

@DependencyGraph(DisplayScope::class)
public interface DisplayGraph {
  public val displayId: DisplayId
  public val displayContext: DisplayContext
  public val displayFlow: StateFlow<Display>
  public val displayMetricsFlow: StateFlow<android.util.DisplayMetrics>

  @DependencyGraph.Factory
  public interface Factory {
    public fun create(
      @Provides displayId: DisplayId,
      @Provides displayContext: DisplayContext,
      @Provides displayFlow: StateFlow<Display>,
      @Provides displayMetricsFlow: StateFlow<android.util.DisplayMetrics>
    ): DisplayGraph
  }
}

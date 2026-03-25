// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.display

import android.content.Context
import android.hardware.display.DisplayManager
import android.util.DisplayMetrics
import android.view.Display
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import foundation.software.kmp.core.context.ApplicationContext
import kotlinx.coroutines.flow.StateFlow

@JvmInline
public value class DisplayContext(public val context: Context)

public annotation class DisplayScope

@JvmInline
public value class DisplayId(public val id: Int)

@GraphExtension(DisplayScope::class)
public interface DisplayGraph {
  public val displayId: DisplayId
  public val displayContext: DisplayContext
  public val displayFlow: StateFlow<Display>
  public val displayMetricsFlow: StateFlow<DisplayMetrics>

  @Provides
  public fun provideDisplay(displayManager: DisplayManager, displayId: DisplayId): Display =
    displayManager.getDisplay(displayId.id)

  @Provides
  public fun provideDisplayContext(applicationContext: ApplicationContext, display: Display): DisplayContext =
    DisplayContext(applicationContext.context.createDisplayContext(display))

  @GraphExtension.Factory
  public interface Factory {
    public fun create(
      @Provides displayId: DisplayId,
      @Provides displayFlow: StateFlow<Display>,
      @Provides displayMetricsFlow: StateFlow<DisplayMetrics>,
    ): DisplayGraph
  }
}

// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.display

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import foundation.software.kmp.core.context.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow

@JvmInline
public value class DisplayContext(public val context: Context)

public annotation class DisplayScope

@JvmInline
public value class DisplayId(public val id: Int)

@GraphExtension(DisplayScope::class)
public interface DisplayGraph {
  public val displayId: DisplayId
  public val displayContext: DisplayContext
  public val displayFlow: MutableStateFlow<Display>
  public val displayMetricsFlow: MutableStateFlow<android.util.DisplayMetrics>

  @Provides
  public fun provideDisplay(displayManager: DisplayManager, displayId: DisplayId): Display =
    displayManager.getDisplay(displayId.id)

  @Provides
  public fun provideDisplayContext(applicationContext: ApplicationContext, display: Display): DisplayContext =
    DisplayContext(applicationContext.context.createDisplayContext(display))

  @Provides
  public fun provideDisplayFlow(display: Display): MutableStateFlow<Display> =
    MutableStateFlow(display)

  @Provides
  public fun provideDisplayMetricsFlow(displayContext: DisplayContext): MutableStateFlow<android.util.DisplayMetrics> =
    MutableStateFlow(displayContext.context.resources.displayMetrics)

  @GraphExtension.Factory
  public interface Factory {
    public fun create(@Provides displayId: DisplayId): DisplayGraph
  }
}

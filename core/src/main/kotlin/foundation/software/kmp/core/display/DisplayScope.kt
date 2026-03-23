// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.display

import android.hardware.display.DisplayManager
import android.view.Display
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import foundation.software.kmp.core.context.ApplicationContext
import foundation.software.kmp.core.context.DisplayContext
import foundation.software.kmp.core.coroutines.ApplicationCoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

public annotation class DisplayScope

@JvmInline
public value class DisplayId(public val id: Int)

@DependencyGraph(DisplayScope::class)
public interface DisplayGraph {
  public val displayId: DisplayId
  public val displayContext: DisplayContext
  public val display: Display
  public val displayMetrics: android.util.DisplayMetrics

  @DependencyGraph.Factory
  public interface Factory {
    public fun create(
      @Provides displayId: DisplayId,
      @Provides displayContext: DisplayContext
    ): DisplayGraph
  }
}

@ContributesTo(DisplayScope::class)
public interface DisplayModule {

  @Provides
  @SingleIn(DisplayScope::class)
  public fun provideDisplay(displayContext: DisplayContext): Display {
    val displayManager = displayContext.context.getSystemService(DisplayManager::class.java)
    // The display context is already tied to the specific display
    return displayManager.getDisplay(displayContext.context.display?.displayId ?: Display.DEFAULT_DISPLAY)
  }

  @Provides
  @SingleIn(DisplayScope::class)
  public fun provideDisplayMetrics(displayContext: DisplayContext): android.util.DisplayMetrics {
    return displayContext.context.resources.displayMetrics
  }
}

@SingleIn(AppScope::class)
public class DisplayObserver @dev.zacsweers.metro.Inject constructor(
  private val applicationContext: ApplicationContext,
  private val displayGraphFactory: DisplayGraph.Factory,
  private val appScope: ApplicationCoroutineScope
) {
  private val displayManager = applicationContext.context.getSystemService(DisplayManager::class.java)
  private val activeDisplays = mutableMapOf<Int, DisplayState>()

  private val _displaysFlow = MutableStateFlow<Map<Int, DisplayState>>(emptyMap())
  public val displaysFlow: StateFlow<Map<Int, DisplayState>> = _displaysFlow.asStateFlow()

  private fun createGraphForDisplay(displayId: Int): DisplayGraph {
    val display = displayManager.getDisplay(displayId)
    val displayContext = DisplayContext(applicationContext.context.createDisplayContext(display))
    return displayGraphFactory.create(
      displayId = DisplayId(displayId),
      displayContext = displayContext
    )
  }

  public fun startObserving() {
    val listener = object : DisplayManager.DisplayListener {
      override fun onDisplayAdded(displayId: Int) {
        val graph = createGraphForDisplay(displayId)
        val state = DisplayState(graph)
        activeDisplays[displayId] = state
        _displaysFlow.value = activeDisplays.toMap()
      }

      override fun onDisplayRemoved(displayId: Int) {
        activeDisplays.remove(displayId)
        _displaysFlow.value = activeDisplays.toMap()
      }

      override fun onDisplayChanged(displayId: Int) {
        // Trigger a re-emit to notify subscribers of changes to an existing display
        _displaysFlow.value = activeDisplays.toMap()
      }
    }

    displayManager.registerDisplayListener(listener, null)

    // Initial population
    displayManager.displays.forEach { display ->
      val graph = createGraphForDisplay(display.displayId)
      activeDisplays[display.displayId] = DisplayState(graph)
    }
    _displaysFlow.value = activeDisplays.toMap()
  }

  public data class DisplayState(
    public val graph: DisplayGraph
  )
}

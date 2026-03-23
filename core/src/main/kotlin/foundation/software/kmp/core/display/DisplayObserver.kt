// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.display

import android.hardware.display.DisplayManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.SingleIn
import foundation.software.kmp.core.coroutines.ApplicationCoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@SingleIn(AppScope::class)
public class DisplayObserver @dev.zacsweers.metro.Inject constructor(
  private val displayManager: DisplayManager,
  private val appScope: ApplicationCoroutineScope,
  private val displayGraphFactory: DisplayGraph.Factory,
) {
  private val activeDisplays = mutableMapOf<Int, DisplayGraph>()

  private fun getInitialDisplays(): Map<Int, DisplayGraph> {
    displayManager.displays.forEach { display ->
      activeDisplays[display.displayId] = displayGraphFactory.create(DisplayId(display.displayId))
    }
    return activeDisplays.toMap()
  }

  private val _displaysFlow = MutableStateFlow<Map<Int, DisplayGraph>>(getInitialDisplays())
  public val displaysFlow: StateFlow<Map<Int, DisplayGraph>> = _displaysFlow.asStateFlow()

  public fun startObserving() {
    val listener = object : DisplayManager.DisplayListener {
      override fun onDisplayAdded(displayId: Int) {
        activeDisplays[displayId] = displayGraphFactory.create(DisplayId(displayId))
        _displaysFlow.value = activeDisplays.toMap()
      }

      override fun onDisplayRemoved(displayId: Int) {
        activeDisplays.remove(displayId)
        _displaysFlow.value = activeDisplays.toMap()
      }

      override fun onDisplayChanged(displayId: Int) {
        val graph = activeDisplays[displayId] ?: return
        val display = displayManager.getDisplay(displayId) ?: return
        graph.displayFlow.value = display
        graph.displayMetricsFlow.value = graph.displayContext.context.resources.displayMetrics
      }
    }

    displayManager.registerDisplayListener(listener, null)
  }
}

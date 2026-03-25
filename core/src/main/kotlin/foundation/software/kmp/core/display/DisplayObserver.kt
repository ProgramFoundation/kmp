// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.display

import android.hardware.display.DisplayManager
import android.util.DisplayMetrics
import android.view.Display
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.SingleIn
import foundation.software.kmp.core.context.ApplicationContext
import foundation.software.kmp.core.coroutines.ApplicationCoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@SingleIn(AppScope::class)
public class DisplayObserver @dev.zacsweers.metro.Inject constructor(
  private val displayManager: DisplayManager,
  private val appScope: ApplicationCoroutineScope,
  private val applicationContext: ApplicationContext,
  private val displayGraphFactory: DisplayGraph.Factory,
) {
  private val activeDisplayStates = mutableMapOf<Int, ActiveDisplayState>()
  private val _displaysFlow: MutableStateFlow<Map<Int, DisplayGraph>>

  init {
    displayManager.displays.forEach { display ->
      createDisplayState(display.displayId)?.let { activeDisplayStates[display.displayId] = it }
    }
    _displaysFlow = MutableStateFlow(activeDisplayStates.mapValues { it.value.graph })
  }

  public val displaysFlow: StateFlow<Map<Int, DisplayGraph>> = _displaysFlow.asStateFlow()

  public fun startObserving() {
    val listener = object : DisplayManager.DisplayListener {
      override fun onDisplayAdded(displayId: Int) {
        createDisplayState(displayId)?.let { state ->
          activeDisplayStates[displayId] = state
          _displaysFlow.value = activeDisplayStates.mapValues { it.value.graph }
        }
      }

      override fun onDisplayRemoved(displayId: Int) {
        activeDisplayStates.remove(displayId)
        _displaysFlow.value = activeDisplayStates.mapValues { it.value.graph }
      }

      override fun onDisplayChanged(displayId: Int) {
        val state = activeDisplayStates[displayId] ?: return
        val display = displayManager.getDisplay(displayId) ?: return
        state.displayFlow.value = display
        state.displayMetricsFlow.value = state.graph.displayContext.context.resources.displayMetrics
      }
    }

    displayManager.registerDisplayListener(listener, null)
  }

  private fun createDisplayState(displayId: Int): ActiveDisplayState? {
    val display = displayManager.getDisplay(displayId) ?: return null
    val displayFlow = MutableStateFlow(display)
    val displayContext = applicationContext.context.createDisplayContext(display)
    val displayMetricsFlow = MutableStateFlow(displayContext.resources.displayMetrics)
    val graph = displayGraphFactory.create(DisplayId(displayId), displayFlow.asStateFlow(), displayMetricsFlow.asStateFlow())
    return ActiveDisplayState(graph, displayFlow, displayMetricsFlow)
  }

  private class ActiveDisplayState(
    val graph: DisplayGraph,
    val displayFlow: MutableStateFlow<Display>,
    val displayMetricsFlow: MutableStateFlow<DisplayMetrics>,
  )
}

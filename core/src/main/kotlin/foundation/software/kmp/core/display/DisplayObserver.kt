// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.display

import android.hardware.display.DisplayManager
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
  private val applicationContext: ApplicationContext,
  private val displayManager: DisplayManager,
  private val appScope: ApplicationCoroutineScope
) {
  private val activeDisplays = mutableMapOf<Int, DisplayState>()

  private fun getInitialDisplays(): Map<Int, DisplayState> {
    displayManager.displays.forEach { display ->
      val graph = createGraphForDisplay(display.displayId)
      activeDisplays[display.displayId] = graph
    }
    return activeDisplays.toMap()
  }

  private val _displaysFlow = MutableStateFlow<Map<Int, DisplayState>>(getInitialDisplays())
  public val displaysFlow: StateFlow<Map<Int, DisplayState>> = _displaysFlow.asStateFlow()

  private fun createGraphForDisplay(displayId: Int): DisplayState {
    val display = displayManager.getDisplay(displayId)
    val displayContext = DisplayContext(applicationContext.context.createDisplayContext(display))

    val displayFlow = MutableStateFlow(display)
    val displayMetricsFlow = MutableStateFlow(displayContext.context.resources.displayMetrics)

    val graph = dev.zacsweers.metro.createGraphFactory<DisplayGraph.Factory>().create(
      displayId = DisplayId(displayId),
      displayContext = displayContext,
      displayFlow = displayFlow.asStateFlow(),
      displayMetricsFlow = displayMetricsFlow.asStateFlow()
    )

    return DisplayState(graph, displayFlow, displayMetricsFlow)
  }

  public fun startObserving() {
    val listener = object : DisplayManager.DisplayListener {
      override fun onDisplayAdded(displayId: Int) {
        val state = createGraphForDisplay(displayId)
        activeDisplays[displayId] = state
        _displaysFlow.value = activeDisplays.toMap()
      }

      override fun onDisplayRemoved(displayId: Int) {
        activeDisplays.remove(displayId)
        _displaysFlow.value = activeDisplays.toMap()
      }

      override fun onDisplayChanged(displayId: Int) {
        val state = activeDisplays[displayId]
        if (state != null) {
          val display = displayManager.getDisplay(displayId)
          if (display != null) {
            state.displayFlow.value = display
            state.displayMetricsFlow.value = state.graph.displayContext.context.resources.displayMetrics
          }
        }
      }
    }

    displayManager.registerDisplayListener(listener, null)
  }

  public data class DisplayState(
    public val graph: DisplayGraph,
    internal val displayFlow: MutableStateFlow<Display>,
    internal val displayMetricsFlow: MutableStateFlow<android.util.DisplayMetrics>
  )
}

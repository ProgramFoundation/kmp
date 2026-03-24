// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.input

import android.hardware.input.InputManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@SingleIn(AppScope::class)
public class InputDeviceObserver @dev.zacsweers.metro.Inject constructor(
  private val inputManager: InputManager,
  private val inputDeviceGraphFactory: InputDeviceGraph.Factory,
) {
  private val _inputDevicesFlow: MutableStateFlow<Set<InputDeviceGraph>>

  init {
    val graphs = inputManager.inputDeviceIds
      .toList()
      .mapNotNull { inputManager.getInputDevice(it) }
      .map { inputDeviceGraphFactory.create(it) }
      .toSet()
    _inputDevicesFlow = MutableStateFlow(graphs)
  }

  public val inputDevicesFlow: StateFlow<Set<InputDeviceGraph>> = _inputDevicesFlow.asStateFlow()

  public fun startObserving() {
    inputManager.registerInputDeviceListener(object : InputManager.InputDeviceListener {
      override fun onInputDeviceAdded(deviceId: Int) {
        val device = inputManager.getInputDevice(deviceId) ?: return
        _inputDevicesFlow.value = _inputDevicesFlow.value + inputDeviceGraphFactory.create(device)
      }

      override fun onInputDeviceRemoved(deviceId: Int) {
        val removed = _inputDevicesFlow.value.find { it.device.id == deviceId } ?: return
        removed.coroutineScope.cancel("Input device removed", Exception("Cancelled by InputDeviceObserver"))
        _inputDevicesFlow.value = _inputDevicesFlow.value - removed
      }

      override fun onInputDeviceChanged(deviceId: Int) {
        val existing = _inputDevicesFlow.value.find { it.device.id == deviceId } ?: return
        val device = inputManager.getInputDevice(deviceId) ?: return
        existing.coroutineScope.cancel("Input device changed", Exception("Cancelled by InputDeviceObserver"))
        _inputDevicesFlow.value = _inputDevicesFlow.value - existing + inputDeviceGraphFactory.create(device)
      }
    }, null)
  }
}

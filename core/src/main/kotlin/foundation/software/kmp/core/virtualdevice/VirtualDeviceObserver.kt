// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.virtualdevice

import android.companion.virtual.VirtualDeviceManager
import android.os.Build
import androidx.annotation.RequiresApi
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@SingleIn(AppScope::class)
public class VirtualDeviceObserver @dev.zacsweers.metro.Inject constructor(
  private val virtualDeviceManager: VirtualDeviceManager,
  private val virtualDeviceGraphFactory: VirtualDeviceGraph.Factory,
) {
  private val _virtualDevicesFlow: MutableStateFlow<Set<VirtualDeviceGraph>>

  init {
    val graphs = virtualDeviceManager.virtualDevices.map { virtualDeviceGraphFactory.create(it) }.toMutableSet()
    _virtualDevicesFlow = MutableStateFlow(graphs)
  }

  public val virtualDevicesFlow: StateFlow<Set<VirtualDeviceGraph>> = _virtualDevicesFlow.asStateFlow()

  public fun startObserving() {
    val listener = object : VirtualDeviceManager.VirtualDeviceListener {
      override fun onVirtualDeviceCreated(deviceId: Int) {
        val device = virtualDeviceManager.virtualDevices.find { it.deviceId == deviceId } ?: return
        _virtualDevicesFlow.value = _virtualDevicesFlow.value + virtualDeviceGraphFactory.create(device)
      }

      override fun onVirtualDeviceDeleted(deviceId: Int) {
        val removed = _virtualDevicesFlow.value.find { it.device.deviceId == deviceId } ?: return
        removed.coroutineScope.cancel("Virtual device removed")
        _virtualDevicesFlow.value = _virtualDevicesFlow.value - removed
      }
    }

    virtualDeviceManager.registerVirtualDeviceListener(Runnable::run, listener)
  }
}

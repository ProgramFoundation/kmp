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
  private val activeDeviceGraphs = mutableMapOf<Int, VirtualDeviceGraph>()
  private val _virtualDevicesFlow: MutableStateFlow<Map<Int, VirtualDeviceGraph>>

  init {
    virtualDeviceManager.virtualDevices.forEach { device ->
      val graph = virtualDeviceGraphFactory.create(VirtualDeviceId(device.deviceId), VirtualDeviceName(device.name))
      activeDeviceGraphs[device.deviceId] = graph
    }
    _virtualDevicesFlow = MutableStateFlow(activeDeviceGraphs.toMap())
  }

  public val virtualDevicesFlow: StateFlow<Map<Int, VirtualDeviceGraph>> = _virtualDevicesFlow.asStateFlow()

  public fun startObserving() {
    val listener = object : VirtualDeviceManager.VirtualDeviceListener {
      override fun onVirtualDeviceCreated(deviceId: Int) {
        val device = virtualDeviceManager.virtualDevices.find { it.deviceId == deviceId } ?: return
        val graph = virtualDeviceGraphFactory.create(VirtualDeviceId(device.deviceId), VirtualDeviceName(device.name))
        activeDeviceGraphs[deviceId] = graph
        _virtualDevicesFlow.value = activeDeviceGraphs.toMap()
      }

      override fun onVirtualDeviceDeleted(deviceId: Int) {
        activeDeviceGraphs.remove(deviceId)?.coroutineScope?.coroutineScope?.cancel("Virtual device removed")
        _virtualDevicesFlow.value = activeDeviceGraphs.toMap()
      }
    }

    virtualDeviceManager.registerVirtualDeviceListener(Runnable::run, listener)
  }
}

// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.camera

import android.hardware.camera2.CameraManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@SingleIn(AppScope::class)
public class CameraObserver @dev.zacsweers.metro.Inject constructor(
  private val cameraManager: CameraManager,
  private val cameraGraphFactory: CameraGraph.Factory,
) {
  private val _camerasFlow: MutableStateFlow<Set<CameraGraph>>

  init {
    val graphs = cameraManager.cameraIdList.map { cameraGraphFactory.create(CameraId(it)) }.toSet()
    _camerasFlow = MutableStateFlow(graphs)
  }

  public val camerasFlow: StateFlow<Set<CameraGraph>> = _camerasFlow.asStateFlow()

  public fun startObserving() {
    cameraManager.registerAvailabilityCallback(object : CameraManager.AvailabilityCallback() {
      override fun onCameraAvailable(cameraId: String) {
        _camerasFlow.value = _camerasFlow.value + cameraGraphFactory.create(CameraId(cameraId))
      }

      override fun onCameraUnavailable(cameraId: String) {
        val removed = _camerasFlow.value.find { it.cameraId.id == cameraId } ?: return
        removed.coroutineScope.cancel("Camera unavailable", Exception("Cancelled by CameraObserver"))
        _camerasFlow.value = _camerasFlow.value - removed
      }
    }, null)
  }
}

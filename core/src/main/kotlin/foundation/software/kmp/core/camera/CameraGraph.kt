// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.camera

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import foundation.software.kmp.core.coroutines.IoDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@JvmInline
public value class CameraId(public val id: String)

public annotation class CameraScope

@GraphExtension(CameraScope::class)
public interface CameraGraph {
  public val cameraId: CameraId
  public val characteristics: CameraCharacteristics
  public val coroutineScope: CoroutineScope

  @Provides
  public fun provideCameraCharacteristics(cameraManager: CameraManager, cameraId: CameraId): CameraCharacteristics =
    cameraManager.getCameraCharacteristics(cameraId.id)

  @Provides
  public fun provideCoroutineScope(cameraId: CameraId, ioDispatcher: IoDispatcher): CoroutineScope =
    CoroutineScope(SupervisorJob() + ioDispatcher.dispatcher + CoroutineName("camera-${cameraId.id}"))

  @GraphExtension.Factory
  public interface Factory {
    public fun create(@Provides cameraId: CameraId): CameraGraph
  }
}

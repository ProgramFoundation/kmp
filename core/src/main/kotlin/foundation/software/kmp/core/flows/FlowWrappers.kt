// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.flows

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.CoroutineScope
import dev.zacsweers.metro.Inject

public class SensorFlows @Inject constructor(
  private val sensorManager: SensorManager,
  private val scope: CoroutineScope
) {
  public sealed interface SensorResult {
    public data class SensorChanged(public val event: SensorEvent) : SensorResult
    public data class AccuracyChanged(public val sensor: Sensor?, public val accuracy: Int) : SensorResult
  }

  public fun sensorFlow(sensorType: Int, samplingPeriodUs: Int = SensorManager.SENSOR_DELAY_NORMAL): SharedFlow<SensorResult> =
    callbackFlow {
      val sensor = sensorManager.getDefaultSensor(sensorType)
      if (sensor == null) {
        close(IllegalArgumentException("Sensor $sensorType not found"))
        return@callbackFlow
      }

      val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
          trySend(SensorResult.SensorChanged(event))
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
          trySend(SensorResult.AccuracyChanged(sensor, accuracy))
        }
      }

      sensorManager.registerListener(listener, sensor, samplingPeriodUs)

      awaitClose {
        sensorManager.unregisterListener(listener)
      }
    }.shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1)

  public fun accelerometerFlow(): SharedFlow<SensorResult> = sensorFlow(Sensor.TYPE_ACCELEROMETER)
  public fun gyroscopeFlow(): SharedFlow<SensorResult> = sensorFlow(Sensor.TYPE_GYROSCOPE)
}

public class LocationFlows @Inject constructor(
  private val locationManager: LocationManager,
  private val scope: CoroutineScope
) {
  public fun locationFlow(provider: String, minTimeMs: Long = 1000L, minDistanceM: Float = 0f): SharedFlow<Location> =
    callbackFlow {
      val listener = LocationListener { location ->
        trySend(location)
      }

      try {
        locationManager.requestLocationUpdates(provider, minTimeMs, minDistanceM, listener)
      } catch (e: SecurityException) {
        close(e)
      }

      awaitClose {
        locationManager.removeUpdates(listener)
      }
    }.shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1)

  public fun gpsLocationFlow(): SharedFlow<Location> = locationFlow(LocationManager.GPS_PROVIDER)
  public fun networkLocationFlow(): SharedFlow<Location> = locationFlow(LocationManager.NETWORK_PROVIDER)
}

public class BroadcastFlows @Inject constructor(
  private val context: Context,
  private val scope: CoroutineScope
) {
  public fun broadcastFlow(intentFilter: IntentFilter): SharedFlow<Intent> =
    callbackFlow {
      val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
          trySend(intent)
        }
      }

      context.registerReceiver(receiver, intentFilter)

      awaitClose {
        context.unregisterReceiver(receiver)
      }
    }.shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1)
}

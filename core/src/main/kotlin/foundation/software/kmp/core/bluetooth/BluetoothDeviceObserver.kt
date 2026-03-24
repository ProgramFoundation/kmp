// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.SingleIn
import foundation.software.kmp.core.context.ApplicationContext
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@SuppressLint("MissingPermission")
@SingleIn(AppScope::class)
public class BluetoothDeviceObserver @dev.zacsweers.metro.Inject constructor(
  private val bluetoothManager: BluetoothManager,
  private val applicationContext: ApplicationContext,
  private val bluetoothDeviceGraphFactory: BluetoothDeviceGraph.Factory,
) {
  private val activeDeviceStates = mutableMapOf<String, ActiveBluetoothDeviceState>()
  private val _bluetoothDevicesFlow: MutableStateFlow<Set<BluetoothDeviceGraph>>

  init {
    bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).forEach { device ->
      val graph = bluetoothDeviceGraphFactory.create(device)
      activeDeviceStates[device.address] = ActiveBluetoothDeviceState(graph)
    }
    _bluetoothDevicesFlow = MutableStateFlow(activeDeviceStates.values.map { it.graph }.toSet())
  }

  public val bluetoothDevicesFlow: StateFlow<Set<BluetoothDeviceGraph>> = _bluetoothDevicesFlow.asStateFlow()

  public fun startObserving() {
    applicationContext.context.registerReceiver(object : BroadcastReceiver() {
      override fun onReceive(context: Context, intent: Intent) {
        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
          @Suppress("DEPRECATION")
          intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        } ?: return
        when (intent.action) {
          BluetoothDevice.ACTION_ACL_CONNECTED -> {
            val graph = bluetoothDeviceGraphFactory.create(device)
            activeDeviceStates[device.address] = ActiveBluetoothDeviceState(graph)
            _bluetoothDevicesFlow.value = activeDeviceStates.values.map { it.graph }.toSet()
          }
          BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
            val state = activeDeviceStates.remove(device.address) ?: return
            state.graph.coroutineScope.cancel("Bluetooth device disconnected", Exception("Cancelled by BluetoothDeviceObserver"))
            _bluetoothDevicesFlow.value = activeDeviceStates.values.map { it.graph }.toSet()
          }
        }
      }
    }, IntentFilter().apply {
      addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
      addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
    })

    val profileListener = object : BluetoothProfile.ServiceListener {
      override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
        activeDeviceStates.values.forEach { state ->
          when (profile) {
            BluetoothProfile.A2DP -> state.a2dpGraph = state.graph.a2dpGraphFactory.create(proxy as BluetoothA2dp)
            BluetoothProfile.HEADSET -> state.headsetGraph = state.graph.headsetGraphFactory.create(proxy as BluetoothHeadset)
          }
        }
      }

      override fun onServiceDisconnected(profile: Int) {
        activeDeviceStates.values.forEach { state ->
          when (profile) {
            BluetoothProfile.A2DP -> state.a2dpGraph = null
            BluetoothProfile.HEADSET -> state.headsetGraph = null
          }
        }
      }
    }

    bluetoothManager.adapter.getProfileProxy(applicationContext.context, profileListener, BluetoothProfile.A2DP)
    bluetoothManager.adapter.getProfileProxy(applicationContext.context, profileListener, BluetoothProfile.HEADSET)
  }

  private class ActiveBluetoothDeviceState(
    val graph: BluetoothDeviceGraph,
    var a2dpGraph: BluetoothA2dpGraph? = null,
    var headsetGraph: BluetoothHeadsetGraph? = null,
  )
}

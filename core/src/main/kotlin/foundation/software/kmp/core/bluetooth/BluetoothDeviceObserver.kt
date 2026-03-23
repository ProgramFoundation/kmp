// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.bluetooth

import android.bluetooth.BluetoothDevice
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

@SingleIn(AppScope::class)
public class BluetoothDeviceObserver @dev.zacsweers.metro.Inject constructor(
  private val bluetoothManager: BluetoothManager,
  private val applicationContext: ApplicationContext,
  private val bluetoothDeviceGraphFactory: BluetoothDeviceGraph.Factory,
) {
  private val _bluetoothDevicesFlow: MutableStateFlow<Set<BluetoothDeviceGraph>>

  init {
    val graphs = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT)
      .map { bluetoothDeviceGraphFactory.create(it) }
      .toSet()
    _bluetoothDevicesFlow = MutableStateFlow(graphs)
  }

  public val bluetoothDevicesFlow: StateFlow<Set<BluetoothDeviceGraph>> = _bluetoothDevicesFlow.asStateFlow()

  public fun startObserving() {
    val receiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context, intent: Intent) {
        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
          @Suppress("DEPRECATION")
          intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        } ?: return
        when (intent.action) {
          BluetoothDevice.ACTION_ACL_CONNECTED ->
            _bluetoothDevicesFlow.value = _bluetoothDevicesFlow.value + bluetoothDeviceGraphFactory.create(device)
          BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
            val removed = _bluetoothDevicesFlow.value.find { it.device.address == device.address } ?: return
            removed.coroutineScope.cancel("Bluetooth device disconnected", Exception("Cancelled by BluetoothDeviceObserver"))
            _bluetoothDevicesFlow.value = _bluetoothDevicesFlow.value - removed
          }
        }
      }
    }

    applicationContext.context.registerReceiver(receiver, IntentFilter().apply {
      addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
      addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
    })
  }
}

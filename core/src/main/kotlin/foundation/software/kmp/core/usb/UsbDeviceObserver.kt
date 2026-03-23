// Copyright (C) 2025
// SPDX-License-Identifier: Apache-2.0
package foundation.software.kmp.core.usb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.SingleIn
import foundation.software.kmp.core.context.ApplicationContext
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@SingleIn(AppScope::class)
public class UsbDeviceObserver @dev.zacsweers.metro.Inject constructor(
  private val usbManager: UsbManager,
  private val applicationContext: ApplicationContext,
  private val usbDeviceGraphFactory: UsbDeviceGraph.Factory,
) {
  private val _usbDevicesFlow: MutableStateFlow<Set<UsbDeviceGraph>>

  init {
    val graphs = usbManager.deviceList.values.map { usbDeviceGraphFactory.create(it) }.toSet()
    _usbDevicesFlow = MutableStateFlow(graphs)
  }

  public val usbDevicesFlow: StateFlow<Set<UsbDeviceGraph>> = _usbDevicesFlow.asStateFlow()

  public fun startObserving() {
    val receiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context, intent: Intent) {
        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
        } else {
          @Suppress("DEPRECATION")
          intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
        } ?: return
        when (intent.action) {
          UsbManager.ACTION_USB_DEVICE_ATTACHED ->
            _usbDevicesFlow.value = _usbDevicesFlow.value + usbDeviceGraphFactory.create(device)
          UsbManager.ACTION_USB_DEVICE_DETACHED -> {
            val removed = _usbDevicesFlow.value.find { it.device.deviceName == device.deviceName } ?: return
            removed.coroutineScope.cancel("USB device detached", Exception("Cancelled by UsbDeviceObserver"))
            _usbDevicesFlow.value = _usbDevicesFlow.value - removed
          }
        }
      }
    }

    applicationContext.context.registerReceiver(receiver, IntentFilter().apply {
      addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
      addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
    })
  }
}

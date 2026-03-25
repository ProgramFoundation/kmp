package foundation.software.kmp.hid.remote

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import foundation.software.kmp.hid.btclassic.BluetoothClassicHidDevice
import foundation.software.kmp.hid.gatt.GattHidDevice
import foundation.software.kmp.hid.usb.UsbHidDevice

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Placeholder instantiation to ensure modules are wired correctly
        val btHid = BluetoothClassicHidDevice("BT Controller")
        val gattHid = GattHidDevice("GATT Server")
        val usbHid = UsbHidDevice("USB Controller")

        // TODO: UI implementation to read USB and relay over BT
    }
}

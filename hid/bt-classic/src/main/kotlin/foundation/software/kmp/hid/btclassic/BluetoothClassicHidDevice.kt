package foundation.software.kmp.hid.btclassic

import foundation.software.kmp.hid.domain.HidDevice

/**
 * Placeholder implementation for HID over Bluetooth Classic using Android Bluetooth HID API.
 */
class BluetoothClassicHidDevice(override val name: String) : HidDevice {

    override suspend fun connect(): Boolean {
        // TODO: Implement Bluetooth Classic HID connection logic
        return false
    }

    override suspend fun disconnect() {
        // TODO: Implement Bluetooth Classic HID disconnection logic
    }

    override suspend fun sendReport(reportId: Int, data: ByteArray) {
        // TODO: Implement Bluetooth Classic HID sending report logic
    }

    override suspend fun receiveReport(): ByteArray {
        // TODO: Implement Bluetooth Classic HID receiving report logic
        return ByteArray(0)
    }
}

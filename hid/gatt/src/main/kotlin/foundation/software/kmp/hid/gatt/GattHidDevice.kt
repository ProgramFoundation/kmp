package foundation.software.kmp.hid.gatt

import foundation.software.kmp.hid.domain.HidDevice

/**
 * Placeholder implementation for spinning up a GATT server and implementing HID over GATT.
 */
class GattHidDevice(override val name: String) : HidDevice {

    override suspend fun connect(): Boolean {
        // TODO: Implement GATT server connection/advertising logic
        return false
    }

    override suspend fun disconnect() {
        // TODO: Implement GATT server disconnection/teardown logic
    }

    override suspend fun sendReport(reportId: Int, data: ByteArray) {
        // TODO: Implement GATT server sending report logic (e.g., via characteristic notifications)
    }

    override suspend fun receiveReport(): ByteArray {
        // TODO: Implement GATT server receiving report logic (e.g., from write requests)
        return ByteArray(0)
    }
}

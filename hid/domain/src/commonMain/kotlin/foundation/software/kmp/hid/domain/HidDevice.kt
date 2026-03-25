package foundation.software.kmp.hid.domain

/**
 * Common domain model for HID (Human Interface Device) protocol.
 */
interface HidDevice {
    /**
     * The name of the HID device.
     */
    val name: String

    /**
     * Connect to the device.
     */
    suspend fun connect(): Boolean

    /**
     * Disconnect from the device.
     */
    suspend fun disconnect()

    /**
     * Send a report to the device.
     */
    suspend fun sendReport(reportId: Int, data: ByteArray)

    /**
     * Receive a report from the device.
     */
    suspend fun receiveReport(): ByteArray
}

package ir.bolino.traficcollector.model

data class TrafficData(
    val timestamp: Long,
    val sourceIp: String,
    val destIp: String,
    val sourcePort: Int,
    val destPort: Int,
    val protocol: String,
    val dataLength: Int,
    val direction: String, // OUTGOING or INCOMING
    val rawData: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TrafficData

        if (timestamp != other.timestamp) return false
        if (sourceIp != other.sourceIp) return false
        if (destIp != other.destIp) return false
        if (sourcePort != other.sourcePort) return false
        if (destPort != other.destPort) return false
        if (protocol != other.protocol) return false
        if (dataLength != other.dataLength) return false
        if (direction != other.direction) return false
        if (rawData != null) {
            if (other.rawData == null) return false
            if (!rawData.contentEquals(other.rawData)) return false
        } else if (other.rawData != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + sourceIp.hashCode()
        result = 31 * result + destIp.hashCode()
        result = 31 * result + sourcePort
        result = 31 * result + destPort
        result = 31 * result + protocol.hashCode()
        result = 31 * result + dataLength
        result = 31 * result + direction.hashCode()
        result = 31 * result + (rawData?.contentHashCode() ?: 0)
        return result
    }
}

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: android.graphics.drawable.Drawable?
)
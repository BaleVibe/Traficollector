package ir.bolino.traficcollector.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import ir.bolino.traficcollector.MainActivity
import ir.bolino.traficcollector.R
import ir.bolino.traficcollector.model.TrafficData
import ir.bolino.traficcollector.utils.TrafficDataStore
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.util.concurrent.ConcurrentLinkedQueue

class TrafficVpnService : VpnService() {

    private val TAG = "TrafficVpnService"
    private val NOTIFICATION_CHANNEL_ID = "TrafficVpnServiceChannel"
    private val NOTIFICATION_ID = 1

    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val trafficQueue = ConcurrentLinkedQueue<TrafficData>()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "VPN Service started")
        
        when (intent?.action) {
            "START_VPN" -> startVpn()
            "STOP_VPN" -> stopVpn()
        }
        
        return START_STICKY
    }

    private fun startVpn() {
        if (isRunning) return
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        scope.launch {
            try {
                setupVpnInterface()
                isRunning = true
                runVpnTraffic()
            } catch (e: Exception) {
                Log.e(TAG, "Error starting VPN", e)
                stopVpn()
            }
        }
    }

    private fun stopVpn() {
        isRunning = false
        vpnInterface?.close()
        vpnInterface = null
        job.cancelChildren()
        stopForeground(true)
        stopSelf()
    }

    private fun setupVpnInterface(): ParcelFileDescriptor {
        val builder = Builder()
            .setSession("Traficcollector")
            .addAddress("10.0.0.2", 24)
            .addDnsServer("8.8.8.8")
            .addDnsServer("8.8.4.4")
            .addRoute("0.0.0.0", 0)

        return builder.establish()
            ?: throw IllegalStateException("Cannot establish VPN interface")
    }

    private suspend fun runVpnTraffic() {
        val vpnInput = FileInputStream(vpnInterface?.fileDescriptor)
        val vpnOutput = FileOutputStream(vpnInterface?.fileDescriptor)
        
        val buffer = ByteBuffer.allocate(32767)
        
        while (isRunning) {
            try {
                // Read from VPN interface
                val bytesRead = vpnInput.read(buffer.array())
                if (bytesRead > 0) {
                    processPacket(buffer.array(), bytesRead, true)
                    
                    // Forward to actual network
                    forwardToNetwork(buffer.array(), bytesRead)
                }
                
                delay(10) // Small delay to prevent busy waiting
                
            } catch (e: Exception) {
                if (isRunning) {
                    Log.e(TAG, "Error in VPN traffic loop", e)
                }
            }
        }
    }

    private suspend fun processPacket(packet: ByteArray, length: Int, isOutgoing: Boolean) {
        try {
            val ipHeader = parseIpHeader(packet, length)
            if (ipHeader != null) {
                val trafficData = TrafficData(
                    timestamp = System.currentTimeMillis(),
                    sourceIp = ipHeader.sourceIp,
                    destIp = ipHeader.destIp,
                    sourcePort = ipHeader.sourcePort,
                    destPort = ipHeader.destPort,
                    protocol = ipHeader.protocol,
                    dataLength = length,
                    direction = if (isOutgoing) "OUTGOING" else "INCOMING",
                    rawData = if (TrafficDataStore.monitoringMode == "FULL") packet.copyOfRange(0, length) else null
                )
                
                TrafficDataStore.addTrafficData(trafficData)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing packet", e)
        }
    }

    private fun parseIpHeader(packet: ByteArray, length: Int): IpHeaderInfo? {
        if (length < 20) return null
        
        val ipVersion = (packet[0].toInt() and 0xF0) shr 4
        if (ipVersion != 4) return null // Only IPv4
        
        val headerLength = (packet[0].toInt() and 0x0F) * 4
        if (length < headerLength) return null
        
        val protocol = packet[9].toInt() and 0xFF
        val sourceIp = "${packet[12].toInt() and 0xFF}.${packet[13].toInt() and 0xFF}.${packet[14].toInt() and 0xFF}.${packet[15].toInt() and 0xFF}"
        val destIp = "${packet[16].toInt() and 0xFF}.${packet[17].toInt() and 0xFF}.${packet[18].toInt() and 0xFF}.${packet[19].toInt() and 0xFF}"
        
        var sourcePort = 0
        var destPort = 0
        
        if (protocol == 6 || protocol == 17) { // TCP or UDP
            if (length >= headerLength + 8) {
                sourcePort = ((packet[headerLength].toInt() and 0xFF) shl 8) or (packet[headerLength + 1].toInt() and 0xFF)
                destPort = ((packet[headerLength + 2].toInt() and 0xFF) shl 8) or (packet[headerLength + 3].toInt() and 0xFF)
            }
        }
        
        return IpHeaderInfo(
            sourceIp = sourceIp,
            destIp = destIp,
            sourcePort = sourcePort,
            destPort = destPort,
            protocol = when (protocol) {
                1 -> "ICMP"
                6 -> "TCP"
                17 -> "UDP"
                else -> "OTHER($protocol)"
            }
        )
    }

    private fun forwardToNetwork(packet: ByteArray, length: Int) {
        // This is a simplified implementation
        // In a real implementation, you would forward the packet to the actual network
        // using raw sockets or other mechanisms
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Traffic Collector VPN Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors network traffic for selected applications"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Traffic Collector Active")
            .setContentText("Monitoring network traffic")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
        Log.d(TAG, "VPN Service destroyed")
    }

    data class IpHeaderInfo(
        val sourceIp: String,
        val destIp: String,
        val sourcePort: Int,
        val destPort: Int,
        val protocol: String
    )
}
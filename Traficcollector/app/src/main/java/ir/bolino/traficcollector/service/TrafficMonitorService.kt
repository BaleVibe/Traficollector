package ir.bolino.traficcollector.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import ir.bolino.traficcollector.MainActivity
import ir.bolino.traficcollector.R

class TrafficMonitorService : android.app.Service() {

    private val TAG = "TrafficMonitorService"
    private val NOTIFICATION_CHANNEL_ID = "TrafficMonitorServiceChannel"
    private val NOTIFICATION_ID = 2

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Traffic Monitor Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Traffic Monitor Service started")
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        return START_STICKY
    }

    override fun onBind(intent: Intent?): android.os.IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Traffic Monitor Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background service for traffic monitoring"
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
            .setContentTitle("Traffic Monitor")
            .setContentText("Service is running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Traffic Monitor Service destroyed")
    }
}
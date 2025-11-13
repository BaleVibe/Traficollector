package ir.bolino.traficcollector.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import ir.bolino.traficcollector.model.AppInfo
import ir.bolino.traficcollector.model.TrafficData
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

object TrafficDataStore {
    
    private val trafficDataList = mutableListOf<TrafficData>()
    private val appInfoCache = ConcurrentHashMap<String, AppInfo>()
    var selectedAppPackage: String? = null
    var monitoringMode: String = "SIMPLE" // SIMPLE or FULL
    
    fun addTrafficData(data: TrafficData) {
        synchronized(trafficDataList) {
            trafficDataList.add(data)
            // Keep only last 1000 entries to prevent memory issues
            if (trafficDataList.size > 1000) {
                trafficDataList.removeAt(0)
            }
        }
    }
    
    fun getTrafficData(): List<TrafficData> {
        return synchronized(trafficDataList) {
            trafficDataList.toList()
        }
    }
    
    fun getTrafficDataForApp(packageName: String): List<TrafficData> {
        return synchronized(trafficDataList) {
            trafficDataList.filter { data ->
                // Filter by app - this is simplified, in real implementation you'd need
                // to map network connections to specific apps
                true // For now, return all data
            }
        }
    }
    
    fun clearTrafficData() {
        synchronized(trafficDataList) {
            trafficDataList.clear()
        }
    }
    
    fun exportToCSV(): String {
        val data = getTrafficData()
        val csv = StringBuilder()
        csv.appendLine("Timestamp,Source IP,Destination IP,Source Port,Destination Port,Protocol,Data Length,Direction")
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        
        data.forEach { traffic ->
            csv.appendLine(
                "${dateFormat.format(Date(traffic.timestamp))}," +
                "${traffic.sourceIp}," +
                "${traffic.destIp}," +
                "${traffic.sourcePort}," +
                "${traffic.destPort}," +
                "${traffic.protocol}," +
                "${traffic.dataLength}," +
                "${traffic.direction}"
            )
        }
        
        return csv.toString()
    }
    
    fun getInstalledApps(context: Context): List<AppInfo> {
        val packageManager = context.packageManager
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        
        val appInfoList = mutableListOf<AppInfo>()
        
        installedApps.forEach { appInfo ->
            // Filter out system apps
            if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                try {
                    val appName = packageManager.getApplicationLabel(appInfo).toString()
                    val packageName = appInfo.packageName
                    val icon = packageManager.getApplicationIcon(appInfo)
                    
                    appInfoList.add(AppInfo(packageName, appName, icon))
                } catch (e: Exception) {
                    // Skip apps that can't be loaded
                }
            }
        }
        
        return appInfoList.sortedBy { it.appName }
    }
    
    fun formatBytes(bytes: Int): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return "%.1f KB".format(kb)
        val mb = kb / 1024.0
        if (mb < 1024) return "%.1f MB".format(mb)
        val gb = mb / 1024.0
        return "%.1f GB".format(gb)
    }
    
    fun formatTimestamp(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
    
    fun getProtocolColor(protocol: String): Int {
        return when (protocol) {
            "TCP" -> android.graphics.Color.parseColor("#4CAF50")
            "UDP" -> android.graphics.Color.parseColor("#2196F3")
            "ICMP" -> android.graphics.Color.parseColor("#FF9800")
            else -> android.graphics.Color.parseColor("#9E9E9E")
        }
    }
    
    fun getDirectionColor(direction: String): Int {
        return when (direction) {
            "OUTGOING" -> android.graphics.Color.parseColor("#F44336")
            "INCOMING" -> android.graphics.Color.parseColor("#4CAF50")
            else -> android.graphics.Color.parseColor("#9E9E9E")
        }
    }
}
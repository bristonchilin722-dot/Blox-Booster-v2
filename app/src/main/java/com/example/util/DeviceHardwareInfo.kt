package com.example.util

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Point
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.view.WindowManager

data class DeviceSpec(
    val deviceName: String,
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val apiLevel: Int,
    val cpuCores: Int,
    val cpuArch: String,
    val totalRamMb: Long,
    val availableRamMb: Long,
    val ramUsagePercent: Int,
    val screenWidth: Int,
    val screenHeight: Int,
    val screenDpi: Int,
    val refreshRateHz: Int,
    val thermalStatus: String,
    val batteryPercent: Int,
    val isCharging: Boolean
)

object DeviceHardwareInfo {

    fun getSpecs(context: Context): DeviceSpec {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)

        val totalRamMb = memInfo.totalMem / (1024 * 1024)
        val availRamMb = memInfo.availMem / (1024 * 1024)
        val ramUsedPercent = if (totalRamMb > 0) {
            (((totalRamMb - availRamMb).toDouble() / totalRamMb) * 100).toInt()
        } else 0

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        var width = 1080
        var height = 2400
        var refreshRate = 60

        if (wm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bounds = wm.currentWindowMetrics.bounds
                width = bounds.width()
                height = bounds.height()
                val display = context.display
                refreshRate = display?.refreshRate?.toInt() ?: 60
            } else {
                @Suppress("DEPRECATION")
                val display = wm.defaultDisplay
                val size = Point()
                @Suppress("DEPRECATION")
                display.getRealSize(size)
                width = size.x
                height = size.y
                @Suppress("DEPRECATION")
                refreshRate = display.refreshRate.toInt()
            }
        }

        val dpi = context.resources.displayMetrics.densityDpi

        // Battery
        val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, batteryFilter)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 100
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        // Thermal Status
        var thermal = "Normal"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            thermal = when (pm?.currentThermalStatus) {
                PowerManager.THERMAL_STATUS_NONE -> "Cool (Nominal)"
                PowerManager.THERMAL_STATUS_LIGHT -> "Light Warmth"
                PowerManager.THERMAL_STATUS_MODERATE -> "Moderate Warmth"
                PowerManager.THERMAL_STATUS_SEVERE -> "Throttling Risk"
                PowerManager.THERMAL_STATUS_CRITICAL -> "Thermal Throttling"
                PowerManager.THERMAL_STATUS_EMERGENCY -> "Emergency Cool Down"
                PowerManager.THERMAL_STATUS_SHUTDOWN -> "Critical Heat"
                else -> "Cool (Nominal)"
            }
        }

        val primaryAbi = if (Build.SUPPORTED_ABIS.isNotEmpty()) Build.SUPPORTED_ABIS[0] else "arm64-v8a"

        return DeviceSpec(
            deviceName = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            androidVersion = "Android ${Build.VERSION.RELEASE}",
            apiLevel = Build.VERSION.SDK_INT,
            cpuCores = Runtime.getRuntime().availableProcessors(),
            cpuArch = primaryAbi,
            totalRamMb = totalRamMb,
            availableRamMb = availRamMb,
            ramUsagePercent = ramUsedPercent,
            screenWidth = width,
            screenHeight = height,
            screenDpi = dpi,
            refreshRateHz = refreshRate,
            thermalStatus = thermal,
            batteryPercent = batteryPct,
            isCharging = isCharging
        )
    }
}

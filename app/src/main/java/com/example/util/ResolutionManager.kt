package com.example.util

import android.content.Context
import android.os.IBinder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

data class ResolutionPreset(
    val name: String,
    val width: Int,
    val height: Int,
    val densityDpi: Int,
    val scaleFactor: Float,
    val gpuWorkloadPercent: Int,
    val description: String,
    val isRecommendedForLowEnd: Boolean
)

data class ResolutionState(
    val currentWidth: Int = 1080,
    val currentHeight: Int = 2400,
    val nativeWidth: Int = 1080,
    val nativeHeight: Int = 2400,
    val nativeDpi: Int = 400,
    val currentDpi: Int = 400,
    val isScaled: Boolean = false,
    val isShizukuAvailable: Boolean = false,
    val activePresetName: String = "Native Resolution",
    val revertTimerSeconds: Int = 0,
    val isAwaitingConfirmation: Boolean = false
)

object ResolutionManager {

    private val _state = MutableStateFlow(ResolutionState())
    val state: StateFlow<ResolutionState> = _state.asStateFlow()

    fun init(nativeW: Int, nativeH: Int, nativeDpi: Int) {
        val isShizuku = checkShizukuAvailable()
        _state.value = ResolutionState(
            currentWidth = nativeW,
            currentHeight = nativeH,
            nativeWidth = nativeW,
            nativeHeight = nativeH,
            nativeDpi = nativeDpi,
            currentDpi = nativeDpi,
            isScaled = false,
            isShizukuAvailable = isShizuku,
            activePresetName = "Native Resolution"
        )
    }

    private fun checkShizukuAvailable(): Boolean {
        return try {
            val service = Class.forName("android.os.ServiceManager")
                .getMethod("getService", String::class.java)
                .invoke(null, "moe.shizuku.server") as? IBinder
            service != null && service.pingBinder()
        } catch (_: Throwable) {
            false
        }
    }

    fun getPresets(nativeW: Int, nativeH: Int, nativeDpi: Int): List<ResolutionPreset> {
        val w = nativeW.coerceAtLeast(720)
        val h = nativeH.coerceAtLeast(1280)
        val aspect = h.toFloat() / w.toFloat()

        fun makePreset(scale: Float, name: String, desc: String, lowEndRec: Boolean): ResolutionPreset {
            // align to 8 pixels for GPU stride efficiency
            val scaledW = ((w * scale) / 8).roundToInt() * 8
            val scaledH = ((scaledW * aspect) / 8).roundToInt() * 8
            val scaledDpi = (nativeDpi * scale).roundToInt().coerceAtLeast(160)
            val workload = (scale * scale * 100).roundToInt()
            return ResolutionPreset(
                name = name,
                width = scaledW,
                height = scaledH,
                densityDpi = scaledDpi,
                scaleFactor = scale,
                gpuWorkloadPercent = workload,
                description = desc,
                isRecommendedForLowEnd = lowEndRec
            )
        }

        return listOf(
            ResolutionPreset(
                name = "Native Full HD+",
                width = nativeW,
                height = nativeH,
                densityDpi = nativeDpi,
                scaleFactor = 1.0f,
                gpuWorkloadPercent = 100,
                description = "Default display resolution. Maximum visual sharpness, standard GPU workload.",
                isRecommendedForLowEnd = false
            ),
            makePreset(
                scale = 0.83f,
                name = "900p HD+ Enhanced",
                desc = "Reduces GPU pixel shader workload by ~31% with virtually unnoticeable loss in sharpness.",
                lowEndRec = false
            ),
            makePreset(
                scale = 0.67f,
                name = "720p HD Balanced (Recommended)",
                desc = "Cuts GPU pixel fill workload in half (55% reduction). Best balance for Poco C71 and budget phones.",
                lowEndRec = true
            ),
            makePreset(
                scale = 0.50f,
                name = "540p Potato Visual Extreme",
                desc = "75% GPU fillrate relief! Delivers maximum FPS boost in heavy Roblox games like Blox Fruits PvP.",
                lowEndRec = true
            )
        )
    }

    fun generateAdbCommand(targetWidth: Int, targetHeight: Int, targetDpi: Int): String {
        return "adb shell wm size ${targetWidth}x${targetHeight} && adb shell wm density $targetDpi"
    }

    fun generateResetAdbCommand(): String {
        return "adb shell wm size reset && adb shell wm density reset"
    }

    fun applyPresetSimulation(preset: ResolutionPreset) {
        _state.value = _state.value.copy(
            currentWidth = preset.width,
            currentHeight = preset.height,
            currentDpi = preset.densityDpi,
            isScaled = preset.scaleFactor < 0.98f,
            activePresetName = preset.name
        )
    }

    fun resetToNative() {
        val curr = _state.value
        _state.value = curr.copy(
            currentWidth = curr.nativeWidth,
            currentHeight = curr.nativeHeight,
            currentDpi = curr.nativeDpi,
            isScaled = false,
            activePresetName = "Native Resolution",
            isAwaitingConfirmation = false,
            revertTimerSeconds = 0
        )
    }
}

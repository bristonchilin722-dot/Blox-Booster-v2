package com.example.util

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    val activePresetName: String = "Native Resolution",
    val revertTimerSeconds: Int = 0,
    val isAwaitingConfirmation: Boolean = false,
    val lastExecutionLog: String? = null,
    val lastError: String? = null,
    val isApplying: Boolean = false
)

object ResolutionManager {

    private val _state = MutableStateFlow(ResolutionState())
    val state: StateFlow<ResolutionState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)
    private var revertJob: Job? = null

    fun init(nativeW: Int, nativeH: Int, nativeDpi: Int) {
        val safeW = nativeW.coerceAtLeast(720)
        val safeH = nativeH.coerceAtLeast(1280)
        val safeDpi = nativeDpi.coerceAtLeast(160)

        _state.value = ResolutionState(
            currentWidth = safeW,
            currentHeight = safeH,
            nativeWidth = safeW,
            nativeHeight = safeH,
            nativeDpi = safeDpi,
            currentDpi = safeDpi,
            isScaled = false,
            activePresetName = "Native Resolution",
            lastExecutionLog = "Initialized native display at ${safeW}x${safeH} (${safeDpi} DPI)"
        )
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
                name = "Native Resolution",
                width = nativeW,
                height = nativeH,
                densityDpi = nativeDpi,
                scaleFactor = 1.0f,
                gpuWorkloadPercent = 100,
                description = "Standard display resolution. 100% sharp, standard GPU fragment workload.",
                isRecommendedForLowEnd = false
            ),
            makePreset(
                scale = 0.83f,
                name = "900p HD+ Enhanced",
                desc = "Reduces GPU pixel fill workload by ~31% with virtually unnoticeable loss in sharpness.",
                lowEndRec = false
            ),
            makePreset(
                scale = 0.67f,
                name = "720p HD Balanced (Recommended)",
                desc = "Cuts GPU pixel fill workload by ~55%. Recommended for sustained FPS and lower device thermals.",
                lowEndRec = true
            ),
            makePreset(
                scale = 0.50f,
                name = "540p Potato Visual Extreme",
                desc = "75% GPU fillrate reduction. Best for maximum FPS boost in intense combat or crowded servers.",
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

    suspend fun applyPreset(preset: ResolutionPreset): CommandExecutionResult {
        _state.value = _state.value.copy(isApplying = true, lastError = null)

        val isNative = preset.scaleFactor >= 0.98f
        val result = if (isNative) {
            ShizukuManager.resetScreenResolution()
        } else {
            ShizukuManager.setScreenResolution(preset.width, preset.height, preset.densityDpi)
        }

        if (result.success) {
            _state.value = _state.value.copy(
                currentWidth = preset.width,
                currentHeight = preset.height,
                currentDpi = preset.densityDpi,
                isScaled = !isNative,
                activePresetName = preset.name,
                isApplying = false,
                isAwaitingConfirmation = !isNative,
                revertTimerSeconds = if (!isNative) 15 else 0,
                lastExecutionLog = "Applied ${preset.name} (${preset.width}x${preset.height} @ ${preset.densityDpi} DPI)",
                lastError = null
            )

            if (!isNative) {
                startRevertCountdown()
            } else {
                cancelRevertCountdown()
            }
        } else {
            val fallbackAdb = generateAdbCommand(preset.width, preset.height, preset.densityDpi)
            val errMessage = if (!ShizukuManager.state.value.isRunning) {
                "Shizuku service not running. Run via ADB:\n$fallbackAdb"
            } else if (!ShizukuManager.state.value.isPermissionGranted) {
                "Shizuku permission not granted. Authorize Blox Booster in Shizuku or run via ADB:\n$fallbackAdb"
            } else {
                "Command failed (exit ${result.exitCode}): ${result.stderr.ifBlank { result.stdout }}"
            }

            _state.value = _state.value.copy(
                isApplying = false,
                lastError = errMessage,
                lastExecutionLog = "Failed: ${result.command}"
            )
        }

        return result
    }

    fun confirmCurrentResolution() {
        cancelRevertCountdown()
        _state.value = _state.value.copy(
            isAwaitingConfirmation = false,
            revertTimerSeconds = 0,
            lastExecutionLog = "Resolution confirmed: ${_state.value.activePresetName}"
        )
    }

    suspend fun resetToNative(): CommandExecutionResult {
        cancelRevertCountdown()
        _state.value = _state.value.copy(isApplying = true)

        val result = ShizukuManager.resetScreenResolution()
        val curr = _state.value

        if (result.success) {
            _state.value = curr.copy(
                currentWidth = curr.nativeWidth,
                currentHeight = curr.nativeHeight,
                currentDpi = curr.nativeDpi,
                isScaled = false,
                activePresetName = "Native Resolution",
                isAwaitingConfirmation = false,
                revertTimerSeconds = 0,
                isApplying = false,
                lastExecutionLog = "Display restored to native ${curr.nativeWidth}x${curr.nativeHeight} (${curr.nativeDpi} DPI)",
                lastError = null
            )
        } else {
            val fallbackAdb = generateResetAdbCommand()
            val errMessage = if (!ShizukuManager.state.value.isRunning) {
                "Shizuku not active. Restore via ADB:\n$fallbackAdb"
            } else {
                "Failed to reset: ${result.stderr.ifBlank { result.stdout }}"
            }
            _state.value = curr.copy(
                isApplying = false,
                lastError = errMessage
            )
        }

        return result
    }

    private fun startRevertCountdown() {
        revertJob?.cancel()
        revertJob = scope.launch {
            for (sec in 15 downTo 1) {
                _state.value = _state.value.copy(revertTimerSeconds = sec)
                delay(1000)
            }
            // Auto revert if user didn't confirm
            if (_state.value.isAwaitingConfirmation) {
                resetToNative()
                _state.value = _state.value.copy(
                    lastExecutionLog = "Auto-reverted to native resolution (timeout expired for safety)."
                )
            }
        }
    }

    private fun cancelRevertCountdown() {
        revertJob?.cancel()
        revertJob = null
    }
}

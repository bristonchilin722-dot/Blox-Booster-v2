package com.example.ui.viewmodel

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GameProfile
import com.example.engine.PotatoVisualConfig
import com.example.engine.PotatoVisualEngine
import com.example.service.VisualOverlayService
import com.example.util.CommandExecutionResult
import com.example.util.DeviceHardwareInfo
import com.example.util.DeviceSpec
import com.example.util.FpsMetrics
import com.example.util.PerformanceMonitor
import com.example.util.ResolutionManager
import com.example.util.ResolutionPreset
import com.example.util.ResolutionState
import com.example.util.RobloxLauncher
import com.example.util.ShizukuManager
import com.example.util.ShizukuServiceStatus
import com.example.util.ShizukuState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BloxBoosterViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication()
    private val db = AppDatabase.getInstance(context)
    private val profileDao = db.profileDao()

    private val perfMonitor = PerformanceMonitor(context)

    val visualConfig: StateFlow<PotatoVisualConfig> = PotatoVisualEngine.config
    val fpsMetrics: StateFlow<FpsMetrics> = perfMonitor.metrics
    val resolutionState: StateFlow<ResolutionState> = ResolutionManager.state
    val shizukuState: StateFlow<ShizukuState> = ShizukuManager.state

    private val _deviceSpec = MutableStateFlow(DeviceHardwareInfo.getSpecs(context))
    val deviceSpec: StateFlow<DeviceSpec> = _deviceSpec.asStateFlow()

    private val _isRobloxInstalled = MutableStateFlow(RobloxLauncher.isRobloxInstalled(context))
    val isRobloxInstalled: StateFlow<Boolean> = _isRobloxInstalled.asStateFlow()

    private val _robloxVersion = MutableStateFlow(RobloxLauncher.getRobloxVersion(context))
    val robloxVersion: StateFlow<String?> = _robloxVersion.asStateFlow()

    private val _isOverlayActive = MutableStateFlow(false)
    val isOverlayActive: StateFlow<Boolean> = _isOverlayActive.asStateFlow()

    private val _hasOverlayPermission = MutableStateFlow(Settings.canDrawOverlays(context))
    val hasOverlayPermission: StateFlow<Boolean> = _hasOverlayPermission.asStateFlow()

    private val _actionFeedback = MutableStateFlow<String?>(null)
    val actionFeedback: StateFlow<String?> = _actionFeedback.asStateFlow()

    val savedProfiles: StateFlow<List<GameProfile>> = profileDao.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        perfMonitor.start()
        val spec = _deviceSpec.value
        ResolutionManager.init(spec.screenWidth, spec.screenHeight, spec.screenDpi)
        ShizukuManager.init(context)
    }

    override fun onCleared() {
        super.onCleared()
        perfMonitor.stop()
    }

    fun refreshState() {
        _deviceSpec.value = DeviceHardwareInfo.getSpecs(context)
        _isRobloxInstalled.value = RobloxLauncher.isRobloxInstalled(context)
        _robloxVersion.value = RobloxLauncher.getRobloxVersion(context)
        _hasOverlayPermission.value = Settings.canDrawOverlays(context)
        ShizukuManager.checkStatus(context)
    }

    fun requestShizukuPermission() {
        val initiated = ShizukuManager.requestPermission()
        if (!initiated) {
            _actionFeedback.value = ShizukuManager.state.value.lastError ?: "Cannot request Shizuku permission."
        }
    }

    fun openShizukuApp() {
        ShizukuManager.openShizukuApp(context)
    }

    fun launchRoblox() {
        RobloxLauncher.launchRoblox(context)
    }

    fun togglePotatoVisualMode(enable: Boolean? = null) {
        val next = enable ?: !visualConfig.value.potatoModeActive
        PotatoVisualEngine.togglePotatoVisualMode(next)

        viewModelScope.launch {
            if (next) {
                // If Shizuku is authorized, automatically apply balanced 720p resolution for real GPU fillrate reduction
                if (ShizukuManager.state.value.status == ShizukuServiceStatus.AUTHORIZED) {
                    val spec = _deviceSpec.value
                    val presets = ResolutionManager.getPresets(spec.screenWidth, spec.screenHeight, spec.screenDpi)
                    val recPreset = presets.find { it.scaleFactor in 0.65f..0.75f } ?: presets.lastOrNull()
                    if (recPreset != null) {
                        val res = ResolutionManager.applyPreset(recPreset)
                        if (res.success) {
                            _actionFeedback.value = "Potato Mode Active: Applied ${recPreset.name} & high-contrast visual filters."
                        } else {
                            _actionFeedback.value = "Potato Visuals applied. Resolution change failed: ${res.stderr}"
                        }
                    }
                } else {
                    _actionFeedback.value = "Potato Visual Filters Active. (Shizuku not active for OS resolution downscaling)."
                }
                cleanMemory()
            } else {
                // Restore native resolution if it was scaled
                if (resolutionState.value.isScaled) {
                    val res = ResolutionManager.resetToNative()
                    if (res.success) {
                        _actionFeedback.value = "Potato Mode Deactivated: Restored native resolution and default color grading."
                    } else {
                        _actionFeedback.value = "Potato Mode Deactivated. (Resolution reset via ADB: ${ResolutionManager.generateResetAdbCommand()})"
                    }
                } else {
                    _actionFeedback.value = "Potato Mode Deactivated: Restored default color grading."
                }
            }
        }
    }

    fun togglePerformanceMode(enable: Boolean? = null) {
        PotatoVisualEngine.togglePerformanceMode(enable)
        if (visualConfig.value.performanceModeActive) {
            cleanMemory()
        }
    }

    fun toggleVisualMode(enable: Boolean? = null) {
        PotatoVisualEngine.toggleVisualMode(enable)
    }

    fun updateSliders(saturation: Float, contrast: Float, brightness: Float, gamma: Float) {
        PotatoVisualEngine.updateSliders(saturation, contrast, brightness, gamma)
    }

    fun applyResolutionPreset(preset: ResolutionPreset) {
        viewModelScope.launch {
            val result = ResolutionManager.applyPreset(preset)
            if (result.success) {
                _actionFeedback.value = "Applied ${preset.name} successfully."
            } else {
                _actionFeedback.value = "Resolution change failed (exit ${result.exitCode}): ${result.stderr.ifBlank { result.stdout }}"
            }
        }
    }

    fun confirmResolution() {
        ResolutionManager.confirmCurrentResolution()
        _actionFeedback.value = "Resolution confirmed: ${resolutionState.value.activePresetName}"
    }

    fun resetResolution() {
        viewModelScope.launch {
            val result = ResolutionManager.resetToNative()
            if (result.success) {
                _actionFeedback.value = "Display resolution restored to native."
            } else {
                _actionFeedback.value = "Failed to reset resolution: ${result.stderr}"
            }
        }
    }

    fun restoreAllSettings() {
        viewModelScope.launch {
            // 1. Reset screen resolution
            val resResult = ResolutionManager.resetToNative()
            // 2. Reset visual engine
            PotatoVisualEngine.restoreDefaults()
            // 3. Stop overlay service
            if (_isOverlayActive.value) {
                VisualOverlayService.stop(context)
                _isOverlayActive.value = false
            }
            // 4. Memory clean
            cleanMemory()

            val msg = if (resResult.success) {
                "All settings restored to native defaults."
            } else {
                "Visual settings restored to default. Resolution reset: ${resResult.stderr.ifBlank { "Check ADB command." }}"
            }
            _actionFeedback.value = msg
        }
    }

    fun cleanMemory() {
        try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            actManager?.killBackgroundProcesses(context.packageName)
            System.gc()
        } catch (_: Throwable) {}
    }

    fun clearFeedback() {
        _actionFeedback.value = null
    }

    fun toggleOverlayService() {
        if (!Settings.canDrawOverlays(context)) {
            _hasOverlayPermission.value = false
            return
        }
        val next = !_isOverlayActive.value
        _isOverlayActive.value = next
        if (next) {
            VisualOverlayService.start(context)
        } else {
            VisualOverlayService.stop(context)
        }
    }

    fun applyProfile(profile: GameProfile) {
        viewModelScope.launch {
            PotatoVisualEngine.togglePotatoVisualMode(profile.potatoVisualEnabled)
            PotatoVisualEngine.togglePerformanceMode(profile.performanceModeEnabled)
            PotatoVisualEngine.updateSliders(
                saturation = profile.saturationBoost,
                contrast = profile.contrastBoost,
                brightness = 1.05f,
                gamma = 1.15f
            )
            val spec = _deviceSpec.value
            val presets = ResolutionManager.getPresets(spec.screenWidth, spec.screenHeight, spec.screenDpi)
            val matchedPreset = presets.minByOrNull { kotlin.math.abs(it.scaleFactor - profile.resolutionScale) }
            if (matchedPreset != null) {
                ResolutionManager.applyPreset(matchedPreset)
            }
            _actionFeedback.value = "Profile '${profile.name}' applied."
        }
    }

    fun createCustomProfile(name: String, gameTitle: String, description: String) {
        viewModelScope.launch {
            val cfg = visualConfig.value
            val res = resolutionState.value
            val scale = (res.currentWidth.toFloat() / res.nativeWidth.toFloat()).coerceIn(0.4f, 1.0f)
            val profile = GameProfile(
                name = name,
                gameTitle = gameTitle,
                resolutionScale = scale,
                potatoVisualEnabled = cfg.potatoModeActive,
                performanceModeEnabled = cfg.performanceModeActive,
                saturationBoost = cfg.saturation,
                contrastBoost = cfg.contrast,
                targetFps = 60,
                description = description
            )
            profileDao.insertProfile(profile)
            _actionFeedback.value = "Custom profile '$name' created."
        }
    }

    fun deleteProfile(profile: GameProfile) {
        viewModelScope.launch {
            profileDao.deleteProfile(profile)
            _actionFeedback.value = "Profile '${profile.name}' deleted."
        }
    }
}

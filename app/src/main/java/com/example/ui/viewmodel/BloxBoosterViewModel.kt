package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GameProfile
import com.example.engine.PotatoVisualConfig
import com.example.engine.PotatoVisualEngine
import com.example.service.VisualOverlayService
import com.example.util.DeviceHardwareInfo
import com.example.util.DeviceSpec
import com.example.util.FpsMetrics
import com.example.util.PerformanceMonitor
import com.example.util.ResolutionManager
import com.example.util.ResolutionPreset
import com.example.util.ResolutionState
import com.example.util.RobloxLauncher
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

    val savedProfiles: StateFlow<List<GameProfile>> = profileDao.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        perfMonitor.start()
        val spec = _deviceSpec.value
        ResolutionManager.init(spec.screenWidth, spec.screenHeight, spec.screenDpi)
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
    }

    fun launchRoblox() {
        RobloxLauncher.launchRoblox(context)
    }

    fun togglePotatoVisualMode(enable: Boolean? = null) {
        PotatoVisualEngine.togglePotatoVisualMode(enable)
    }

    fun togglePerformanceMode(enable: Boolean? = null) {
        PotatoVisualEngine.togglePerformanceMode(enable)
    }

    fun toggleVisualMode(enable: Boolean? = null) {
        PotatoVisualEngine.toggleVisualMode(enable)
    }

    fun updateSliders(saturation: Float, contrast: Float, brightness: Float, gamma: Float) {
        PotatoVisualEngine.updateSliders(saturation, contrast, brightness, gamma)
    }

    fun applyResolutionPreset(preset: ResolutionPreset) {
        ResolutionManager.applyPresetSimulation(preset)
    }

    fun resetResolution() {
        ResolutionManager.resetToNative()
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
        PotatoVisualEngine.togglePotatoVisualMode(profile.potatoVisualEnabled)
        PotatoVisualEngine.togglePerformanceMode(profile.performanceModeEnabled)
        PotatoVisualEngine.updateSliders(
            saturation = profile.saturationBoost,
            contrast = profile.contrastBoost,
            brightness = 1.05f,
            gamma = 1.10f
        )
        val spec = _deviceSpec.value
        val presets = ResolutionManager.getPresets(spec.screenWidth, spec.screenHeight, spec.screenDpi)
        val matchedPreset = presets.minByOrNull { kotlin.math.abs(it.scaleFactor - profile.resolutionScale) }
        if (matchedPreset != null) {
            ResolutionManager.applyPresetSimulation(matchedPreset)
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
        }
    }

    fun deleteProfile(profile: GameProfile) {
        viewModelScope.launch {
            profileDao.deleteProfile(profile)
        }
    }
}

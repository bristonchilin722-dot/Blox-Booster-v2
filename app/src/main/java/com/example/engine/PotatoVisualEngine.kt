package com.example.engine

import android.graphics.ColorMatrix
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PotatoVisualConfig(
    val potatoModeActive: Boolean = false,
    val performanceModeActive: Boolean = false,
    val visualModeActive: Boolean = false,
    val saturation: Float = 1.35f,       // 1.0 = normal, 1.35 = vivid gaming
    val contrast: Float = 1.25f,         // 1.0 = normal, 1.25 = punchy blacks
    val brightness: Float = 1.05f,       // 1.0 = normal, 1.05 = lifted shadows
    val gammaLift: Float = 1.10f,        // lifts dark zones for competitive games
    val edgeClarity: Boolean = true,     // micro-contrast enhancement
    val lowLatencySync: Boolean = true,  // reduce frame queue buffer
    val memorySaver: Boolean = true,     // active heap compaction
    val activePreset: String = "Potato Maximum Graphics"
)

object PotatoVisualEngine {

    private val _config = MutableStateFlow(PotatoVisualConfig())
    val config: StateFlow<PotatoVisualConfig> = _config.asStateFlow()

    fun togglePotatoVisualMode(enable: Boolean? = null) {
        val next = enable ?: !_config.value.potatoModeActive
        _config.value = _config.value.copy(
            potatoModeActive = next,
            // When Potato Visual is enabled, it sets optimized high-clarity settings
            saturation = if (next) 1.40f else 1.0f,
            contrast = if (next) 1.28f else 1.0f,
            brightness = if (next) 1.06f else 1.0f,
            edgeClarity = next,
            activePreset = if (next) "Potato Max Visuals" else "Standard Default"
        )
    }

    fun togglePerformanceMode(enable: Boolean? = null) {
        val next = enable ?: !_config.value.performanceModeActive
        _config.value = _config.value.copy(
            performanceModeActive = next,
            lowLatencySync = next,
            memorySaver = next
        )
    }

    fun toggleVisualMode(enable: Boolean? = null) {
        val next = enable ?: !_config.value.visualModeActive
        _config.value = _config.value.copy(
            visualModeActive = next,
            saturation = if (next) 1.50f else _config.value.saturation,
            contrast = if (next) 1.32f else _config.value.contrast
        )
    }

    fun updateSliders(saturation: Float, contrast: Float, brightness: Float, gamma: Float) {
        _config.value = _config.value.copy(
            saturation = saturation,
            contrast = contrast,
            brightness = brightness,
            gammaLift = gamma,
            activePreset = "Custom Visuals"
        )
    }

    /**
     * Builds a real Android ColorMatrix that can be applied to an overlay or image filter.
     * Combines contrast, saturation, and brightness into a hardware-accelerated 4x5 color matrix.
     */
    fun buildColorMatrix(cfg: PotatoVisualConfig = _config.value): ColorMatrix {
        val matrix = ColorMatrix()

        // 1. Saturation
        matrix.setSaturation(cfg.saturation)

        // 2. Contrast & Brightness adjustment
        val c = cfg.contrast
        val b = (cfg.brightness - 1.0f) * 255f
        val translate = (-0.5f * c + 0.5f) * 255f + b

        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                c, 0f, 0f, 0f, translate,
                0f, c, 0f, 0f, translate,
                0f, 0f, c, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )

        matrix.postConcat(contrastMatrix)
        return matrix
    }
}

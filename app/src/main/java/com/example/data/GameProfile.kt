package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_profiles")
data class GameProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val gameTitle: String,
    val resolutionScale: Float, // 1.0 = native, 0.75 = 900p, 0.67 = 720p, 0.5 = 540p
    val potatoVisualEnabled: Boolean,
    val performanceModeEnabled: Boolean,
    val saturationBoost: Float, // 1.0f = normal, 1.35f = vivid
    val contrastBoost: Float,
    val targetFps: Int,
    val description: String,
    val isDefault: Boolean = false
)

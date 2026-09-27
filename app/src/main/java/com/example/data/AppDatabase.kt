package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [GameProfile::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "blox_booster.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getInstance(context).profileDao()
                            dao.insertAll(DEFAULT_PROFILES)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private val DEFAULT_PROFILES = listOf(
            GameProfile(
                name = "Poco Extreme Potato",
                gameTitle = "Blox Fruits / Heavy PvP",
                resolutionScale = 0.5f,
                potatoVisualEnabled = true,
                performanceModeEnabled = true,
                saturationBoost = 1.35f,
                contrastBoost = 1.25f,
                targetFps = 60,
                description = "Maximum fillrate relief for budget chips (Helio G36/G85, Unisoc). Renders at 540p equivalent with rich vibrant HDR color compensation.",
                isDefault = true
            ),
            GameProfile(
                name = "Competitive Clarity",
                gameTitle = "Arsenal / Rivals FPS",
                resolutionScale = 0.67f,
                potatoVisualEnabled = true,
                performanceModeEnabled = true,
                saturationBoost = 1.20f,
                contrastBoost = 1.40f,
                targetFps = 60,
                description = "High contrast black-equalizer for competitive visibility with 720p scaling for responsive touch input latency.",
                isDefault = false
            ),
            GameProfile(
                name = "Balanced Vibrant",
                gameTitle = "Brookhaven / Adopt Me / RP",
                resolutionScale = 0.75f,
                potatoVisualEnabled = true,
                performanceModeEnabled = false,
                saturationBoost = 1.45f,
                contrastBoost = 1.15f,
                targetFps = 45,
                description = "Looks like ultra graphics on low settings by enriching washed out colors and boosting ambient saturation without GPU strain.",
                isDefault = false
            ),
            GameProfile(
                name = "Dungeon Raid Potato",
                gameTitle = "Deepwoken / Dungeon Quest",
                resolutionScale = 0.5f,
                potatoVisualEnabled = true,
                performanceModeEnabled = true,
                saturationBoost = 1.30f,
                contrastBoost = 1.30f,
                targetFps = 60,
                description = "Heavy particle scene saver. Drastically reduces pixel fill overhead during boss abilities while maintaining sharp player visibility.",
                isDefault = false
            )
        )
    }
}

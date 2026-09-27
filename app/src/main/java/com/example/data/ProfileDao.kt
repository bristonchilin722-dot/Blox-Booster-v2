package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM game_profiles ORDER BY isDefault DESC, id DESC")
    fun getAllProfiles(): Flow<List<GameProfile>>

    @Query("SELECT * FROM game_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): GameProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: GameProfile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(profiles: List<GameProfile>)

    @Update
    suspend fun updateProfile(profile: GameProfile)

    @Delete
    suspend fun deleteProfile(profile: GameProfile)

    @Query("SELECT COUNT(*) FROM game_profiles")
    suspend fun getProfileCount(): Int
}

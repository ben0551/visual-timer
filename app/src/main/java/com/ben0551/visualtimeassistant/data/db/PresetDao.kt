package com.ben0551.visualtimeassistant.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ben0551.visualtimeassistant.data.entity.PresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preset: PresetEntity): Long

    @Update
    suspend fun update(preset: PresetEntity)

    @Delete
    suspend fun delete(preset: PresetEntity)

    @Query("SELECT * FROM presets ORDER BY isFavourite DESC, sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<PresetEntity>>

    @Query("SELECT * FROM presets WHERE id = :id")
    suspend fun byId(id: Long): PresetEntity?

    @Query("SELECT COUNT(*) FROM presets")
    suspend fun count(): Int

    @Query("UPDATE presets SET isFavourite = :favourite WHERE id = :id")
    suspend fun setFavourite(id: Long, favourite: Boolean)
}

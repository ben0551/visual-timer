package com.ben0551.visualtimeassistant.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ben0551.visualtimeassistant.data.entity.TimerSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerSessionDao {

    @Insert
    suspend fun insert(session: TimerSessionEntity): Long

    @Query("SELECT * FROM timer_sessions ORDER BY startTime DESC")
    fun observeAll(): Flow<List<TimerSessionEntity>>

    @Query("SELECT * FROM timer_sessions WHERE startTime >= :since ORDER BY startTime DESC")
    suspend fun sessionsSince(since: Long): List<TimerSessionEntity>

    @Query("DELETE FROM timer_sessions")
    suspend fun deleteAll()
}

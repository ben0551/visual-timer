package com.ben0551.visualtimeassistant.data.repo

import com.ben0551.visualtimeassistant.data.db.TimerSessionDao
import com.ben0551.visualtimeassistant.data.toDomain
import com.ben0551.visualtimeassistant.data.toEntity
import com.ben0551.visualtimeassistant.domain.model.TimerSessionRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerRepository @Inject constructor(
    private val timerSessionDao: TimerSessionDao,
) {
    val sessions: Flow<List<TimerSessionRecord>> =
        timerSessionDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun record(record: TimerSessionRecord) {
        timerSessionDao.insert(record.toEntity())
    }

    suspend fun clearAll() = timerSessionDao.deleteAll()
}

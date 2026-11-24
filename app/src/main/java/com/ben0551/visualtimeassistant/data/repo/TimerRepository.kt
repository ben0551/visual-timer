package com.ben0551.visualtimeassistant.data.repo

import com.ben0551.visualtimeassistant.data.db.TimerSessionDao
import com.ben0551.visualtimeassistant.di.AppCoroutineScope
import com.ben0551.visualtimeassistant.data.toDomain
import com.ben0551.visualtimeassistant.data.toEntity
import com.ben0551.visualtimeassistant.domain.model.TimerSessionRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerRepository @Inject constructor(
    private val timerSessionDao: TimerSessionDao,
    private val appScope: AppCoroutineScope,
) {
    val sessions: Flow<List<TimerSessionRecord>> =
        timerSessionDao.observeAll().map { list -> list.map { it.toDomain() } }

    /** Fire-and-forget on the app scope so the write survives the caller being torn down. */
    fun record(record: TimerSessionRecord) {
        appScope.launch { timerSessionDao.insert(record.toEntity()) }
    }

    suspend fun clearAll() = timerSessionDao.deleteAll()
}

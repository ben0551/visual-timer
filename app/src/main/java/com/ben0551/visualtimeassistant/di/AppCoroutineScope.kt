package com.ben0551.visualtimeassistant.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Process-wide scope for work that must outlive a screen, e.g. writing a finished timer
 * session to Room right as the user navigates away (a ViewModel scope would be cancelled
 * mid-write).
 */
@Singleton
class AppCoroutineScope @Inject constructor() : CoroutineScope {
    override val coroutineContext = SupervisorJob() + Dispatchers.Default
}

package com.example.stringcanvas.domain.repository

import com.example.stringcanvas.data.models.SpeechSettings
import kotlinx.coroutines.flow.Flow

interface SpeechSettingsRepository {
    val settingsFlow: Flow<SpeechSettings>
    suspend fun setRate(rate: Float)
    suspend fun setPause(ms: Int)
}

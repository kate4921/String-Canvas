package com.example.stringcanvas.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.stringcanvas.data.models.SpeechSettings
import com.example.stringcanvas.domain.repository.SpeechSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SpeechSettingsRepositoryImpl(
    private val store: DataStore<Preferences>
) : SpeechSettingsRepository {

    private val RATE_KEY   = floatPreferencesKey("speech_rate")
    private val PAUSE_KEY  = intPreferencesKey("pause_ms")

    override val settingsFlow: Flow<SpeechSettings> = store.data.map { p ->
        SpeechSettings(
            rate     = p[RATE_KEY]  ?: 1.0f,
            pauseMs  = p[PAUSE_KEY] ?: 0
        )
    }

    override suspend fun setRate(rate: Float) {
        store.edit { it[RATE_KEY] = rate }
    }

    override suspend fun setPause(ms: Int) {
        store.edit { it[PAUSE_KEY] = ms }
    }
}

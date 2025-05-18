package com.example.stringcanvas.domain.usecase

import com.example.stringcanvas.domain.repository.SpeechSettingsRepository

class GetSpeechSettingsFlow(private val repo: SpeechSettingsRepository) {
    operator fun invoke() = repo.settingsFlow
}
package com.example.stringcanvas.domain.usecase

import com.example.stringcanvas.domain.repository.SpeechSettingsRepository

class SetSpeechRate(private val repo: SpeechSettingsRepository) {
    suspend operator fun invoke(rate: Float) = repo.setRate(rate)
}
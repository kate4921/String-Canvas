package com.example.stringcanvas.domain.usecase

import com.example.stringcanvas.domain.repository.SpeechSettingsRepository

class SetPauseBetweenSteps(private val repo: SpeechSettingsRepository) {
    suspend operator fun invoke(ms: Int) = repo.setPause(ms)
}
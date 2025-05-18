package com.example.stringcanvas.domain.usecase

import com.example.stringcanvas.domain.models.ThemeOption
import com.example.stringcanvas.domain.repository.ThemeRepository

class SetTheme(private val repo: ThemeRepository) {
    suspend operator fun invoke(option: ThemeOption) = repo.setTheme(option)
}
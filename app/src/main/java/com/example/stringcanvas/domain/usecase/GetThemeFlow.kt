package com.example.stringcanvas.domain.usecase

import com.example.stringcanvas.domain.repository.ThemeRepository

class GetThemeFlow(private val repo: ThemeRepository) {
    operator fun invoke() = repo.themeFlow
}
package com.example.stringcanvas.domain.repository

import com.example.stringcanvas.domain.models.ThemeOption
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    val themeFlow: Flow<ThemeOption>
    suspend fun setTheme(option: ThemeOption)
}
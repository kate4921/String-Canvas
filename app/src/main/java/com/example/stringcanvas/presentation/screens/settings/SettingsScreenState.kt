package com.example.stringcanvas.presentation.screens.settings

import com.example.stringcanvas.domain.models.ThemeOption

data class SettingsScreenState(
    val currentTheme: ThemeOption = ThemeOption.SYSTEM,
    val speechRate  : Float       = 1.0f,   // 0.5 … 2.0
    val pauseMs     : Int         = 0       // 0 … 10 000
)

package com.example.stringcanvas.presentation.screens.settings

import com.example.stringcanvas.domain.models.ThemeOption

sealed interface SettingsScreenEvent {
    /* тема */
    data class ThemeSelected(val option: ThemeOption) : SettingsScreenEvent

    /* речь */
    data class RateChanged(val rate: Float) : SettingsScreenEvent      // ползунок скорости
    data class PauseChanged(val ms: Int)    : SettingsScreenEvent      // ползунок паузы
}

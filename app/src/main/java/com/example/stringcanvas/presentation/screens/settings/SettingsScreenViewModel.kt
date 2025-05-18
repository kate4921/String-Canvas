package com.example.stringcanvas.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stringcanvas.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsScreenViewModel(
    /* Theme */
    getThemeFlow      : GetThemeFlow,
    private val setTheme   : SetTheme,

    /* Speech */
    getSpeechSettings : GetSpeechSettingsFlow,
    private val setRate    : SetSpeechRate,
    private val setPause   : SetPauseBetweenSteps
) : ViewModel() {

    /* ------------ UI-state ------------ */
    private val _state = MutableStateFlow(SettingsScreenState())
    val state: StateFlow<SettingsScreenState> = _state

    init {
        /* Собираем два независимых Flow в единый стейт */
        viewModelScope.launch {
            combine(
                getThemeFlow(),         // Flow<ThemeOption>
                getSpeechSettings()     // Flow<SpeechSettings>
            ) { theme, speech ->
                SettingsScreenState(
                    currentTheme = theme,
                    speechRate   = speech.rate,
                    pauseMs      = speech.pauseMs
                )
            }.collect { _state.value = it }
        }
    }

    /* ------------ события от UI ------------ */
    fun onEvent(event: SettingsScreenEvent) = when (event) {
        is SettingsScreenEvent.ThemeSelected -> viewModelScope.launch {
            setTheme(event.option)
        }
        is SettingsScreenEvent.RateChanged -> viewModelScope.launch {
            setRate(event.rate)
        }
        is SettingsScreenEvent.PauseChanged -> viewModelScope.launch {
            setPause(event.ms)
        }
    }
}

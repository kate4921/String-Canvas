package com.example.stringcanvas.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stringcanvas.di.SettingsScreenUseCases


class SettingsScreenViewModelFactory(
    private val uc: SettingsScreenUseCases
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SettingsScreenViewModel(
            getThemeFlow      = uc.getTheme,
            setTheme          = uc.setTheme,
            getSpeechSettings = uc.getSpeech,
            setRate           = uc.setRate,
            setPause          = uc.setPause
        ) as T
}




package com.example.stringcanvas.presentation.screens.savedInstructions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stringcanvas.di.SavedInstructionsUseCases

class SavedInstructionsScreenViewModelFactory(
    private val useCases: SavedInstructionsUseCases
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SavedInstructionsScreenViewModel(
            useCases.get,
            useCases.delete,
            useCases.getPdf,
            useCases.updateName
        ) as T
    }
}
package com.example.stringcanvas.presentation.screens.instruction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stringcanvas.di.InstructionScreenDeps


class InstructionScreenViewModelFactory(
    private val deps: InstructionScreenDeps
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        InstructionScreenViewModel(
            deps.useCases.get,
            deps.useCases.save,
            deps.useCases.delete,
            deps.useCases.playInstruction,
            deps.ttsEvents,
            deps.useCases.workWImage,
            deps.useCases.renameInstruction,
            deps.useCases.downloadPdf,
            deps.useCases.getSpeechSettings            // ← передали
        ) as T
}

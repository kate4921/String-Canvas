package com.example.stringcanvas.presentation.screens.savedInstructions

import com.example.stringcanvas.domain.models.Instruction

sealed class SavedInstructionsScreenEvent {
    object LoadSavedInstructions : SavedInstructionsScreenEvent()

    // Удаление
    data class RequestDelete(val instructionId: Long) : SavedInstructionsScreenEvent()
    data class ConfirmDelete(val instructionId: Long) : SavedInstructionsScreenEvent()

    // PDF
    data class RequestGeneratePdf(val instruction: Instruction) : SavedInstructionsScreenEvent()

    // ★ NEW ────────────────  Переименование  ────────────────
    data class RequestRename(val instruction: Instruction) : SavedInstructionsScreenEvent()
    data class ConfirmRename(val instructionId: Long, val newName: String) : SavedInstructionsScreenEvent()

    data class SearchQueryChanged(val query: String) : SavedInstructionsScreenEvent()   // ★ NEW

    // Новое событие для выделения инструкции
    data class SelectInstruction(val instructionId: Long?) : SavedInstructionsScreenEvent()
}
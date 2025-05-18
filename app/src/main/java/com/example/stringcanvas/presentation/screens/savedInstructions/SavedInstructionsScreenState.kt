package com.example.stringcanvas.presentation.screens.savedInstructions

import com.example.stringcanvas.domain.models.Instruction

data class SavedInstructionsScreenState(
    val allInstructions: List<Instruction> = emptyList(),   // исходный список
    val instructions: List<Instruction> = emptyList(),      // отфильтрованный
    val searchQuery: String = "",                           // текст в строке поиска
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedInstructionId: Long? = null // Добавляем выбранный id инструкции

)

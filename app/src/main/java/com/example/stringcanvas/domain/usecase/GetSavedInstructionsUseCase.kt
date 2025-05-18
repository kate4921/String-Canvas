package com.example.stringcanvas.domain.usecase

import com.example.stringcanvas.domain.models.Instruction
import com.example.stringcanvas.domain.repository.InstructionRepository

class GetSavedInstructionsUseCase(
    private val instructionRepository: InstructionRepository
) {
    suspend fun execute(): List<Instruction> {
        // Получаем сохраненные инструкции через репозиторий
        return instructionRepository.getSavedInstructions()
    }
}
package com.example.stringcanvas.domain.usecase

import com.example.stringcanvas.domain.repository.InstructionRepository

class DeleteInstructionByIdUseCase(
    private val instructionRepository: InstructionRepository
) {
    suspend fun execute(instructionId: Long) {
        instructionRepository.deleteInstructionById(instructionId)
    }
}

package com.example.stringcanvas.domain.usecase

import com.example.stringcanvas.domain.models.Instruction
import com.example.stringcanvas.domain.repository.InstructionRepository

class GetInstructionByIdUseCase(private val instructionRepository: InstructionRepository) {

    suspend fun execute(id: Long): Instruction? {
        return instructionRepository.getInstructionById(id)
    }
}
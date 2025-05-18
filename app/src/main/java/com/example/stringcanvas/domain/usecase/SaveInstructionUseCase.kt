package com.example.stringcanvas.domain.usecase

import com.example.stringcanvas.domain.models.Instruction
import com.example.stringcanvas.domain.repository.InstructionRepository

class SaveInstructionUseCase(
    private val instructionRepository: InstructionRepository
) {
    /**
     * Сохраняет инструкцию и возвращает сгенерированный PRIMARY KEY.
     * Если `id` у объекта уже ненулевой, Room сделает REPLACE и вернёт тот же id.
     */
    suspend fun execute(instruction: Instruction): Long {
        return instructionRepository.saveInstruction(instruction)
    }
}


//class SaveInstructionUseCase(
//    private val instructionRepository: InstructionRepository
//) {
//    suspend fun execute(instruction: Instruction) {
//        instructionRepository.saveInstruction(instruction)
//    }
//}

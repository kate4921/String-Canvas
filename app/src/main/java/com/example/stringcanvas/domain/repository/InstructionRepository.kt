package com.example.stringcanvas.domain.repository

import com.example.stringcanvas.domain.models.Instruction
import com.example.stringcanvas.domain.models.InstructionPreview

/**
 * Интерфейс для управления инструкциями в базе данных.
 * Определяет основные операции CRUD (Create, Read, Update, Delete) для инструкций.
 */
interface InstructionRepository {

    suspend fun saveInstruction(instruction: Instruction): Long

    suspend fun getInstructionById(id: Long): Instruction?

    suspend fun deleteInstructionById(id: Long)

    suspend fun getSavedInstructions(): List<Instruction>

    suspend fun deleteInstruction(id: Long)

    suspend fun deleteInstructions(id: List<Long>)

    suspend fun updateName(id: Long, newName: String)

    suspend fun getInstructionPreviews(): List<InstructionPreview>
}
package com.example.stringcanvas.data.repository

import com.example.stringcanvas.data.database.InstructionDao
import com.example.stringcanvas.data.database.toDomain
import com.example.stringcanvas.data.database.toEntity
import com.example.stringcanvas.domain.models.Instruction
import com.example.stringcanvas.domain.repository.ImageRepository
import com.example.stringcanvas.domain.repository.InstructionRepository


class InstructionRepositoryImpl(
    private val instructionDao: InstructionDao,
    private val imageRepository: ImageRepository
): InstructionRepository {
    // В репозитории добавьте проверку существования
    override suspend fun saveInstruction(instruction: Instruction): Long {
        return if (instruction.id != 0L) {
            // Обновляем существующую
            instructionDao.insertInstruction(instruction.toEntity())
            instruction.id
        } else {
            // Создаём новую
            instructionDao.insertInstruction(instruction.toEntity())
        }
    }

    override suspend fun getInstructionById(id: Long): Instruction? {
        return instructionDao.getInstructionById(id)?.toDomain()
    }

    override suspend fun deleteInstructionById(id: Long) {
        // Получаем инструкцию из базы данных
        val instruction = instructionDao.getInstructionById(id)?.toDomain()

        // Если инструкция существует и у нее есть imageUri, удаляем изображение
        instruction?.imageUri?.let { uri ->
            imageRepository.deleteImage(uri)  // Удаляем изображение из файловой системы
        }

        // Удаляем инструкцию из базы данных
        instructionDao.deleteInstructionById(id)
    }

    override suspend fun getSavedInstructions(): List<Instruction> {
        return instructionDao.getSavedInstructions().map { it.toDomain() }
    }

    override suspend fun deleteInstruction(id: Long) {
        instructionDao.deleteInstructionById(id)
    }

    override suspend fun deleteInstructions(id: List<Long>) {
        TODO("Not yet implemented")
    }

    override suspend fun updateName(id: Long, newName: String) {
        instructionDao.updateInstructionName(id, newName)         // просто делегируем DAO
    }

    override suspend fun getInstructionPreviews() =
        instructionDao.getInstructionPreviews()
}

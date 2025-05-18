package com.example.stringcanvas.domain.usecase

import android.net.Uri
import com.example.stringcanvas.domain.models.Instruction
import com.example.stringcanvas.domain.repository.PdfRepository

class GenerateInstructionPdfUseCase(
    private val pdfRepository: PdfRepository
) {
    suspend operator fun invoke(instruction: Instruction): Result<Uri> {
        // Проверка на валидность данных
        if (instruction.name.isBlank() || instruction.textList.isEmpty()) {
            return Result.failure(IllegalArgumentException("Instruction is invalid"))
        }

        return try {
            // Генерация PDF
            val uri = pdfRepository.generate(instruction)
            Result.success(uri)  // Возвращаем успешный результат с URI
        } catch (e: Exception) {
            // Возвращаем ошибку, если что-то пошло не так
            Result.failure(e)
        }
    }
}
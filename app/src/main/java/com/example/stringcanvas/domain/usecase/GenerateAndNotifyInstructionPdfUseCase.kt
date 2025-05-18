package com.example.stringcanvas.domain.usecase

import android.net.Uri
import com.example.stringcanvas.domain.models.Instruction
import com.example.stringcanvas.domain.repository.PdfRepository
import com.example.stringcanvas.domain.service.PdfNotificationHelper

class GenerateAndNotifyInstructionPdfUseCase(
    private val pdfRepository: PdfRepository,
    private val notifier: PdfNotificationHelper
) {
    suspend operator fun invoke(instruction: Instruction): Result<Uri> {
        // Проверка на валидность данных
        if (instruction.name.isBlank() || instruction.textList.isEmpty()) {
            return Result.failure(IllegalArgumentException("Instruction is invalid"))
        }

        return try {
            // Генерация PDF
            val uri = pdfRepository.generate(instruction)

            // Уведомление пользователя о том, что PDF готов
            notifier.notifyDownloaded(uri, "${instruction.name}.pdf")

            Result.success(uri)  // Возвращаем успешный результат с URI
        } catch (e: Exception) {
            // Возвращаем ошибку, если что-то пошло не так
            Result.failure(e)
        }
    }
}

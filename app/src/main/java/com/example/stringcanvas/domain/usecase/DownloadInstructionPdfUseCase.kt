package com.example.stringcanvas.domain.usecase

import com.example.stringcanvas.domain.models.Instruction
import com.example.stringcanvas.domain.service.PdfNotificationHelper

class DownloadInstructionPdfUseCase(
    private val generatePdf : GenerateInstructionPdfUseCase,
    private val notifier    : PdfNotificationHelper
) {
    suspend operator fun invoke(instr: Instruction): Result<Unit> =
        generatePdf(instr).mapCatching { uri ->
            notifier.notifyDownloaded(uri, "${instr.name}.pdf")
        }
}

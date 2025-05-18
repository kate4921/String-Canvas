package com.example.stringcanvas.domain.usecase

import com.example.stringcanvas.domain.models.InstructionPreview
import com.example.stringcanvas.domain.repository.InstructionRepository

class GetInstructionPreviewsUseCase(
    private val repository: InstructionRepository
) {
    suspend operator fun invoke(): List<InstructionPreview> =
        repository.getInstructionPreviews()
}

package com.example.stringcanvas.data.repository

import android.net.Uri
import com.example.stringcanvas.domain.models.Instruction
import com.example.stringcanvas.domain.repository.PdfRepository
import com.example.stringcanvas.domain.service.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PdfRepositoryImpl(
    private val generator: PdfGenerator
) : PdfRepository {

    /** Генерирует PDF и возвращает Uri, подходящий для шаринга */
    override suspend fun generate(instruction: Instruction): Uri =
        withContext(Dispatchers.IO) {
            generator.generate(instruction)
        }
}

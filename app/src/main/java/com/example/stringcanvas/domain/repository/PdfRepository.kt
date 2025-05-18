package com.example.stringcanvas.domain.repository

import android.net.Uri
import com.example.stringcanvas.domain.models.Instruction

interface PdfRepository {
    suspend fun generate(instruction: Instruction): Uri
}

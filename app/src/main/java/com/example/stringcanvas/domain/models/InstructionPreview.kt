package com.example.stringcanvas.domain.models

import android.net.Uri

data class InstructionPreview(
    val id: Long,
    val name: String,
    val imageUriString: String?      // лёгкая строка, без Uri-конвертера
) {
    val imageUri get() = imageUriString?.let(Uri::parse)   // ленивое преобразование
}

package com.example.stringcanvas.domain.usecase

import android.graphics.Bitmap
import android.net.Uri
import com.example.stringcanvas.domain.repository.ImageRepository

class LoadBitmapFromUriUseCase(private val imageRepository: ImageRepository) {
    suspend fun execute(uri: Uri): Bitmap? {
        return imageRepository.loadBitmapFromUri(uri)
    }
}
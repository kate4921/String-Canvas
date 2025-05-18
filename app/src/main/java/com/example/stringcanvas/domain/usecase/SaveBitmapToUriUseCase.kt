package com.example.stringcanvas.domain.usecase

import android.graphics.Bitmap
import android.net.Uri
import com.example.stringcanvas.domain.repository.ImageRepository


class SaveBitmapToUriUseCase(private val imageRepository: ImageRepository) {
    suspend fun execute(bitmap: Bitmap, imageName: String): Uri? {
        return imageRepository.saveBitmapToUri(bitmap, imageName)
    }
}
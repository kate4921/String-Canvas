package com.example.stringcanvas.domain.usecase

import android.graphics.Bitmap
import com.example.stringcanvas.domain.service.InstructionGenerator
import com.example.stringcanvas.domain.models.CanvasSize
import com.example.stringcanvas.domain.models.Instruction2
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class GenerateInstructionsUseCase(
    private val instructionGenerator: InstructionGenerator
) {
    suspend fun execute(bitmap: Bitmap, nails: Int, threads: Int): Pair<Instruction2, Instruction2> {
        return coroutineScope {
            val size1 = CanvasSize(lines = 1000, nailsCount = nails)
            val size2 = CanvasSize(lines = 2000, nailsCount = nails)

            val bitmapCopy1 = getSafeBitmap(bitmap) // Копия для первой задачи
            val bitmapCopy2 = getSafeBitmap(bitmap) // Копия для второй задачи

            val deferred1 = async { instructionGenerator.execute(size1, bitmapCopy1) }
            val deferred2 = async { instructionGenerator.execute(size2, bitmapCopy2) }

            // дождёмся обоих
            val result1 = deferred1.await()
            val result2 = deferred2.await()
            result1 to result2
        }
    }

    // Функция для получения безопасной копии Bitmap
    private fun getSafeBitmap(bitmap: Bitmap): Bitmap {
        return if (bitmap.config != Bitmap.Config.ARGB_8888) {
            bitmap.copy(Bitmap.Config.ARGB_8888, true) // Создаём копию в ARGB_8888
        } else {
            bitmap
        }
    }
}

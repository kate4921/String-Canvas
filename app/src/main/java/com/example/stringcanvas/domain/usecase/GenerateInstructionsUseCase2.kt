//package com.example.stringcanvas.domain.usecase
//
//import android.graphics.Bitmap
//import com.example.stringcanvas.domain.models.CanvasSize
//import com.example.stringcanvas.domain.models.Instruction2
//import com.example.stringcanvas.domain.service.InstructionGenerator2
//import com.example.stringcanvas.utils.toSafeArgb
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.async
//import kotlinx.coroutines.coroutineScope
//import org.opencv.android.Utils
//import org.opencv.core.Mat
//import kotlin.math.roundToInt
//import kotlin.math.max
//
//class GenerateInstructionsUseCase2(
//    private val instructionGenerator: InstructionGenerator2
//) {
//
//    suspend fun execute(
//        bitmap: Bitmap,
//        nails : Int,
//        threads: Int
//    ): Pair<Instruction2, Instruction2> = coroutineScope {
//
//        /* ➊ чуть-чуть «безопаснее» Bitmap → Mat */
////        val safeBmp = bitmap.safeARGB(4_096)          // ≤ 4096 по длинной стороне
//        val safeBmp = bitmap.toSafeArgb(4_096)
//        val srcMat  = Mat().also { Utils.bitmapToMat(safeBmp, it) }
//
//        /* ➋ размеры */
//        val previewLines = (threads * 0.05).roundToInt().coerceIn(10, 300)
//        val sizePreview  = CanvasSize(previewLines, nails)
//        val sizeFull     = CanvasSize(threads,      nails)
//
//        try {
//            /* ➌ параллельные задачи */
//            val previewJob = async(Dispatchers.Default) {
//                instructionGenerator.execute(sizePreview, srcMat)     // генератор сам клонирует
//            }
//            val fullJob = async(Dispatchers.Default) {
//                instructionGenerator.execute(sizeFull, srcMat)        // тот же Mat
//            }
//
//            previewJob.await() to fullJob.await()
//        }
//        finally {
//            srcMat.release()     // освобождаем оригинальный Mat
//        }
//    }
//}
//
////class GenerateInstructionsUseCase(
////    private val instructionGenerator: InstructionGenerator2
////) {
////
////    /**
////     * @param bitmap  исходное изображение
////     * @param nails   количество гвоздей
////     * @param threads полный (финальный) счёт нитей
////     * @return пара (preview, full)
////     */
////    suspend fun execute(
////        bitmap: Bitmap,
////        nails: Int,
////        threads: Int
////    ): Pair<Instruction2, Instruction2> = coroutineScope {
////
////        val previewLines = (threads * 0.05).roundToInt().coerceIn(10, 300)
////
////        val sizePreview = CanvasSize(lines = previewLines, nailsCount = nails)
////        val sizeFull    = CanvasSize(lines = threads,      nailsCount = nails)
////
////        val deferredPreview = async(Dispatchers.Default) {
////            instructionGenerator.execute(sizePreview, bitmap)
////        }
////        val deferredFull = async(Dispatchers.Default) {
////            instructionGenerator.execute(sizeFull, bitmap)
////        }
////
////        deferredPreview.await() to deferredFull.await()
////    }
////}
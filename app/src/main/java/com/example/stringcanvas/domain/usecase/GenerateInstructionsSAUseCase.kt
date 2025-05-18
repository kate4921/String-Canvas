//package com.example.stringcanvas.domain.usecase
//
//import android.graphics.Bitmap
//import com.example.stringcanvas.domain.models.Instruction2
//import com.example.stringcanvas.domain.service.StringArtGenerator
//import com.example.stringcanvas.utils.toSafeArgb
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.async
//import kotlinx.coroutines.coroutineScope
//import kotlin.math.roundToInt
//
//class GenerateInstructionsSAUseCase {
//
//    /**
//     * @param bitmap   исходное фото
//     * @param nails    кол-во гвоздиков
//     * @param threads  кол-во нитей в «полной» инструкции
//     * @return         (preview, full)
//     */
//    suspend fun execute(
//        bitmap : Bitmap,
//        nails  : Int,
//        threads: Int
//    ): Pair<Instruction2, Instruction2> = coroutineScope {
//
//        /* 1) подчищаем Bitmap (ARGB + ≤4096 px по длинной стороне) */
//        val safeBmp = bitmap.toSafeArgb(maxSide = 4_096)
//
//        /* 2) «лёгкая» версия берёт ~5 % нитей */
//        val previewLines = (threads * 0.05).roundToInt().coerceIn(10, 300)
//
//        /* 3) готовим два независимых генератора           *
//         *    (иначе будет гонка за одно и то же Mat/Bitmap) */
//        val genPreview = StringArtGenerator(
//            nailsMode  = "border",          // или GRID / RANDOM – на ваш вкус
//            formType   = "circle",          // CIRCLE / RECT / …
//            nailsCount = nails,
//            linesCount = previewLines
//            /* остальные параметры — дефолтные */
//        )
//
//        val genFull = StringArtGenerator(
//            nailsMode  = "border",
//            formType   = "circle",
//            nailsCount = nails,
//            linesCount = threads
//        )
//
//        /* 4) параллельный рендер */
//        val previewJob = async(Dispatchers.Default) {
//            genPreview.execute(safeBmp)
//        }
//        val fullJob = async(Dispatchers.Default) {
//            genFull.execute(safeBmp)
//        }
//
//        previewJob.await() to fullJob.await()
//    }
//}
//

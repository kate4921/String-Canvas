package com.example.stringcanvas.domain.models

import android.graphics.Bitmap
import android.net.Uri

data class Instruction(
    val id: Long = 0,
    val name:String = "Инструкция: ${id}",
    val textList: List<String>,
    val nailsCount: Int = 0,
    val linesCount: Int = 0,
    val imageUri: Uri? = null,
    val bookmark: Int = 0 // Закладка, чтобы возвращаться к моменту остановки
)

/**
 *  steps         – номера гвоздей (1-based) в порядке натягивания нитей
 *  resultBitmap  – итоговая «нитяная» картинка «как будет выглядеть на доске»
 *  previewBitmap – облегчённая (уменьшенная) версия для списка / уведомления.
 *                  По умолчанию = resultBitmap, поэтому старый код не ломается.
 */
data class Instruction2(
    val instructionSteps: MutableList<String>,
    val resultBitmap:     Bitmap,
//    val previewBitmap:    Bitmap = resultBitmap      // 👈 параметр «по умолчанию»
)

//data class Instruction2(
//    val instructionSteps: MutableList<String>,
//    val resultBitmap: Bitmap
//)
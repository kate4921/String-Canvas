package com.example.stringcanvas.presentation.screens.instruction

import android.graphics.Bitmap
import com.example.stringcanvas.domain.models.Instruction

data class InstructionScreenState(
    val instruction : Instruction? = null,
    val isSaved     : Boolean      = true,
    val isPlaying   : Boolean      = false,
    val playIndex   : Int          = 0,
    val speechRate  : Float        = 1.0f,   // скорость
    val pauseMs     : Int          = 0,       // пауза (0-1000 мс)
    /** Картинка, которая лежит ТОЛЬКО в памяти, когда файла уже нет */
    val previewBitmap: Bitmap? = null,
    /** Флаг выполнения операции сохранения/удаления */
    val isOperationInProgress: Boolean = false,
    val isRenaming : Boolean = false,   // режим ввода названия
    val renameText : String  = "",       // текст в TextField
    val isPdfLoading : Boolean = false,   // идёт генерация?
    val isPdfReady   : Boolean = false    // показать иконку ✅
)
package com.example.stringcanvas.presentation.screens.instructionVariant

import com.example.stringcanvas.data.cache.TemporaryInstructionData

data class InstructionVariantScreenState(
    val data1: TemporaryInstructionData? = null, // Информация для первого изображения
    val data2: TemporaryInstructionData? = null, // Информация для второго изображения
    val isLoading: Boolean = false, // Индикатор загрузки
    val error: String? = null // Строка ошибки, если есть
)
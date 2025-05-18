package com.example.stringcanvas.utils

import kotlin.math.roundToInt

//// Параметры
//val imgRadius = 200     // Количество пикселей, на которое радиус изображения изменяется
val initPin = 0         // Начальный штырь для начала процесса нарезки
//val numPins = 200       // Количество штырьков на круглом станке
//val numLines = 1000      // Максимальное количество линий
val minLoop = 3         // Запрещать петли менее чем из minLoop линий
val lineWidth = 3       // Количество пикселей, представляющих ширину нити
val lineWeight = 15     // Вес одной нити в терминах "темноты"

inline fun limitPixel(v: Double): Int =
    v.roundToInt().coerceIn(0, 255)

const val ACTION_GEN_IN_PROGRESS = "sc.gen_progress"
const val ACTION_GEN_DONE        = "sc.gen_done"


object StringConstants {
    const val PDF_GENERATION_ERROR = "Не удалось сгенерировать PDF"
    const val PDF_URI_NULL = "Сгенерированный PDF URI равен null"
    const val UNKNOWN_ERROR = "Неизвестная ошибка"
    const val DELETE_FAILED = "Не удалось удалить инструкцию"
    const val DELETE_SUCCESS = "Инструкция удалена"
    const val SAVE_SUCCESS = "Инструкция сохранена"
    const val LOAD_FAILED = "Не удалось загрузить инструкции"
    const val RENAME_FAILED = "Не удалось переименовать инструкцию"
    const val OPERATION_FAILED = "Ошибка при выполнении операции"
    const val PDF_READY = "PDF готов"
    const val INSTRUCTION_NOT_FOUND = "Инструкция не найдена"
    const val SET_BOOKMARK = "Добавлена закладка на шаг"
    const val RENAME_DIALOG_TITLE = "Переименовать инструкцию"
    const val BITMAP_NOT_FOUND = "Bitmap не найден" // Added new constant for bitmap error
}

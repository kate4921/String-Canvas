package com.example.stringcanvas.presentation.screens.instruction

sealed interface InstructionScreenEvent {

    /** Загрузить инструкцию из БД по id (вызывается при открытии экрана) */
    data class LoadInstruction(val instructionId: Long) : InstructionScreenEvent

    /** Переключить: если инструкция сохранена — удалить, иначе — сохранить */
    object ToggleSaveDelete : InstructionScreenEvent

    /** Поставить / обновить закладку */
    data class SetBookmark(val index: Int) : InstructionScreenEvent

    /** Запуск / остановка озвучки */
    object TogglePlay : InstructionScreenEvent

    data class ChangeRate(val rate: Float) : InstructionScreenEvent

    data class ChangePause(val ms  : Int  ) : InstructionScreenEvent   // ← НОВОЕ

    object SeekBackward10 : InstructionScreenEvent
    object SeekForward10  : InstructionScreenEvent

    object StartRename               : InstructionScreenEvent
    data class ChangeRename(val s: String) : InstructionScreenEvent
    object ConfirmRename             : InstructionScreenEvent
    object CancelRename              : InstructionScreenEvent

    object DownloadPdf : InstructionScreenEvent        // клик по ⬇️
    object PdfReadyShownTooLong : InstructionScreenEvent  // внутренний
}

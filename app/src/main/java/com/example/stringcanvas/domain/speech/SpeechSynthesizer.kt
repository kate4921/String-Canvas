package com.example.stringcanvas.domain.speech

import java.util.UUID

interface SpeechSynthesizer {

    /** Произнести текст (pauseMs - пауза *внутри* текста). */
    fun speak(
        text: String,
        pauseMs: Int = 0,
        utteranceId: String = UUID.randomUUID().toString(),
        onDone: () -> Unit = {}
    )

    /** Добавить тишину в очередь TTS. */
    fun playSilence(
        ms: Int,
        onDone: () -> Unit = {}           // вызовется после паузы
    )

    fun stop()
    fun shutdown()
    fun setRate(rate: Float)

    var onError: ((String) -> Unit)?
}

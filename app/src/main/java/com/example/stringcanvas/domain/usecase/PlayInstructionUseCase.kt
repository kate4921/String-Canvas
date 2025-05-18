package com.example.stringcanvas.domain.usecase

import com.example.stringcanvas.domain.models.Instruction
import com.example.stringcanvas.domain.speech.SpeechSynthesizer

class PlayInstructionUseCase(
    private val tts: SpeechSynthesizer
) {
    /* ─── параметры ─── */
    private var pauseMs = 0            // тишина МЕЖДУ шагами (0-10000)

    fun setPause(ms: Int) {            // из UI: ползунок «Пауза»
        pauseMs = ms.coerceIn(0, 10_000)
    }

    fun setRate(rate: Float) = tts.setRate(rate)

    /* ─── воспроизведение ─── */
    fun start(
        instruction: Instruction,
        startIndex : Int = 0,
        onStep     : (Int) -> Unit,
        onFinish   : () -> Unit
    ) {
        fun play(idx: Int) {
            if (idx !in instruction.textList.indices) {    // конец списка
                onFinish(); return
            }

            onStep(idx)                                    // сообщаем UI

            // 1) произносим сам шаг (без внутренних пауз)
            tts.speak(
                text        = instruction.textList[idx],
                pauseMs     = 0,               // ← пауза ВНУТРИ шага не нужна
                utteranceId = "step_$idx"
            ) {
                // 2) добавляем тишину между этим и следующим шагом
                if (pauseMs > 0) {
                    tts.playSilence(pauseMs) { play(idx + 1) }
                } else {
                    play(idx + 1)
                }
            }
        }
        play(startIndex)
    }

    // PlayInstructionUseCase.kt
    fun restart(
        instruction: Instruction,
        newIndex: Int,
        onStep  : (Int) -> Unit,
        onFinish: () -> Unit
    ) {
        stop()
        start(instruction, newIndex, onStep, onFinish)
    }


    fun stop()     { tts.stop() }
    fun shutdown() { tts.shutdown() }
}
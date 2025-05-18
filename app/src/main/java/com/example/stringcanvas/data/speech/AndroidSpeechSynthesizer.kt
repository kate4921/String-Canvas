package com.example.stringcanvas.data.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.stringcanvas.domain.speech.SpeechSynthesizer
import kotlinx.coroutines.*
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

class AndroidSpeechSynthesizer(
    context: Context,
    coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO
) : SpeechSynthesizer, TextToSpeech.OnInitListener {

    private val scope = CoroutineScope(coroutineContext)
    private val tts   = TextToSpeech(context, this)
    private val doneCallbacks = ConcurrentHashMap<String, () -> Unit>()

    override var onError: ((String) -> Unit)? = null

    /* ------------ init ------------ */
    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            onError?.invoke("Ошибка инициализации TTS (status=$status)")
            return
        }

        // язык ─ сначала ru_RU, потом fallback на en_US
        var res = tts.setLanguage(Locale("ru", "RU"))
        if (res < TextToSpeech.LANG_AVAILABLE) {
            res = tts.setLanguage(Locale.US)
            onError?.invoke(
                "Русский голос не найден, переключаюсь на английский"
            )
        }

        tts.setSpeechRate(0.95f)

        /*  !!!  без этого колбэки onDone не приходят  !!! */
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit
            override fun onError(utteranceId: String?) = Unit
            override fun onDone(utteranceId: String?) {
                utteranceId?.let { doneCallbacks.remove(it)?.invoke() }
            }
        })
    }


    /* ------------ speak ------------ */
    override fun speak(
        text: String,
        pauseMs: Int,
        utteranceId: String,
        onDone: () -> Unit
    ) = enqueueSimple(text, utteranceId, onDone)   // pauseMs теперь не используем

    /** тишина между шагами */
    override fun playSilence(ms: Int, onDone: () -> Unit) {
        if (ms <= 0) { onDone(); return }

        val chunk = 5_000                    // безопасный кусок = 5 s
        var left  = ms
        var lastId = ""

        while (left > 0) {
            val thisChunk = minOf(chunk, left)
            lastId = "silence_${System.nanoTime()}"
            // onDone вешаем только на ПОСЛЕДНЮЮ тишину
            if (left <= chunk) doneCallbacks[lastId] = onDone

            tts.playSilentUtterance(thisChunk.toLong(),
                TextToSpeech.QUEUE_ADD,
                lastId)
            left -= thisChunk
        }
    }


    private fun enqueueSimple(text: String, id: String, onDone: () -> Unit) {
        doneCallbacks[id] = onDone
        scope.launch {
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, id)
        }
    }

    /* ------------ misc ------------ */
    override fun stop() { tts.stop() }
    override fun shutdown() { scope.cancel(); tts.shutdown() }
    override fun setRate(r: Float) { tts.setSpeechRate(r.coerceIn(0.1f, 2f)) }
}
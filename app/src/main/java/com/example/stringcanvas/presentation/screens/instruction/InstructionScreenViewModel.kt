package com.example.stringcanvas.presentation.screens.instruction

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stringcanvas.domain.usecase.*
import com.example.stringcanvas.utils.StringConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class InstructionScreenViewModel(
    private val getInstruction: GetInstructionByIdUseCase,
    private val saveInstruction: SaveInstructionUseCase,
    private val deleteInstruction: DeleteInstructionByIdUseCase,
    private val player: PlayInstructionUseCase,
    private val ttsEvents: SharedFlow<String>,
    private val imageUseCase: ImageUseCase,
    private val renameInstruction: RenameInstructionUseCase,
    private val downloadPdf: GenerateAndNotifyInstructionPdfUseCase,
    private val getSpeechSettings: GetSpeechSettingsFlow
) : ViewModel() {

    /* ---------- UI-state ---------- */
    private val _state = mutableStateOf(InstructionScreenState())
    val state: State<InstructionScreenState> = _state

    /* ---------- Потоки для навигации и сообщений ---------- */
    private val _navigationEvents = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val navigationEvents: SharedFlow<UiEvent> = _navigationEvents.asSharedFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val io = Dispatchers.IO

//    init {
//        viewModelScope.launch {
//            ttsEvents.collect { msg -> emitMessage(msg) }
//        }
//    }

    init {
        /* ----- однократное чтение из DataStore ----- */
        viewModelScope.launch {
            getSpeechSettings()          // Flow<SpeechSettings>
                .first()                 // берём ТОЛЬКО ПЕРВОЕ значение
                .also { (rate, pause) ->
                    _state.value = _state.value.copy(
                        speechRate = rate,
                        pauseMs    = pause
                    )
                    player.setRate(rate)     // сразу применяем к TTS
                    player.setPause(pause)
                }
        }

        /* ----- поток ошибок TTS (как и было) ----- */
        viewModelScope.launch {
            ttsEvents.collect { emitMessage(it) }
        }
    }

    /* ---------- обработка UI-событий ---------- */
    fun onEvent(event: InstructionScreenEvent) {
        when (event) {
            is InstructionScreenEvent.LoadInstruction -> loadInstruction(event.instructionId)
            InstructionScreenEvent.ToggleSaveDelete -> toggleSaveDelete()
            is InstructionScreenEvent.SetBookmark -> setBookmark(event.index)
            InstructionScreenEvent.TogglePlay -> togglePlay()
            is InstructionScreenEvent.ChangeRate -> changeRate(event.rate)
            is InstructionScreenEvent.ChangePause -> changePause(event.ms)
            InstructionScreenEvent.SeekBackward10 -> seek(-10)
            InstructionScreenEvent.SeekForward10  -> seek(+10)

            InstructionScreenEvent.StartRename        -> startRename()
            is InstructionScreenEvent.ChangeRename    -> _state.value = _state.value.copy(renameText = event.s)
            InstructionScreenEvent.CancelRename       -> _state.value = _state.value.copy(isRenaming = false)
            InstructionScreenEvent.ConfirmRename      -> confirmRename()

            InstructionScreenEvent.DownloadPdf          -> downloadPdf()
            InstructionScreenEvent.PdfReadyShownTooLong -> _state.value =
                _state.value.copy(isPdfReady = false)
        }
    }

    /* ---------- скачать pdf ---------- */
    private fun downloadPdf() = viewModelScope.launch {
        val instr = _state.value.instruction ?: return@launch
        _state.value = _state.value.copy(isPdfLoading = true)

        downloadPdf(instr)
            .onSuccess {
                _state.value = _state.value.copy(
                    isPdfLoading = false,
                    isPdfReady   = true
                )
                delay(2_000) // 2 с показываем «✔»
                onEvent(InstructionScreenEvent.PdfReadyShownTooLong)
            }
            .onFailure { e ->
                // Используем строку из констант
                emitMessage(StringConstants.PDF_GENERATION_ERROR)
                _state.value = _state.value.copy(isPdfLoading = false)
            }
    }

    /* ---------- параметры воспроизведения ---------- */
    private fun changeRate(rate: Float) {
        player.setRate(rate)
        _state.value = _state.value.copy(speechRate = rate)
    }

    private fun changePause(ms: Int) {
        val clamped = ms.coerceIn(0, 10_000)
        player.setPause(clamped)                 // этого достаточно
        _state.value = _state.value.copy(pauseMs = clamped)
    }

    /* ---------- Play / Stop ---------- */
    private fun togglePlay() {
        val s = _state.value
        val instr = s.instruction ?: return

        if (s.isPlaying) {
            Log.d("TTS", "STOP pressed")
            player.stop()
            _state.value = s.copy(isPlaying = false)
        } else {
            Log.d("TTS", "PLAY pressed from idx=${s.playIndex}")
            player.start(instr, s.playIndex,
                onStep = { idx ->
                    Log.d("TTS", "VM onStep idx=$idx")
                    _state.value = _state.value.copy(playIndex = idx)
                },
                onFinish = {
                    Log.d("TTS", "VM onFinish")
                    _state.value = _state.value.copy(isPlaying = false)
                }
            )
            _state.value = s.copy(isPlaying = true)
        }
    }

    /* --------- перемотка ---------- */
    private fun seek(delta: Int) {
        val s      = _state.value
        val instr  = s.instruction ?: return
        val maxIdx = instr.textList.lastIndex
        val target = (s.playIndex + delta).coerceIn(0, maxIdx)

        // Обновляем UI-state
        _state.value = s.copy(playIndex = target)

        // Если сейчас идёт воспроизведение – перезапускаем TTS
        if (s.isPlaying) {
            player.restart(
                instruction = instr,
                newIndex    = target,
                onStep      = { idx -> _state.value = _state.value.copy(playIndex = idx) },
                onFinish    = { _state.value = _state.value.copy(isPlaying = false) }
            )
        }
    }

    /* ---------- закладка ---------- */
    private fun setBookmark(index: Int) = viewModelScope.launch {
        val instr = _state.value.instruction ?: return@launch
        val updated = instr.copy(bookmark = index)

        saveInstruction.execute(updated)

        _state.value = _state.value.copy(
            instruction = updated,
            isSaved     = true,
            playIndex   = if (_state.value.isPlaying) _state.value.playIndex else index
        )

        emitMessage("${StringConstants.SET_BOOKMARK} ${index + 1}")
    }

    /* ---------- загрузка ---------- */
    private fun loadInstruction(id: Long) = viewModelScope.launch {
        val instr = getInstruction.execute(id)

        _state.value = _state.value.copy(
            instruction   = instr,
            isSaved       = instr != null,
            previewBitmap = null,
            playIndex     = instr?.bookmark ?: 0   // ← НОВАЯ СТРОКА
        )
    }

    /* ---------- сохранить / удалить ---------- */
    private fun toggleSaveDelete() = viewModelScope.launch {
        doSaveOrDelete()
    }

    private suspend fun doSaveOrDelete() {
        if (_state.value.isOperationInProgress) return

        _state.value = _state.value.copy(isOperationInProgress = true)

        try {
            val current = _state.value.instruction ?: return

            if (_state.value.isSaved) {
                val bmp = withContext(io) {
                    current.imageUri?.let { imageUseCase.loadImage(it) }
                }

                deleteInstruction.execute(current.id)

                _state.value = _state.value.copy(
                    isSaved = false,
                    instruction = current.copy(id = 0, imageUri = null),
                    previewBitmap = bmp
                )
                emitMessage(StringConstants.DELETE_SUCCESS)
            } else {
                val newImageUri = withContext(io) {
                    _state.value.previewBitmap?.let {
                        imageUseCase.saveImage(it, "image_${System.currentTimeMillis()}")
                    }
                }

                val toSave = if (current.id != 0L) {
                    // Если у инструкции уже есть ID - обновляем
                    current.copy(imageUri = newImageUri)
                } else {
                    // Если нет ID - создаём новую
                    current.copy(id = 0, imageUri = newImageUri)
                }

                val newId = saveInstruction.execute(toSave)

                _state.value = _state.value.copy(
                    isSaved = true,
                    instruction = current.copy(id = newId, imageUri = newImageUri),
                    previewBitmap = null
                )
                emitMessage(StringConstants.SAVE_SUCCESS)
            }
        } finally {
            _state.value = _state.value.copy(isOperationInProgress = false)
        }
    }

    /* ------- наименование -------- */
    private fun startRename() {
        val name = _state.value.instruction?.name.orEmpty()
        _state.value = _state.value.copy(isRenaming = true, renameText = name)
    }

    private fun confirmRename() = viewModelScope.launch {
        val s      = _state.value
        val instr  = s.instruction ?: return@launch
        val newName = s.renameText.trim()

        if (newName.isBlank()) {
            emitMessage(StringConstants.RENAME_FAILED)
            return@launch
        }

        renameInstruction(instr.id, newName)          // use-case
        _state.value = s.copy(
            instruction = instr.copy(name = newName),
            isRenaming  = false
        )
        emitMessage(StringConstants.RENAME_DIALOG_TITLE)
    }

    private suspend fun emitMessage(msg: String) {
        _messages.emit(msg)  // Эмитим сообщение
    }

    /* ---------- Навигация ---------- */
    private fun stopPlaying() {
        player.stop()  // Останавливаем озвучку
        _state.value = _state.value.copy(isPlaying = false)  // Обновляем состояние UI
    }

    fun requestExit() = viewModelScope.launch {
        if (_state.value.isSaved) {
            _navigationEvents.emit(UiEvent.NavigateBack)
        } else {
            _navigationEvents.emit(UiEvent.AskSaveBeforeExit)
        }

        // Останавливаем воспроизведение при выходе
        stopPlaying()
    }

    fun saveAndExit() = viewModelScope.launch {
        doSaveOrDelete()
        _navigationEvents.emit(UiEvent.NavigateBack)
        _messages.emit(StringConstants.SAVE_SUCCESS)
    }

    fun exitWithoutSaving() = viewModelScope.launch {
        _state.value = _state.value.copy(previewBitmap = null)
        _navigationEvents.emit(UiEvent.NavigateBack)
    }
}

sealed interface UiEvent {
    object AskSaveBeforeExit : UiEvent                         // Диалог «сохранить?»
    object NavigateBack : UiEvent                              // Дать UI команду уйти
}

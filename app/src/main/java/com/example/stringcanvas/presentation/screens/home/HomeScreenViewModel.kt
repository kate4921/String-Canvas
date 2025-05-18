package com.example.stringcanvas.presentation.screens.home

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stringcanvas.domain.usecase.DecodeCroppedImageUseCase
import com.example.stringcanvas.data.cache.TempInstructionsCache
import com.example.stringcanvas.data.cache.TemporaryInstructionData
import com.example.stringcanvas.domain.service.InstructionGenerationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel домашнего экрана после переноса тяжёлой генерации
 * в [InstructionGenerationService].
 *
 * Задачи:
 *  • загружать bitmap из Cropper’а
 *  • хранить параметры nails / threads
 *  • запускать Foreground-Service и ждать broadcast’а ACTION_DONE
 *  • обновлять UI-state
 */
class HomeScreenViewModel(
    private val decodeCroppedImageUseCase: DecodeCroppedImageUseCase,
    private val appContext: Context               // applicationContext для старта сервиса
) : ViewModel() {

    private val _state = MutableStateFlow(HomeScreenState())
    val state: StateFlow<HomeScreenState> = _state

    /* ----------  public API для UI ---------- */

    fun onEvent(event: HomeScreenEvent) = when (event) {
        is HomeScreenEvent.ImageCropped        -> handleImageCropped(event)
        is HomeScreenEvent.NailsCountChanged   -> _state.update { it.copy(countNails  = event.newCount) }
        is HomeScreenEvent.ThreadCountChanged  -> _state.update { it.copy(countLines  = event.newCount) }
        HomeScreenEvent.GenerateClicked        -> startGenerationService()
    }

    /**
     * Получаем broadcast от сервиса, когда оба набора инструкций готовы.
     */
    fun onGenerationDone(id1: Long, id2: Long) {
        _state.update {
            it.copy(
                isGenerating = false,
                generatedIds = listOf(id1, id2)
            )
        }
    }

    /* ----------  private helpers ---------- */

    private fun handleImageCropped(e: HomeScreenEvent.ImageCropped) {
        viewModelScope.launch(Dispatchers.IO) {
            decodeCroppedImageUseCase.execute(e.croppedUri, e.context)?.let { bmp ->
                _state.update { it.copy(bitmap = bmp) }
            }
        }
    }

    /**
     * Кладёт исходный Bitmap в кэш и запускает [InstructionGenerationService].
     */
    private fun startGenerationService() {
        val bmp = _state.value.bitmap ?: return          // если картинка не выбрана — ничего не делаем

        // Переходим в состояние «генерация запущена»
        _state.update { it.copy(isGenerating = true) }

        // Ключ для временных данных (используем nanoseconds, чтобы не пересекался)
        val cacheKey = System.nanoTime()

        // Сохраняем данные, которые нужны сервису
        TempInstructionsCache.put(
            cacheKey,
            TemporaryInstructionData(
                id         = cacheKey,
                steps      = emptyList(),                // будет заполнено сервисом
                bitmap     = bmp,
                nailsCount = _state.value.countNails,
                linesCount = _state.value.countLines
            )
        )

        // Intent на запуск Foreground-Service
        val i = Intent(appContext, InstructionGenerationService::class.java).apply {
            putExtra("cache_key", cacheKey)
            putExtra("nails",     _state.value.countNails)
            putExtra("threads",   _state.value.countLines)
        }

        /* NB: для API 34+ предварительно запросите разрешение
               android.permission.FOREGROUND_SERVICE_DATA_SYNC */
        ContextCompat.startForegroundService(appContext, i)
    }
}


//
//import com.example.stringcanvas.domain.usecase.DecodeCroppedImageUseCase
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.stringcanvas.data.cache.TempInstructionsCache
//import com.example.stringcanvas.data.cache.TemporaryInstructionData
//import com.example.stringcanvas.domain.models.Instruction2
//import com.example.stringcanvas.domain.usecase.GenerateInstructionsUseCase
//import com.example.stringcanvas.domain.usecase.ShowGenerationNotificationUseCase
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//
//
//class HomeScreenViewModel(
//    private val decodeCroppedImageUseCase: DecodeCroppedImageUseCase,
//    private val generateInstructionsUseCase: GenerateInstructionsUseCase,
//    private val showNotificationUseCase: ShowGenerationNotificationUseCase
//) : ViewModel() {
//
//    private val _state = MutableStateFlow(HomeScreenState())
//    val state: StateFlow<HomeScreenState> = _state.asStateFlow()
//
//    fun onEvent(event: HomeScreenEvent) {
//        when (event) {
//            is HomeScreenEvent.ImageCropped -> handleImageCropped(event)
//            is HomeScreenEvent.NailsCountChanged -> updateNailsCount(event.newCount)
//            is HomeScreenEvent.ThreadCountChanged -> updateThreadCount(event.newCount)
//            HomeScreenEvent.GenerateClicked -> generateInstructionsAsync()
//        }
//    }
//
//    private fun handleImageCropped(event: HomeScreenEvent.ImageCropped) {
//        viewModelScope.launch {
//            val bitmap = decodeCroppedImageUseCase.execute(
//                uri = event.croppedUri,
//                context = event.context
//            )
//            bitmap?.let {
//                _state.value = _state.value.copy(bitmap = it)
//            }
//        }
//    }
//
//    private fun updateNailsCount(newCount: Int) {
//        _state.value = _state.value.copy(countNails = newCount)
//    }
//
//    private fun updateThreadCount(newCount: Int) {
//        _state.value = _state.value.copy(countLines = newCount)
//    }
//
//    private fun generateInstructionsAsync() {
//        val currentBitmap = _state.value.bitmap ?: return
//        _state.value = _state.value.copy(isGenerating = true, generatedIds = emptyList())
//
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
////                val (instruction1, instruction2) = generateInstructionsUseCase.execute(
////                    bitmap = currentBitmap,
////                    nails = _state.value.countNails,
////                    threads = _state.value.countLines
////                )
//                val (instruction1, instruction2) = withContext(Dispatchers.Default) {
//                    generateInstructionsUseCase.execute(
//                        bitmap = currentBitmap,
//                        nails = _state.value.countNails,
//                        threads = _state.value.countLines
//                    )
//                }
//
//                val tempId1 = System.currentTimeMillis()
//                val tempId2 = tempId1 + 1
//
//                TempInstructionsCache.put(tempId1, createTempData(tempId1, instruction1))
//                TempInstructionsCache.put(tempId2, createTempData(tempId2, instruction2))
//
//                withContext(Dispatchers.Main) {
//                    _state.value = _state.value.copy(
//                        isGenerating = false,
//                        generatedIds = listOf(tempId1, tempId2)
//                    )
//                    showNotificationUseCase() // Уведомление о завершении
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    _state.value = _state.value.copy(isGenerating = false)
//                }
//            }
//        }
//    }
//
//    private fun createTempData(
//        id: Long,
//        instruction: Instruction2
//    ): TemporaryInstructionData {
//        return TemporaryInstructionData(
//            id = id,
//            steps = instruction.instructionSteps,
//            bitmap = instruction.resultBitmap,
//            nailsCount = _state.value.countNails,
//            linesCount = _state.value.countLines
//        )
//    }
//}

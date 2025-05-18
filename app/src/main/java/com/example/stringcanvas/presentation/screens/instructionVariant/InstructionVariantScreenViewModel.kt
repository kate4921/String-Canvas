package com.example.stringcanvas.presentation.screens.instructionVariant

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.viewModelScope
import com.example.stringcanvas.data.cache.TempInstructionsCache
import com.example.stringcanvas.domain.models.Instruction
import com.example.stringcanvas.domain.usecase.ClearInstructionsUseCase
import com.example.stringcanvas.domain.usecase.SaveBitmapToUriUseCase
import com.example.stringcanvas.domain.usecase.SaveInstructionUseCase
import com.example.stringcanvas.utils.StringConstants
import kotlinx.coroutines.launch

class InstructionVariantScreenViewModel(
    private val clearInstructionsUseCase: ClearInstructionsUseCase,
    private val saveInstructionUseCase: SaveInstructionUseCase,
    private val saveBitmapToUriUseCase: SaveBitmapToUriUseCase
) : ViewModel() {

    private val _state = mutableStateOf(InstructionVariantScreenState())
    val state: State<InstructionVariantScreenState> = _state

    // Флаг, чтобы попросить UI вернуться назад
    private val _navigateBack = mutableStateOf(false)
    val navigateBack: State<Boolean> = _navigateBack

    // Храним ID инструкции, на которую хотим перейти
    private val _navigateToInstruction = mutableStateOf<Long?>(null)
    val navigateToInstruction: State<Long?> = _navigateToInstruction

    fun onEvent(event: InstructionVariantScreenEvent) {
        when (event) {
            is InstructionVariantScreenEvent.LoadInstructions -> {
                loadInstructions(event.id1, event.id2)
            }

            is InstructionVariantScreenEvent.ImageClicked -> {
                // 1) Удаляем второе изображение из кэша
                clearInstructionsUseCase.execute(listOf(event.otherId))

                // 2) Достаём выбранную инструкцию из стейта
                val chosenData = if (event.chosenId == state.value.data1?.id)
                    state.value.data1
                else
                    state.value.data2

                // 3) Преобразуем Bitmap в Uri
                val bitmap = chosenData?.bitmap
                val imageName = event.chosenId.toString() // Используем ID для уникального имени файла

                if (bitmap != null) {
                    // Сохраняем Bitmap в Uri
                    viewModelScope.launch {
                        val uri = saveBitmapToUriUseCase.execute(bitmap, imageName)

                        // 4) Создаем доменный объект с полученным Uri
                        val instruction = Instruction(
                            id = event.chosenId,
                            textList = chosenData.steps ?: listOf(),
                            nailsCount = chosenData.nailsCount ?: 0,
                            linesCount = chosenData.linesCount ?: 0,
                            imageUri = uri
                        )

                        // 5) Сохраняем инструкцию в базу данных
                        saveInstructionUseCase.execute(instruction)

                        // После сохранения — навигация
                        _navigateToInstruction.value = instruction.id
                    }
                } else {
                    // Если Bitmap пустой, можно обработать ошибку или выполнить альтернативное действие
                    _state.value = _state.value.copy(error = StringConstants.INSTRUCTION_NOT_FOUND)
                }
            }

            is InstructionVariantScreenEvent.BackClicked -> {
                // Очистка кэша (обе инструкции)
                clearInstructionsUseCase.execute(listOf(event.id1, event.id2))
                _navigateBack.value = true
            }
        }
    }

    private fun loadInstructions(id1: Long, id2: Long) {
        _state.value = _state.value.copy(isLoading = true)

        try {
            val data1 = TempInstructionsCache.get(id1)
            val data2 = TempInstructionsCache.get(id2)

            if (data1 == null || data2 == null) {
                throw IllegalStateException(StringConstants.LOAD_FAILED)
            }

            _state.value = _state.value.copy(
                data1 = data1,
                data2 = data2,
                isLoading = false,
                error = null
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = e.message ?: StringConstants.UNKNOWN_ERROR
            )
        }
    }

    // Если хотим сбрасывать флаг после навигации
    fun resetNavigateToInstruction() {
        _navigateToInstruction.value = null
    }
}



//class InstructionVariantScreenViewModel(
//    private val clearInstructionsUseCase: ClearInstructionsUseCase,
//    private val saveInstructionUseCase: SaveInstructionUseCase,
//    private val saveBitmapToUriUseCase: SaveBitmapToUriUseCase
//) : ViewModel() {
//
//    private val _state = mutableStateOf(InstructionVariantScreenState())
//    val state: State<InstructionVariantScreenState> = _state
//
//    // Флаг, чтобы попросить UI вернуться назад
//    private val _navigateBack = mutableStateOf(false)
//    val navigateBack: State<Boolean> = _navigateBack
//
//    // Храним ID инструкции, на которую хотим перейти
//    private val _navigateToInstruction = mutableStateOf<Long?>(null)
//    val navigateToInstruction: State<Long?> = _navigateToInstruction
//
//    fun onEvent(event: InstructionVariantScreenEvent) {
//        when (event) {
//            is InstructionVariantScreenEvent.LoadInstructions -> {
//                loadInstructions(event.id1, event.id2)
//            }
//
//            is InstructionVariantScreenEvent.ImageClicked -> {
//                // 1) Удаляем второе изображение из кэша
//                clearInstructionsUseCase.execute(listOf(event.otherId))
//
//                // 2) Достаём выбранную инструкцию из стейта
//                val chosenData = if (event.chosenId == state.value.data1?.id)
//                    state.value.data1
//                else
//                    state.value.data2
//
//                // 3) Преобразуем Bitmap в Uri
//                val bitmap = chosenData?.bitmap
//                val imageName = event.chosenId.toString() // Используем ID для уникального имени файла
//
//                if (bitmap != null) {
//                    // Сохраняем Bitmap в Uri
//                    viewModelScope.launch {
//                        val uri = saveBitmapToUriUseCase.execute(bitmap, imageName)
//
//                        // 4) Создаем доменный объект с полученным Uri
//                        val instruction = Instruction(
//                            id = event.chosenId,
//                            textList = chosenData.steps ?: listOf(),
//                            nailsCount = chosenData.nailsCount ?: 0,
//                            linesCount = chosenData.linesCount ?: 0,
//                            imageUri = uri//,  // Используем полученный Uri
//                            //bookmark = chosenData?.bookmark ?: 0
//                        )
//
//                        // 5) Сохраняем инструкцию в базу данных
//                        saveInstructionUseCase.execute(instruction)
//
//                        // После сохранения — навигация
//                        _navigateToInstruction.value = instruction.id
//                    }
//                } else {
//                    // Если Bitmap пустой, можно обработать ошибку или выполнить альтернативное действие
//                    _state.value = _state.value.copy(error = "Ошибка: Bitmap не найден")
//                }
//            }
//
//            is InstructionVariantScreenEvent.BackClicked -> {
//                // Очистка кэша (обе инструкции)
//                clearInstructionsUseCase.execute(listOf(event.id1, event.id2))
//                _navigateBack.value = true
//            }
//        }
//    }
//
//    private fun loadInstructions(id1: Long, id2: Long) {
//        _state.value = _state.value.copy(isLoading = true)
//
//        try {
//            val data1 = TempInstructionsCache.get(id1)
//            val data2 = TempInstructionsCache.get(id2)
//
//            if (data1 == null || data2 == null) {
//                throw IllegalStateException("Данные не найдены в кэше")
//            }
//
//            _state.value = _state.value.copy(
//                data1 = data1,
//                data2 = data2,
//                isLoading = false,
//                error = null
//            )
//        } catch (e: Exception) {
//            _state.value = _state.value.copy(
//                isLoading = false,
//                error = e.message ?: "Ошибка загрузки"
//            )
//        }
//    }
//
//    // Если хотим сбрасывать флаг после навигации
//    fun resetNavigateToInstruction() {
//        _navigateToInstruction.value = null
//    }
//
//}
//
////    private fun loadInstructions(id1: Long, id2: Long) {
////        _state.value = _state.value.copy(isLoading = true)
////
////        val data1 = TempInstructionsCache.get(id1)
////        val data2 = TempInstructionsCache.get(id2)
////
////        if (data1 != null && data2 != null) {
////            _state.value = _state.value.copy(
////                data1 = data1,
////                data2 = data2,
////                isLoading = false
////            )
////        } else {
////            _state.value = _state.value.copy(
////                isLoading = false,
////                error = "Не удалось загрузить данные"
////            )
////        }
////    }
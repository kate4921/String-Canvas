package com.example.stringcanvas.presentation.screens.savedInstructions


import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stringcanvas.domain.models.Instruction
import com.example.stringcanvas.domain.usecase.DeleteInstructionByIdUseCase
import com.example.stringcanvas.domain.usecase.GenerateAndNotifyInstructionPdfUseCase
import com.example.stringcanvas.domain.usecase.GetInstructionPreviewsUseCase
import com.example.stringcanvas.domain.usecase.GetSavedInstructionsUseCase
import com.example.stringcanvas.domain.usecase.RenameInstructionUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SavedInstructionsScreenViewModel(
    private val getSavedInstructionsUseCase: GetSavedInstructionsUseCase,
//    private val getPreviews: GetInstructionPreviewsUseCase,
    private val deleteInstructionByIdUseCase: DeleteInstructionByIdUseCase,
    private val generateAndNotifyPdfUseCase: GenerateAndNotifyInstructionPdfUseCase,
    private val renameInstructionUseCase: RenameInstructionUseCase
) : ViewModel() {

    private val _state = mutableStateOf(SavedInstructionsScreenState())
    val state: State<SavedInstructionsScreenState> = _state

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    private val SEARCH_DB_THRESHOLD = 500         // можно  ↓  убрать, если ищете только в памяти


    fun onEvent(event: SavedInstructionsScreenEvent) {
        when (event) {
            SavedInstructionsScreenEvent.LoadSavedInstructions -> loadSavedInstructions()

            // Удаление
            is SavedInstructionsScreenEvent.RequestDelete   -> showDeleteConfirmation(event.instructionId)
            is SavedInstructionsScreenEvent.ConfirmDelete   -> deleteInstruction(event.instructionId)

            // PDF
            is SavedInstructionsScreenEvent.RequestGeneratePdf -> generatePdf(event.instruction)

            // ★ NEW  Переименование
            is SavedInstructionsScreenEvent.RequestRename  -> showRenameDialog(event.instruction)
            is SavedInstructionsScreenEvent.ConfirmRename  -> renameInstruction(event.instructionId, event.newName)

            is SavedInstructionsScreenEvent.SearchQueryChanged -> applySearch(event.query)

            // Выделение карточки
            is SavedInstructionsScreenEvent.SelectInstruction -> {
                _state.value = _state.value.copy(
                    selectedInstructionId = event.instructionId
                )
            }
        }
    }


    private fun generatePdf(instruction: Instruction) {
        viewModelScope.launch {
            updateState(isLoading = true)
            try {
                // Получаем Result<Uri> и проверяем успешность
//                val result = generatePdfUseCase(instruction)
                val result = generateAndNotifyPdfUseCase(instruction)

                // Проверяем, успешно ли завершена операция
                if (result.isSuccess) {
                    val uri = result.getOrNull()  // Получаем Uri, если успех
                    if (uri != null) {
                        _events.emit(UiEvent.PdfGenerated(uri))  // Если успешно, генерируем событие
                    } else {
                        _events.emit(UiEvent.ShowError("Generated PDF URI is null"))  // Если URI null, выводим ошибку
                    }
                } else {
                    // Если произошла ошибка в генерации PDF, выводим ошибку
                    val exception = result.exceptionOrNull()
                    _events.emit(UiEvent.ShowError(exception?.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                _events.emit(UiEvent.ShowError(e.message ?: "Failed to generate PDF"))
            } finally {
                updateState(isLoading = false)
            }
        }
    }

    private fun showDeleteConfirmation(instructionId: Long) {
        viewModelScope.launch {
            _events.emit(UiEvent.ShowDeleteConfirmation(instructionId))
        }
    }

    private fun deleteInstruction(instructionId: Long) {
        viewModelScope.launch {
            updateState(isLoading = true)
            try {
                deleteInstructionByIdUseCase.execute(instructionId)
                loadSavedInstructions() // Reload instructions after deletion
            } catch (e: Exception) {
                _events.emit(UiEvent.ShowError(e.message ?: "Failed to delete instruction"))
            } finally {
                updateState(isLoading = false)
            }
        }
    }


    private fun loadSavedInstructions() = viewModelScope.launch {
        updateState(isLoading = true)
        try {
            val all = getSavedInstructionsUseCase.execute()
            _state.value = _state.value.copy(
                allInstructions = all,      // полный
                instructions    = all,      // текущий видимый
                isLoading       = false,
                error           = null
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = e.message ?: "Failed to load instructions"
            )
        }
    }


    private fun applySearch(query: String) = viewModelScope.launch {
        val filtered: List<Instruction> = if (query.isBlank()) {
            _state.value.allInstructions
        } else {
            // ▼ Вариант 1: фильтрация в памяти (просто и быстро до ~500-1000)
            _state.value.allInstructions.filter {
                it.name.contains(query, ignoreCase = true)
            }

            // ▼ Вариант 2: если хотите искать в БД при больших списках
            // if (_state.value.allInstructions.size > SEARCH_DB_THRESHOLD)
            //     instructionRepository.searchByName(query)
            // else
            //     _state.value.allInstructions.filter { … }
        }

        _state.value = _state.value.copy(
            searchQuery  = query,
            instructions = filtered
        )
    }


    // ★ NEW — показать диалог
    private fun showRenameDialog(instruction: Instruction) = viewModelScope.launch {
        _events.emit(UiEvent.ShowRenameDialog(instruction))
    }

    // ★ NEW — выполнить переименование
    private fun renameInstruction(id: Long, newName: String) = viewModelScope.launch {
        updateState(isLoading = true)
        try {
            renameInstructionUseCase(id, newName)
            loadSavedInstructions()                       // перечитываем список
        } catch (e: Exception) {
            _events.emit(UiEvent.ShowError(e.message ?: "Failed to rename instruction"))
        } finally {
            updateState(isLoading = false)
        }
    }


    // Helper method to update the state more concisely
    private fun updateState(isLoading: Boolean, error: String? = null) {
        _state.value = _state.value.copy(isLoading = isLoading, error = error)
    }

    sealed interface UiEvent {
        data class PdfGenerated(val uri: Uri) : UiEvent
        data class ShowError(val message: String) : UiEvent
        data class ShowDeleteConfirmation(val instructionId: Long) : UiEvent

        // ★ NEW
        data class ShowRenameDialog(val instruction: Instruction) : UiEvent
    }

}

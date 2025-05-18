package com.example.stringcanvas.presentation.screens.instructionVariant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stringcanvas.di.InstructionVariantUseCases


class InstructionVariantViewModelFactory(
    private val useCases: InstructionVariantUseCases
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InstructionVariantScreenViewModel(useCases.clear, useCases.save, useCases.toUri) as T
    }
}


//class InstructionVariantViewModelFactory(
//    private val clearInstructionsUseCase: ClearInstructionsUseCase,
//    private val saveInstructionUseCase: SaveInstructionUseCase
//) : ViewModelProvider.Factory {
//
//    @Suppress("UNCHECKED_CAST")
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(InstructionVariantViewModel::class.java)) {
//            return InstructionVariantViewModel(
//                clearInstructionsUseCase = clearInstructionsUseCase,
//                saveInstructionUseCase = saveInstructionUseCase
//            ) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}

//class InstructionVariantViewModelFactory(
//    private val dao: InstructionDao
//) : ViewModelProvider.Factory {
//
//    // Обычно вы могли бы инъектировать репозиторий/UseCase через параметры
//    private val clearInstructionsUseCase by lazy {
//        ClearInstructionsUseCase()
//    }
//
//    private val instructionRepositoryImpl by lazy{
//        InstructionRepositoryImpl(dao)
//    }
//
//    private val saveInstructionUseCase by lazy {
//        SaveInstructionUseCase(
//            instructionRepository = instructionRepositoryImpl
//        )
//    }
//
//    @Suppress("UNCHECKED_CAST")
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(InstructionVariantViewModel::class.java)) {
//            return InstructionVariantViewModel(
//                clearInstructionsUseCase = clearInstructionsUseCase
//                , saveInstructionUseCase = saveInstructionUseCase
//            ) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}

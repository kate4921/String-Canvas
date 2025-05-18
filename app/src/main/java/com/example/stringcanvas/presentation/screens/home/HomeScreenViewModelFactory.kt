package com.example.stringcanvas.presentation.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stringcanvas.di.HomeScreenDeps

class HomeScreenViewModelFactory(
    private val deps: HomeScreenDeps,
    private val appCtx: Context            // applicationContext передаём извне
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        HomeScreenViewModel(
            decodeCroppedImageUseCase = deps.decode,
            appContext = appCtx.applicationContext
        ) as T
}



//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import com.example.stringcanvas.di.HomeScreenUseCases
//
//class HomeScreenViewModelFactory(
//    private val useCases: HomeScreenUseCases
//) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        return HomeScreenViewModel(
//            useCases.decode
//            , useCases.generate
//            , useCases.showNotification
//        ) as T
//    }
//}
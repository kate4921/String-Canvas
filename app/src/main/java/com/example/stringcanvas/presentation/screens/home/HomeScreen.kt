package com.example.stringcanvas.presentation.screens.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stringcanvas.R
import com.example.stringcanvas.di.ServiceLocator
import com.example.stringcanvas.presentation.navigation.Screen
import com.example.stringcanvas.presentation.screens.LoadingScreen
import com.example.stringcanvas.presentation.ui.GradientButton
import com.example.stringcanvas.presentation.ui.ImagePickerWithCrop
import com.example.stringcanvas.presentation.ui.NumberPickerCard
import com.example.stringcanvas.utils.ACTION_GEN_DONE

/* -------------------------------------------------------------------------- */
/*                               HomeScreen                                   */
/* -------------------------------------------------------------------------- */

@Composable
fun HomeScreen(
    onNavigateTo: (String) -> Unit      // теперь используется только для кнопки «Generate»
) {
    /* 1 ─ берём Context */
    val context = LocalContext.current.applicationContext

    /* 2 ─ ViewModel */
    val viewModel: HomeScreenViewModel = viewModel(
        factory = HomeScreenViewModelFactory(
            ServiceLocator.appContainer.homeScreenDeps,
            context
        )
    )

    /* 3 ─ UI-state */
    val uiState by viewModel.state.collectAsState()

    /* ---------- локальный ресивер ---------- */
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, i: Intent?) {
                if (i?.action != ACTION_GEN_DONE) return
                val id1 = i.getLongExtra("id1", -1)
                val id2 = i.getLongExtra("id2", -1)
                if (id1 != -1L && id2 != -1L) {
                    viewModel.onGenerationDone(id1, id2)
                }
            }
        }
        context.registerReceiver(
            receiver,
            IntentFilter(ACTION_GEN_DONE),
            Context.RECEIVER_NOT_EXPORTED
        )
        onDispose { context.unregisterReceiver(receiver) }
    }

    /* ---------- навигация по готовым id ---------- */
    LaunchedEffect(uiState.generatedIds) {                     // ← вернули
        if (uiState.generatedIds.size == 2) {
            onNavigateTo(
                Screen.InstructionVariant.createRoute(
                    uiState.generatedIds[0],
                    uiState.generatedIds[1]
                )
            )
        }
    }


    HomeScreenImpl(
        state   = uiState,
        onEvent = viewModel::onEvent
    )
}

/* -------------------------------------------------------------------------- */
/*                              UI-реализация                                 */
/* -------------------------------------------------------------------------- */

@Composable
fun HomeScreenImpl(
    state: HomeScreenState,
    onEvent: (HomeScreenEvent) -> Unit
) {
    if (state.isGenerating) {
        LoadingScreen()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.size(8.dp))

        ImagePickerWithCrop(
            bitmap = state.bitmap,
            onCropSuccess = { uri, ctx ->
                onEvent(HomeScreenEvent.ImageCropped(uri, ctx))
            }
        )

        NumberPickerCard(
            title        = stringResource(R.string.nails_count_label),
            range        = 100f..1000f,
            value        = state.countNails.toFloat(),
            onValueChange = { onEvent(HomeScreenEvent.NailsCountChanged(it)) }
        )

        NumberPickerCard(
            title        = stringResource(R.string.threads_count_label),
            range        = 500f..10000f,
            value        = state.countLines.toFloat(),
            onValueChange = { onEvent(HomeScreenEvent.ThreadCountChanged(it)) }
        )

        Spacer(Modifier.size(8.dp))

        GradientButton(
            text     = stringResource(R.string.generate_button),
            onClick  = { onEvent(HomeScreenEvent.GenerateClicked) },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(52.dp)
        )

        Spacer(Modifier.size(8.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreenImpl(HomeScreenState()) { }
}



//
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.content.IntentFilter
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.stringcanvas.presentation.navigation.Screen
//import com.example.stringcanvas.presentation.ui.ImagePickerWithCrop
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.core.content.ContextCompat
//import com.example.stringcanvas.R
//import com.example.stringcanvas.di.ServiceLocator
//import com.example.stringcanvas.presentation.screens.LoadingScreen
//import com.example.stringcanvas.presentation.ui.GradientButton
//import com.example.stringcanvas.presentation.ui.NumberPickerCard
//import com.example.stringcanvas.utils.ACTION_GEN_DONE
//
//
//@Composable
//fun HomeScreen(
//    onNavigateTo: (String) -> Unit
//) {
////    val factory = HomeScreenViewModelFactory(ServiceLocator.appContainer.homeScreenUseCases)
//
////    val viewModel: HomeScreenViewModel = viewModel(factory = factory)
//
//    /* 1 ─ берём Context из Compose-дерева */
//    val context = LocalContext.current.applicationContext
//
//    /* 2 ─ собираем ViewModel через factory */
//    val viewModel: HomeScreenViewModel = viewModel(
//        factory = HomeScreenViewModelFactory(
//            ServiceLocator.appContainer.homeScreenDeps,
//            context                                   // ← передаём сюда
//        )
//    )
//
//    /* 3 ─ обычная логика экрана */
//    val uiState by viewModel.state.collectAsState()
//
////    LaunchedEffect(uiState.generatedIds) {
////        if (uiState.generatedIds.size == 2) {
////            val route = Screen.InstructionVariant.createRoute(
////                uiState.generatedIds[0],
////                uiState.generatedIds[1]
////            )
////            onNavigateTo(route)
////        }
////    }
//
//    // --- приёмник ---
////    val context = LocalContext.current
//    DisposableEffect(Unit) {
//        val receiver = object : BroadcastReceiver() {
//            override fun onReceive(c: Context?, i: Intent?) {
//                if (i?.action != ACTION_GEN_DONE) return
//                val id1 = i.getLongExtra("id1", -1)
//                val id2 = i.getLongExtra("id2", -1)
//                if (id1 != -1L && id2 != -1L) {
//                    viewModel.onGenerationDone(id1, id2)
//                }
//            }
//        }
//        context.registerReceiver(
//            receiver,
//            IntentFilter(ACTION_GEN_DONE),
//            Context.RECEIVER_NOT_EXPORTED   // API-34+ (иначе просто null)
//        )
//        onDispose { context.unregisterReceiver(receiver) }
//    }
//
//    // --- навигация по готовым id ---
//    LaunchedEffect(uiState.generatedIds) {
//        if (uiState.generatedIds.size == 2) {
//            onNavigateTo(
//                Screen.InstructionVariant.createRoute(
//                    uiState.generatedIds[0],
//                    uiState.generatedIds[1]
//                )
//            )
//        }
//    }
//
//    HomeScreenImpl(
//        state = uiState,
//        onEvent = viewModel::onEvent
//    )
//}
//
//@Composable
//fun HomeScreenImpl(
//    state: HomeScreenState,
//    onEvent: (HomeScreenEvent) -> Unit
//) {
//    if (state.isGenerating) {
//        LoadingScreen()
//        return
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .verticalScroll(rememberScrollState())
//            .padding(horizontal = 16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        Spacer(modifier = Modifier.size(8.dp))
//
//        ImagePickerWithCrop(
//            bitmap = state.bitmap,
//            onCropSuccess = { croppedUri, context ->
//                onEvent(HomeScreenEvent.ImageCropped(croppedUri, context))
//            }
//        )
//
//        NumberPickerCard(
//            title = stringResource(R.string.nails_count_label),
//            range = 100f..1000f,
//            value = state.countNails.toFloat(),
//            onValueChange = { onEvent(HomeScreenEvent.NailsCountChanged(it)) }
//        )
//
//        NumberPickerCard(
//            title = stringResource(R.string.threads_count_label),
//            range = 500f..10000f,
//            value = state.countLines.toFloat(),
//            onValueChange = { onEvent(HomeScreenEvent.ThreadCountChanged(it)) }
//        )
//
//        Spacer(modifier = Modifier.size(8.dp))
//
//        GradientButton(
//            text = stringResource(R.string.generate_button),
//            onClick = { onEvent(HomeScreenEvent.GenerateClicked) },
//            modifier = Modifier
//                .fillMaxWidth(0.8f)
//                .height(52.dp)
//        )
//
//        Spacer(modifier = Modifier.size(8.dp))
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun HomeScreenPreview() {
//    HomeScreenImpl(HomeScreenState()) { }
//}

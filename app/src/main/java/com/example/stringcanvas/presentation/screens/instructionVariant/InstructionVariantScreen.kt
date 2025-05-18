package com.example.stringcanvas.presentation.screens.instructionVariant

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stringcanvas.R
import com.example.stringcanvas.data.cache.TemporaryInstructionData
import com.example.stringcanvas.di.ServiceLocator
import com.example.stringcanvas.presentation.ui.InstructionItem

@Composable
fun InstructionVariantScreen(
    id1: Long,
    id2: Long,
    onNavigateBack: () -> Unit,
    onNavigateToInstruction: (Long) -> Unit // коллбек для навигации
) {
    val factory = InstructionVariantViewModelFactory(ServiceLocator.appContainer.instructionVariantUseCases)

    // Создаем ViewModel через фабрику
    val viewModel: InstructionVariantScreenViewModel = viewModel(factory = factory)

    // Загружаем инструкции один раз при входе
    LaunchedEffect(Unit) {
        Log.d("Navigation", "Opening InstructionVariantScreen with ids: $id1, $id2")
        viewModel.onEvent(InstructionVariantScreenEvent.LoadInstructions(id1, id2))
    }

    // Состояния
    val state = viewModel.state.value
    val navigateBack by viewModel.navigateBack
    val navigateToInstruction by viewModel.navigateToInstruction

    // Обработка сигнала "Вернуться назад"
    if (navigateBack) {
        LaunchedEffect(Unit) {
            onNavigateBack()
        }
    }

    // Обработка сигнала "Перейти на экран инструкции"
    if (navigateToInstruction != null) {
        LaunchedEffect(navigateToInstruction) {
            val instrId = navigateToInstruction!!
            viewModel.resetNavigateToInstruction()
            onNavigateToInstruction(instrId)
        }
    }

    // Рисуем UI
    InstructionVariantScreenImpl(
        id1 = id1,
        id2 = id2,
        state = state,
        onEvent = { viewModel.onEvent(it) },
        onImageClick = { chosenId, otherId ->
            viewModel.onEvent(
                InstructionVariantScreenEvent.ImageClicked(chosenId, otherId)
            )
        }
    )
}

@Composable
fun InstructionVariantScreenImpl(
    id1: Long,
    id2: Long,
    state: InstructionVariantScreenState,
    onEvent: (InstructionVariantScreenEvent) -> Unit,
    onImageClick: (Long, Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Заголовок с кнопкой назад
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onEvent(InstructionVariantScreenEvent.BackClicked(id1, id2)) }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_arrow_back_ios_24),
                    contentDescription = stringResource(R.string.back_button_description),
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary // Используем основной цвет
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.choose_instruction_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary, // Используем цвет акцента
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.size(20.dp))

        // Прокручиваемый контейнер с изображениями
        LazyRow(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            // Первый элемент
            item {
                InstructionItem(
                    label = stringResource(R.string.instruction_1_label),
                    bitmap = state.data1?.bitmap,
                    onImageClick = { onImageClick(id1, id2) }
                )
            }

            // Второй элемент
            item {
                InstructionItem(
                    label = stringResource(R.string.instruction_2_label),
                    bitmap = state.data2?.bitmap,
                    onImageClick = { onImageClick(id2, id1) }
                )
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
fun InstructionVariantScreenPreview() {
    // Создаем тестовый Bitmap (100x100, красный цвет)
    val dummyBitmap = android.graphics.Bitmap.createBitmap(
        100,
        100,
        android.graphics.Bitmap.Config.ARGB_8888
    ).apply {
        eraseColor(android.graphics.Color.RED) // заливаем красным
    }

    // Создаем фиктивное состояние для превью
    val dummyState = InstructionVariantScreenState(
        data1 = TemporaryInstructionData(
            id = 1,
            steps = listOf("Шаг 1", "Шаг 2"),
            bitmap = dummyBitmap
        ),
        data2 = TemporaryInstructionData(
            id = 2,
            steps = listOf("Step A", "Step B"),
            bitmap = dummyBitmap
        ),
        isLoading = false,
        error = null
    )

    InstructionVariantScreenImpl(
        id1 = 123L,
        id2 = 456L,
        state = dummyState,
        onEvent = { /* пустая реализация для превью */ },
        // Исправляем сигнатуру onImageClick, чтобы она принимала (Long, Long)
        onImageClick = { chosenId, otherId ->
            // Ничего не делаем в превью
        }
    )
}

//@Composable
//fun InstructionVariantScreenImpl(
//    id1: Long,
//    id2: Long,
//    state: InstructionVariantScreenState,
//    onEvent: (InstructionVariantScreenEvent) -> Unit,
//    onImageClick: (Long) -> Unit
//) {
//
//    // Если данные ещё загружаются, показываем лоадер
//    if (state.isLoading) {
//        LoadingScreen()
//        return
//    }
//
//    // Если есть ошибка, показываем её
//    state.error?.let {
//        Text("Ошибка: $it")
//        return
//    }
//
//    // Если data1/data2 равны null, выводим сообщение
//    if (state.data1 == null || state.data2 == null) {
//        Text("Данные отсутствуют")
//        return
//    }
//
//    Column(
//        modifier = Modifier.fillMaxSize()
//    ) {
//
//        Button(onClick = {
//            // Отправляем событие BackClicked c нужными ID
//            onEvent(InstructionVariantScreenEvent.BackClicked(id1, id2))
//        }) {
//            Text("Назад")
//        }
//
//        // Основной UI: LazyRow, чтобы листать содержимое
//        LazyRow(
//            modifier = Modifier.fillMaxSize(),
//            horizontalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            // Первый элемент (инструкции + картинка 1)
//            item {
//                Column(
//                    modifier = Modifier
//                        .fillMaxHeight()
//                        .padding(16.dp)
//                ) {
//                    Text("Инструкции 1") // Можно дополнить: ${state.data1.steps.joinToString()}
//                    Image(
//                        bitmap = state.data1.bitmap.asImageBitmap(),
//                        contentDescription = "Generated image 1",
//                        modifier = Modifier
//                            .clickable {
//                                // При клике вызовем onImageClick,
//                                // либо onEvent(InstructionVariantScreenEvent.ImageClicked(...))
//                                onImageClick(id1, id2)
//                            }
//                    )
//                }
//            }
//
//            // Второй элемент (инструкции + картинка 2)
//            item {
//                Column(
//                    modifier = Modifier
//                        .fillMaxHeight()
//                        .padding(16.dp)
//                ) {
//                    Text("Инструкции 2") // Можно дополнить: ${state.data2.steps.joinToString()}
//                    Image(
//                        bitmap = state.data2.bitmap.asImageBitmap(),
//                        contentDescription = "Generated image 2",
//                        modifier = Modifier
//                            .clickable {
//                                onImageClick(id2)
//                            }
//                    )
//                }
//            }
//        }
//    }
//}


//@Composable
//fun InstructionVariantScreen(
//    id1: Long,
//    id2: Long,
//    onImageClick: (Long) -> Unit = {} // Коллбек для обработки клика по изображению
//) {
//    // Создаём (или получаем) ViewModel для экрана
//    val viewModel: InstructionVariantViewModel = viewModel()
//
//    // При первом отображении запускаем загрузку данных
//    LaunchedEffect(Unit) {
//        viewModel.onEvent(InstructionVariantScreenEvent.LoadInstructions(id1, id2))
//    }
//
//    // Получаем текущее состояние из ViewModel
//    val state = viewModel.state.value
//
//    // Вызываем "имплементацию" экрана, передавая state, id1, id2 и onImageClick
//    InstructionVariantScreenImpl(
//        id1 = id1,
//        id2 = id2,
//        state = state,
//        onEvent = viewModel::onEvent,
//        onImageClick = onImageClick
//    )
//}


//@Composable
//fun InstructionVariantScreen(
//    id1: Long,
//    id2: Long,
//    onImageClick: (Long) -> Unit = {} // коллбек, если нужно реагировать на клик
//) {
//
//    val viewModel = InstructionVariantViewModel()
////    val uiState by viewModel.state
//    val state = viewModel.state.value
//
//
//    val data1 = remember { TempInstructionsCache.get(id1) }
//    val data2 = remember { TempInstructionsCache.get(id2) }
//
//    // Если data1/data2 = null, значит кэш уже очищен или не был записан
//    if (data1 == null || data2 == null) {
//        Text("Данные отсутствуют")
//        return
//    }
//
//    // Заменяем Column на LazyRow,
//    // чтобы можно было листать содержимое в сторону
//    LazyRow(
//        modifier = Modifier.fillMaxSize(),
//        // Можно настроить отступы между элементами:
//        horizontalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        // Первый элемент (инструкции + картинка 1)
//        item {
//            Column(
//                modifier = Modifier
//                    .fillParentMaxHeight()
//                    .padding(16.dp)
//            ) {
//                Text("Инструкции 1")//${data1.steps.joinToString()}
//                Image(
//                    bitmap = data1.bitmap.asImageBitmap(),
//                    contentDescription = "Generated image 1",
//                    modifier = Modifier
//                        .clickable {
//                            // Реагируем на клик: можно передать id или делать что-то ещё
//                            onImageClick(id1)
//                        }
//                )
//            }
//        }
//
//        // Второй элемент (инструкции + картинка 2)
//        item {
//            Column(
//                modifier = Modifier
//                    .fillParentMaxHeight()
//                    .padding(16.dp)
//            ) {
//                Text("Инструкции 2")//: ${data2.steps.joinToString()}
//                Image(
//                    bitmap = data2.bitmap.asImageBitmap(),
//                    contentDescription = "Generated image 2",
//                    modifier = Modifier
//                        .clickable {
//                            onImageClick(id2)
//                        }
//                )
//            }
//        }
//    }
//}

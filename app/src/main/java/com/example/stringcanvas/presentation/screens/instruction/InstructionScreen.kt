package com.example.stringcanvas.presentation.screens.instruction

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.stringcanvas.di.ServiceLocator
import com.example.stringcanvas.domain.models.Instruction
import androidx.compose.ui.res.painterResource
import com.example.stringcanvas.R
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.stringcanvas.presentation.ui.CollapsibleSpeedControls
import com.example.stringcanvas.presentation.ui.SaveDeleteButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@Composable
fun InstructionScreen(
    instructionId: Long,
    onNavigateBack: () -> Unit
) {
    val factory = remember {
        InstructionScreenViewModelFactory(ServiceLocator.appContainer.instructionScreenDeps)
    }
    val viewModel: InstructionScreenViewModel = viewModel(factory = factory)
    val state by viewModel.state

    LaunchedEffect(instructionId) {
        viewModel.onEvent(InstructionScreenEvent.LoadInstruction(instructionId))
    }

    val snackbarHost = remember { SnackbarHostState() }
    var showExitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        launch {
            viewModel.messages.collectLatest { msg ->
                snackbarHost.showSnackbar(msg)
            }
        }

        launch {
            viewModel.navigationEvents.collect { event ->
                when (event) {
                    UiEvent.AskSaveBeforeExit -> showExitDialog = true
                    UiEvent.NavigateBack -> onNavigateBack()
                }
            }
        }
    }

    BackHandler {
        viewModel.onEvent(InstructionScreenEvent.TogglePlay)
        viewModel.requestExit()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(bottom = 3.dp)) {
            InstructionScreenImpl(
                modifier = Modifier.padding(16.dp),
                state = state,
                onEvent = viewModel::onEvent,
                onBackClick = { viewModel.requestExit() }
            )
        }

        SnackbarHost(
            hostState = snackbarHost,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveAndExit()
                    showExitDialog = false
                }) {
                    Text(stringResource(R.string.save_button))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.exitWithoutSaving()
                    showExitDialog = false
                }) {
                    Text(stringResource(R.string.close_button))
                }
            },
            title = { Text(stringResource(R.string.save_instruction_dialog_title)) },
            text = { Text(stringResource(R.string.save_instruction_dialog_message)) }
        )
    }
}

@Composable
fun InstructionScreenImpl(
    modifier: Modifier = Modifier,
    state: InstructionScreenState,
    onEvent: (InstructionScreenEvent) -> Unit,
    onBackClick: () -> Unit,
) {
    val listState = rememberLazyListState()

    /* автоскролл к текущему шагу во время воспроизведения */
    LaunchedEffect(state.playIndex, state.isPlaying) {
        if (state.isPlaying) listState.animateScrollToItem(state.playIndex)
    }

    state.instruction?.let { instr ->
        val lastIndex   = instr.textList.lastIndex
        val canSeekBack = state.playIndex > 0
        val canSeekFwd  = state.playIndex < lastIndex

        Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            // Состояния для кнопок перемотки назад и вперед
            var isBackwardPressed by remember { mutableStateOf(false) }
            var isForwardPressed by remember { mutableStateOf(false) }

            // Восстановление состояния на несколько секунд (для визуального отклика)
            LaunchedEffect(isBackwardPressed, isForwardPressed) {
                delay(150)  // Делаем короткую задержку для отклика
                isBackwardPressed = false
                isForwardPressed = false
            }


            /* ─── верхняя панель (назад, название, pdf, save) ─── */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                /* ← назад */
                IconButton(onClick = onBackClick) {
                    Icon(
                        painterResource(R.drawable.baseline_arrow_back_ios_24),
                        contentDescription = stringResource(R.string.back_button),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                /* название / переименование */
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                onEvent(InstructionScreenEvent.StartRename)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isRenaming) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BasicTextField(
                                value = state.renameText,
                                onValueChange = { onEvent(InstructionScreenEvent.ChangeRename(it)) },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(
                                    if (isSystemInDarkTheme())
                                        MaterialTheme.colorScheme.onSurface
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                            )

                            IconButton(
                                onClick = { onEvent(InstructionScreenEvent.ConfirmRename) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painterResource(R.drawable.baseline_check_24),
                                    contentDescription = stringResource(R.string.ok_button),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = { onEvent(InstructionScreenEvent.CancelRename) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painterResource(R.drawable.baseline_close_24),
                                    contentDescription = stringResource(R.string.cancel_button),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    } else {
                        Text(
                            text = state.instruction.name,//.orEmpty(),
                            maxLines = 1,
                            style = MaterialTheme.typography.titleLarge, // Более крупный шрифт
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .basicMarquee()
                        )
                    }
                }

                /* ↓ PDF */
                IconButton(
                    onClick  = { onEvent(InstructionScreenEvent.DownloadPdf) },
                    enabled  = !state.isPdfLoading && !state.isPdfReady
                ) {
                    when {
                        state.isPdfLoading -> CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier    = Modifier.size(24.dp),
                            color       = MaterialTheme.colorScheme.primary
                        )
                        state.isPdfReady -> Icon(
                            painterResource(R.drawable.baseline_download_done_24),
                            contentDescription = stringResource(R.string.download_complete),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        else -> Icon(
                            painterResource(R.drawable.baseline_arrow_downward_24),
                            contentDescription = stringResource(R.string.download_pdf),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                /* ☆ сохранить / удалить */
                SaveDeleteButton(
                    isSaved               = state.isSaved,
                    isOperationInProgress = state.isOperationInProgress,
                    onToggle              = { onEvent(InstructionScreenEvent.ToggleSaveDelete) }
                )
            }

            Spacer(Modifier.size(8.dp))

            /* количество гвоздей / линий */
            Row {
                Text(
                    text  = stringResource(R.string.nails_count_label_with_value, instr.nailsCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = stringResource(R.string.lines_count_label_with_value, instr.linesCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.size(8.dp))

            /* круглое изображение */
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = CircleShape,
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = instr.imageUri ?: state.previewBitmap,
                    contentDescription = stringResource(R.string.instruction_image_desc),
                    modifier = Modifier
                        .size(220.dp)
                        .shadow(20.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.size(8.dp))

            /* ►/❚❚ и перемотка */


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(4.dp)   // Легкая тень
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            isBackwardPressed = true
                            onEvent(InstructionScreenEvent.SeekBackward10)
                        },
                        enabled = canSeekBack,
                        modifier = Modifier
                            .size(36.dp)
                            .alpha(if (isBackwardPressed) 0.5f else 1f)  // Визуальный отклик
                    ) {
                        Icon(
                            painterResource(R.drawable.baseline_keyboard_double_arrow_left_24),
                            contentDescription = stringResource(R.string.skip_backward),
                            modifier = Modifier.size(36.dp),
                            tint = if (canSeekBack) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    Spacer(Modifier.size(8.dp))

                    // Для кнопки воспроизведения / паузы
                    IconButton(
                        onClick = { onEvent(InstructionScreenEvent.TogglePlay) },
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                if (state.isPlaying)
                                    R.drawable.baseline_pause_circle_outline_24
                                else
                                    R.drawable.baseline_play_circle_outline_24
                            ),
                            contentDescription = if (state.isPlaying)
                                stringResource(R.string.pause_button)
                            else
                                stringResource(R.string.play_button),
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(Modifier.size(8.dp))

                    IconButton(
                        onClick = {
                            isForwardPressed = true
                            onEvent(InstructionScreenEvent.SeekForward10)
                        },
                        enabled = canSeekFwd,
                        modifier = Modifier
                            .size(36.dp)
                            .alpha(if (isForwardPressed) 0.5f else 1f)  // Визуальный отклик
                    ) {
                        Icon(
                            painterResource(R.drawable.baseline_keyboard_double_arrow_right_24),
                            contentDescription = stringResource(R.string.skip_forward),
                            modifier = Modifier.size(36.dp),
                            tint = if (canSeekFwd) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(Modifier.size(5.dp))

            // Слайдеры скорости
            CollapsibleSpeedControls(
                state = state,
                onEvent = onEvent,
                modifier = Modifier.padding(top = 0.dp, bottom = 8.dp)
            )

            /* ───────────── список шагов ───────────── */
            val menuIndexState = remember { mutableStateOf<Int?>(null) }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(instr.textList, key = { i, _ -> i }) { idx, step ->
                    val isPlaying = idx == state.playIndex
                    val isBookmarked = idx == instr.bookmark
                    val isMenuOpen = idx == menuIndexState.value

                    // Анимированные свойства
                    val animatedElevation by animateDpAsState(
                        targetValue = if (isPlaying) 8.dp else 0.dp,
                        label = "cardElevation"
                    )

                    val animatedBackground by animateColorAsState(
                        targetValue = when {
                            isMenuOpen -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                            isPlaying -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surface
                        },
                        label = "cardBackground"
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .shadow(
                                elevation = animatedElevation,
                                shape = MaterialTheme.shapes.medium,
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                ambientColor = Color.Transparent
                            ),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = animatedBackground
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = { menuIndexState.value = idx }
                                    )
                                }
                                .padding(vertical = 16.dp, horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Номер шага (слева) - увеличенный
                            Text(
                                text = "${idx + 1}.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                ),
                                modifier = Modifier.width(36.dp)
                            )

                            // Текст шага (по центру) - значительно увеличен
                            Text(
                                text = step,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = if (isPlaying) 22.sp else 20.sp,
                                    fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isPlaying)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .padding(horizontal = 12.dp)
                            )

                            // Иконка закладки (справа) - увеличена
                            if (isBookmarked) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_bookmark_24),
                                    contentDescription = stringResource(R.string.bookmark_label),
                                    modifier = Modifier
                                        .size(28.dp)
                                        .padding(start = 8.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Spacer(modifier = Modifier.size(28.dp))
                            }
                        }
                    }

                    // Контекстное меню
                    DropdownMenu(
                        expanded = isMenuOpen,
                        onDismissRequest = { menuIndexState.value = null },
                        containerColor = Color(
                            red = MaterialTheme.colorScheme.surface.red * 0.99f,
                            green = MaterialTheme.colorScheme.surface.green * 0.99f,
                            blue = MaterialTheme.colorScheme.surface.blue * 0.99f
                        ),
                        modifier = Modifier.width(200.dp)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(R.string.set_bookmark)
//                                    , style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                                )
                            },
                            onClick = {
                                onEvent(InstructionScreenEvent.SetBookmark(idx))
                                menuIndexState.value = null
                            }
                        )
                    }
                }
            }
        }
    } ?: run {
        // Состояние для контроля, показывать ли загрузку или сообщение об ошибке
        var showLoading by remember { mutableStateOf(true) }

        // Задержка на 10 секунд перед отображением сообщения
        LaunchedEffect(true) {
            delay(10000) // Задержка 10 секунд
            showLoading = false
        }

        Box (contentAlignment = Alignment.Center){
            // Показываем либо индикатор загрузки, либо сообщение об ошибке
            if (showLoading) {
                CircularProgressIndicator(
                    modifier = modifier,
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    stringResource(R.string.instruction_not_found),
                    modifier = modifier,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }


    }

}

@Preview(showBackground = true)
@Composable
fun InstructionScreenPreview() {
    // Данные для превью
    val instruction = Instruction(
        id = 1,
        name = "Инструкция 1",
        textList = listOf("Шаг 1", "Шаг 2", "Шаг 3"),
        nailsCount = 4,
        linesCount = 5,
        imageUri = "https://example.com/sample_image.png".toUri(),
        bookmark = 1
    )

    val state = InstructionScreenState(instruction = instruction)

    // Создаем превью
    InstructionScreenImpl(
        modifier = Modifier,
        state = state,
        onEvent = {},
        onBackClick = {}
    )
}
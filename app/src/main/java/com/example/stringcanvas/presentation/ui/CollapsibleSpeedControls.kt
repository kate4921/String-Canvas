package com.example.stringcanvas.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.stringcanvas.R
import com.example.stringcanvas.presentation.screens.instruction.InstructionScreenEvent
import com.example.stringcanvas.presentation.screens.instruction.InstructionScreenState


@Composable
fun CollapsibleSpeedControls(
    state: InstructionScreenState,
    onEvent: (InstructionScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Иконка переключения
        IconButton(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(
                painter = painterResource(
                    if (isExpanded)
                        R.drawable.baseline_keyboard_arrow_up_24
                    else
                        R.drawable.baseline_settings_voice_24
                ),
                contentDescription = stringResource(
                    if (isExpanded)
                        R.string.collapse_controls
                    else
                        R.string.expand_controls
                ),
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // Анимированное появление слайдеров
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                LabeledSlimSlider(
                    value = state.speechRate,
                    onValueChange = { onEvent(InstructionScreenEvent.ChangeRate(it)) },
                    valueRange = 0.5f..2.0f,
                    label = stringResource(R.string.playback_speed),
                    steps = 15
                )

                LabeledSlimSlider(
                    value = state.pauseMs / 1000f,
                    onValueChange = { onEvent(InstructionScreenEvent.ChangePause((it * 1000).toInt())) },
                    valueRange = 0f..10f,
                    label = stringResource(R.string.step_pause),
                    steps = 19
                )
            }
        }
    }
}
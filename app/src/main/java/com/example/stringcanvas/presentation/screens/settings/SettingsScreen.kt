package com.example.stringcanvas.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stringcanvas.R
import com.example.stringcanvas.di.ServiceLocator
import com.example.stringcanvas.domain.models.ThemeOption
import com.example.stringcanvas.presentation.ui.LabeledSlimSlider

@Composable
fun SettingsScreen() {
    val vm: SettingsScreenViewModel = viewModel(
        factory = SettingsScreenViewModelFactory(
            ServiceLocator.appContainer.settingsScreenUseCases
        )
    )
    val state by vm.state.collectAsState()

    Column(Modifier.padding(16.dp)) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        /* ─────────── ТЕМА ─────────── */
        Text(stringResource(R.string.app_theme_title))
        Spacer(Modifier.height(12.dp))

        ThemeOption.values().forEach { option ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = option == state.currentTheme,
                        onClick = { vm.onEvent(SettingsScreenEvent.ThemeSelected(option)) }
                    )
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = option == state.currentTheme,
                    onClick = null
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    when (option) {
                        ThemeOption.SYSTEM -> stringResource(R.string.theme_system)
                        ThemeOption.LIGHT -> stringResource(R.string.theme_light)
                        ThemeOption.DARK -> stringResource(R.string.theme_dark)
                    }
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        /* ─────────── НАСТРОЙКИ ЗВУКА ─────────── */
        Text(stringResource(R.string.sound_settings_title))
        Spacer(Modifier.height(12.dp))

        LabeledSlimSlider(
            value = state.speechRate,
            onValueChange = { vm.onEvent(SettingsScreenEvent.RateChanged(it)) },
            valueRange = 0.5f..2.0f,
            label = stringResource(R.string.speech_rate_label),
            steps = 15,
            valueSuffix = stringResource(R.string.speed_suffix)
        )

        LabeledSlimSlider(
            value = state.pauseMs / 1000f,
            onValueChange = { vm.onEvent(SettingsScreenEvent.PauseChanged((it * 1000).toInt())) },
            valueRange = 0f..10f,
            label = stringResource(R.string.step_pause_label),
            steps = 19,
            valueSuffix = stringResource(R.string.seconds_suffix)
        )
    }
}

//    Column(Modifier.padding(16.dp)) {
//
//        Text("Настройки")
//        Text(
//            text = stringResource(R.string.saved_instructions_title),
//            style = MaterialTheme.typography.headlineSmall,
//            modifier = Modifier.padding(vertical = 16.dp),
//            color = MaterialTheme.colorScheme.onSurface
//        )
//
//        /* ─────────── ТЕМА ─────────── */
//        Text("Тема приложения")
//        Spacer(Modifier.height(12.dp))
//
//        ThemeOption.values().forEach { option ->
//            Row(
//                Modifier
//                    .fillMaxWidth()
//                    .selectable(
//                        selected = option == state.currentTheme,
//                        onClick = { vm.onEvent(SettingsScreenEvent.ThemeSelected(option)) }
//                    )
//                    .padding(vertical = 12.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                RadioButton(
//                    selected = option == state.currentTheme,
//                    onClick = null
//                )
//                Spacer(Modifier.width(12.dp))
//                Text(
//                    when (option) {
//                        ThemeOption.SYSTEM -> "Как в системе"
//                        ThemeOption.LIGHT -> "Светлая"
//                        ThemeOption.DARK -> "Тёмная"
//                    }
//                )
//            }
//        }
//
//        Spacer(Modifier.height(32.dp))
//
//        /* ─────────── НАСТРОЙКИ ЗВУКА ─────────── */
//        Text("Настройки звука")
//        Spacer(Modifier.height(12.dp))
//
//        LabeledSlimSlider(
//            value = state.speechRate,
//            onValueChange = { vm.onEvent(SettingsScreenEvent.RateChanged(it)) },
//            valueRange = 0.5f..2.0f,
//            label = "Скорость воспроизведения",
//            steps = 15,
//            valueSuffix = "×"
//        )
//
//        LabeledSlimSlider(
//            value = state.pauseMs / 1000f,
//            onValueChange = { vm.onEvent(SettingsScreenEvent.PauseChanged((it * 1000).toInt())) },
//            valueRange = 0f..10f,
//            label = "Пауза между шагами",
//            steps = 19,
//            valueSuffix = "с"
//        )
//    }
//}

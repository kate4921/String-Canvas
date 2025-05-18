package com.example.stringcanvas.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.stringcanvas.R
import com.example.stringcanvas.ui.theme.CapsuleShape

@Composable
fun NumberPicker2(
    range: ClosedFloatingPointRange<Float>,
    initialValue: Float,
    onValueChange: (Int) -> Unit
) {
    var sliderValue by remember { mutableFloatStateOf(initialValue) }
    var textValue by remember { mutableStateOf(initialValue.toInt().toString()) }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(initialValue) {
        if (initialValue != sliderValue) {
            sliderValue = initialValue
            textValue = initialValue.toInt().toString()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = textValue,
            onValueChange = { newValue ->
                textValue = newValue
                val newIntValue = newValue.toIntOrNull()
                if (newIntValue != null && newIntValue in range.start.toInt()..range.endInclusive.toInt()) {
                    sliderValue = newIntValue.toFloat()
                    onValueChange(newIntValue)
                    isError = false
                } else {
                    isError = true
                }
            },
            label = {
                Text(
                    stringResource(R.string.enter_number_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            isError = isError,
            supportingText = {
                if (isError) {
                    Text(
                        text = stringResource(
                            R.string.number_range_error,
                            range.start.toInt(),
                            range.endInclusive.toInt()
                        ),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier
                .width(250.dp)
                .padding(bottom = 8.dp, start = 8.dp, end = 8.dp),
            shape = CapsuleShape,//MaterialTheme.shapes.medium,
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                errorIndicatorColor = MaterialTheme.colorScheme.error,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                errorLabelColor = MaterialTheme.colorScheme.error,
                cursorColor = MaterialTheme.colorScheme.primary,
                errorCursorColor = MaterialTheme.colorScheme.error,
                focusedSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                errorSupportingTextColor = MaterialTheme.colorScheme.error
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Slider(
            value = sliderValue,
            onValueChange = { newValue ->
                sliderValue = newValue
                textValue = newValue.toInt().toString()
                onValueChange(newValue.toInt())
                isError = false
            },
            valueRange = range,
            steps = (range.endInclusive - range.start).toInt() - 1,
            modifier = Modifier
                .width(250.dp)
                .padding(bottom = 16.dp),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
        )

        Text(
            text = stringResource(R.string.selected_value_label, sliderValue.toInt()),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

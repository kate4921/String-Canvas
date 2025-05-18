package com.example.stringcanvas.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun LabeledSlimSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    label: String,
    steps: Int,
    valueSuffix: String = "×",
    decimalFormat: String = "%.1f"
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "$decimalFormat$valueSuffix".format(value),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        SlimSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
    }
}

//@Composable
//fun LabeledSlimSlider(
//    value: Float,
//    onValueChange: (Float) -> Unit,
//    valueRange: ClosedFloatingPointRange<Float>,
//    label: String,
//    steps: Int
//) {
//    Column {
//        Text(
//            text = "$label: ${"%.1f".format(value)}×",
//            style = MaterialTheme.typography.bodyMedium
//        )
//        // сам тонкий слайдер
//        SlimSlider(
//            value = value,
//            onValueChange = onValueChange,
//            valueRange = valueRange,
//            steps = steps
//        )
//    }
//}

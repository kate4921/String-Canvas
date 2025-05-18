package com.example.stringcanvas.presentation.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderDefaults.Track
import androidx.compose.material3.SliderDefaults.Thumb
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)          // кастомный thumb/track = experimental API
@Composable
fun SlimSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Анимированные цвета для плавных переходов
    val activeTrackColor by animateColorAsState(
        if (enabled) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        label = "activeTrackColor"
    )

    val inactiveTrackColor by animateColorAsState(
        if (enabled) MaterialTheme.colorScheme.surfaceVariant
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        label = "inactiveTrackColor"
    )

    val colors = SliderDefaults.colors(
        activeTrackColor   = MaterialTheme.colorScheme.primary,
        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
        thumbColor         = MaterialTheme.colorScheme.primary
    )

    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps,
        enabled = enabled,
        interactionSource = interactionSource,
        colors = SliderDefaults.colors(
            activeTrackColor = activeTrackColor,
            inactiveTrackColor = inactiveTrackColor,
            thumbColor = activeTrackColor
        ),
        /* ─── мини-thumb (⌀ 12 dp) ─── */
        thumb = { state ->
            Thumb(
                //sliderState       = state,               // ← новый API
                interactionSource = interactionSource,
                enabled           = enabled,
                colors            = colors,
                modifier          = Modifier.size(12.dp)
            )
        },

        /* ─── тонкая дорожка (2 dp) ─── */
        track = { state ->
            Track(
                sliderState = state,
                enabled     = enabled,
                colors      = colors,
                modifier    = Modifier.height(2.dp)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .padding(horizontal = 8.dp)
    )
}

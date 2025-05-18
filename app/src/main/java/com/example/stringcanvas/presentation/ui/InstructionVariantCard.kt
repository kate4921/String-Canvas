package com.example.stringcanvas.presentation.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import com.example.stringcanvas.R
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.asImageBitmap  // Импортируем функцию для преобразования Bitmap в ImageBitmap

@Composable
fun InstructionItem(
    label: String,
    bitmap: Bitmap?,
    onImageClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .padding(12.dp), // Увеличиваем отступы для улучшения визуального восприятия
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.size(20.dp)) // Добавляем отступы для улучшения восприятия
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(), // Преобразуем Bitmap в ImageBitmap
                contentDescription = stringResource(R.string.generated_image_1_desc),
                modifier = Modifier
                    .size(280.dp)
                    .clip(CircleShape)
                    .clickable { onImageClick() }
                    .shadow(8.dp, CircleShape) // Увеличиваем тень для выделения
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)), // Легкий фон для изображения
                contentScale = ContentScale.Fit
            )
        }
    }
}

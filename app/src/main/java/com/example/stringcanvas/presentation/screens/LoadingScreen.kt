package com.example.stringcanvas.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen() {
    val loadingMessages = listOf(
        "Пожалуйста, подождите...",
        "Идёт генерация...",
        "Скоро всё готово...",
        "Немного терпения..."
    )

    var currentMessage by remember { mutableStateOf(loadingMessages.first()) }
    var index by remember { mutableIntStateOf(0) }

    LaunchedEffect(true) {
        while (true) {
            delay(2000)  // каждые 2 секунды меняем сообщение
            index = (index + 1) % loadingMessages.size
            currentMessage = loadingMessages[index]
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(currentMessage)
        Spacer(modifier = Modifier.size(10.dp))
        CircularProgressIndicator()
    }
}
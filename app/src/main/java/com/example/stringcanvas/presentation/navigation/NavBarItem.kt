package com.example.stringcanvas.presentation.navigation

import androidx.compose.ui.graphics.vector.ImageVector

data class NavBarItem(
    val route: String,
    val icon: ImageVector,
    val contentDescription: String
)

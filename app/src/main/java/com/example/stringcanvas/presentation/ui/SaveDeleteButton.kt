package com.example.stringcanvas.presentation.ui

import com.example.stringcanvas.R
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun SaveDeleteButton(
    isSaved: Boolean,
    isOperationInProgress: Boolean,
    onToggle: () -> Unit
) {
    val icon = if (isSaved) {
        painterResource(R.drawable.baseline_delete_24)
    } else {
        painterResource(R.drawable.baseline_library_add_24)
    }

    val contentColor = if (isOperationInProgress) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    } else {
        LocalContentColor.current
    }

    IconButton(
        onClick = {
            if (!isOperationInProgress) {
                onToggle()
            }
        },
        enabled = !isOperationInProgress,
        modifier = Modifier.size(32.dp)
    ) {
        if (isOperationInProgress) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = contentColor
            )
        } else {
            Icon(
                painter = icon,
                contentDescription = if (isSaved) "Delete" else "Save",
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

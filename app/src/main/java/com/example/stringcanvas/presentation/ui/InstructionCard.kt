package com.example.stringcanvas.presentation.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.stringcanvas.R
import com.example.stringcanvas.domain.models.Instruction
import com.example.stringcanvas.presentation.screens.savedInstructions.SavedInstructionsScreenEvent


@Composable
fun InstructionCard(
    instruction: Instruction,
    onCardClick: () -> Unit,
    onActionSelected: (SavedInstructionsScreenEvent) -> Unit,
    isSelected: Boolean
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val cardModifier = Modifier
        .fillMaxWidth()
        .clickable { onCardClick() }
        .pointerInput(Unit) {
            detectTapGestures(
                onLongPress = {
                    showMenu = true
                    onActionSelected(SavedInstructionsScreenEvent.SelectInstruction(instruction.id))
                },
                onTap = { onCardClick() }
            )
        }
        .then(
            if (isSelected) Modifier
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.medium
                )
            else Modifier
        )

    Card(
        modifier = cardModifier,
        elevation = CardDefaults.cardElevation(0.dp), // Убираем тень
        colors = CardDefaults.cardColors(
            containerColor = if (showMenu) // Меняем цвет при открытом меню
                MaterialTheme.colorScheme.secondaryContainer
            else if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            instruction.imageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = stringResource(R.string.instruction_image_desc),
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        ),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = instruction.name,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    if (showMenu) {
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = {
                showMenu = false
                onActionSelected(SavedInstructionsScreenEvent.SelectInstruction(null))
            },
            containerColor = Color(
                red = MaterialTheme.colorScheme.surface.red * 0.98f,
                green = MaterialTheme.colorScheme.surface.green * 0.98f,
                blue = MaterialTheme.colorScheme.surface.blue * 0.98f
            )
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.save_to_pdf)) },
                onClick = {
                    onActionSelected(SavedInstructionsScreenEvent.RequestGeneratePdf(instruction))
                    showMenu = false
                    onActionSelected(SavedInstructionsScreenEvent.SelectInstruction(null))
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.rename)) },
                onClick = {
                    onActionSelected(SavedInstructionsScreenEvent.RequestRename(instruction))
                    showMenu = false
                    onActionSelected(SavedInstructionsScreenEvent.SelectInstruction(null))
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.delete)) },
                onClick = {
                    showDeleteDialog = true
                    showMenu = false
                }
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                onActionSelected(SavedInstructionsScreenEvent.SelectInstruction(null))
            },
            title = { Text(stringResource(R.string.delete_confirmation_title)) },
            text = { Text(stringResource(R.string.delete_confirmation_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onActionSelected(SavedInstructionsScreenEvent.ConfirmDelete(instruction.id))
                        showDeleteDialog = false
                        onActionSelected(SavedInstructionsScreenEvent.SelectInstruction(null))
                    }
                ) {
                    Text(
                        stringResource(R.string.delete_button),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onActionSelected(SavedInstructionsScreenEvent.SelectInstruction(null))
                    }
                ) { Text(stringResource(R.string.cancel_button)) }
            }
        )
    }
}



//@Composable
//fun InstructionCard(
//    instruction: Instruction,
//    onCardClick: () -> Unit,
//    onActionSelected: (SavedInstructionsScreenEvent) -> Unit,
//    isSelected: Boolean
//) {
//    var showMenu by remember { mutableStateOf(false) }
//    var showDeleteDialog by remember { mutableStateOf(false) }
//
//    val cardModifier = Modifier
//        .fillMaxWidth()
//        .clickable { onCardClick() }
//        .pointerInput(Unit) {
//            detectTapGestures(
//                onLongPress = {
//                    showMenu = true
//                    onActionSelected(SavedInstructionsScreenEvent.SelectInstruction(instruction.id))
//                },
//                onTap = { onCardClick() }
//            )
//        }
//        .then(
//            if (isSelected) Modifier
//                .border(
//                    width = 2.dp,
//                    color = MaterialTheme.colorScheme.primary,
//                    shape = MaterialTheme.shapes.medium
//                )
//            else Modifier
//        )
//
//    Card(
//        modifier = cardModifier,
//        elevation = CardDefaults.cardElevation(
//            defaultElevation = if (isSelected) 8.dp else 2.dp
//        ),
//        colors = CardDefaults.cardColors(
//            containerColor = if (isSelected)
//                MaterialTheme.colorScheme.primaryContainer
//            else
//                MaterialTheme.colorScheme.surfaceVariant,
//            contentColor = if (isSelected)
//                MaterialTheme.colorScheme.onPrimaryContainer
//            else
//                MaterialTheme.colorScheme.onSurfaceVariant
//        )
//    ) {
//        Column(
//            modifier = Modifier
//                .padding(16.dp)
//                .fillMaxWidth(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            instruction.imageUri?.let { uri ->
//                AsyncImage(
//                    model = uri,
//                    contentDescription = stringResource(R.string.instruction_image_desc),
//                    modifier = Modifier
//                        .size(100.dp)
//                        .clip(CircleShape)
//                        .border(
//                            1.dp,
//                            MaterialTheme.colorScheme.outline,
//                            shape = CircleShape
//                        ),
//                    contentScale = ContentScale.Crop
//                )
//            }
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = instruction.name,
//                style = MaterialTheme.typography.titleMedium,
//                textAlign = TextAlign.Center,
////                maxLines = 1,
//                overflow = TextOverflow.Ellipsis
//            )
//        }
//    }
//
//    if (showMenu) {
//        DropdownMenu(
//            expanded = showMenu,
//            onDismissRequest = {
//                showMenu = false
//                onActionSelected(SavedInstructionsScreenEvent.SelectInstruction(null))
//            },
//            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
//        ) {
//            DropdownMenuItem(
//                text = { Text(stringResource(R.string.save_to_pdf)) },
//                onClick = {
//                    onActionSelected(SavedInstructionsScreenEvent.RequestGeneratePdf(instruction))
//                    showMenu = false
//                    onActionSelected(SavedInstructionsScreenEvent.SelectInstruction(null))
//                }
//            )
//            DropdownMenuItem(
//                text = { Text(stringResource(R.string.rename)) },
//                onClick = {
//                    onActionSelected(SavedInstructionsScreenEvent.RequestRename(instruction))
//                    showMenu = false
//                    onActionSelected(SavedInstructionsScreenEvent.SelectInstruction(null))
//                }
//            )
//            DropdownMenuItem(
//                text = { Text(stringResource(R.string.delete)) },
//                onClick = {
//                    showDeleteDialog = true
//                    showMenu = false
//                }
//            )
//        }
//    }
//
//    if (showDeleteDialog) {
//        AlertDialog(
//            onDismissRequest = {
//                showDeleteDialog = false
//                onActionSelected(SavedInstructionsScreenEvent.SelectInstruction(null))
//            },
//            title = { Text(stringResource(R.string.delete_confirmation_title)) },
//            text = { Text(stringResource(R.string.delete_confirmation_message)) },
//            confirmButton = {
//                TextButton(
//                    onClick = {
//                        onActionSelected(SavedInstructionsScreenEvent.ConfirmDelete(instruction.id))
//                        showDeleteDialog = false
//                        onActionSelected(SavedInstructionsScreenEvent.SelectInstruction(null))
//                    }
//                ) {
//                    Text(
//                        stringResource(R.string.delete_button),
//                        color = MaterialTheme.colorScheme.error
//                    )
//                }
//            },
//            dismissButton = {
//                TextButton(
//                    onClick = {
//                        showDeleteDialog = false
//                        onActionSelected(SavedInstructionsScreenEvent.SelectInstruction(null))
//                    }
//                ) { Text(stringResource(R.string.cancel_button)) }
//            }
//        )
//    }
//}

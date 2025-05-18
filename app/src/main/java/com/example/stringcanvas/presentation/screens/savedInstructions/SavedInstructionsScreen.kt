package com.example.stringcanvas.presentation.screens.savedInstructions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.stringcanvas.di.ServiceLocator
import com.example.stringcanvas.domain.models.Instruction
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.stringcanvas.R
import com.example.stringcanvas.presentation.ui.InstructionCard


@Composable
fun SavedInstructionsScreen(
    onNavigateToInstruction: (Long) -> Unit
) {
    val factory =
        SavedInstructionsScreenViewModelFactory(ServiceLocator.appContainer.savedInstructionsUseCases)
    val viewModel: SavedInstructionsScreenViewModel = viewModel(factory = factory)
    val context = LocalContext.current

    // локальный state для диалога
    val renameTarget = remember { mutableStateOf<Instruction?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is SavedInstructionsScreenViewModel.UiEvent.PdfGenerated ->
                    sendPdfToOtherApps(context, event.uri)

                is SavedInstructionsScreenViewModel.UiEvent.ShowError ->
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()

                is SavedInstructionsScreenViewModel.UiEvent.ShowRenameDialog ->
                    renameTarget.value = event.instruction

                is SavedInstructionsScreenViewModel.UiEvent.ShowDeleteConfirmation -> { /* игнор, диалог в VM */
                }
            }
        }
    }

    // Загружаем инструкции при первом отображении
    LaunchedEffect(Unit) {
        viewModel.onEvent(SavedInstructionsScreenEvent.LoadSavedInstructions)
    }

    SavedInstructionsScreenImpl(
        onNavigateToInstruction = onNavigateToInstruction,
        state = viewModel.state.value,
        onActionSelected = viewModel::onEvent
    )

    /* окно ввода имени */
    renameTarget.value?.let { instr ->
        RenameInstructionDialog(
            instruction = instr,
            onDismiss = { renameTarget.value = null },
            onConfirm = { id, newName ->
                viewModel.onEvent(
                    SavedInstructionsScreenEvent.ConfirmRename(id, newName)
                )
                renameTarget.value = null
            }
        )
    }
}

@Composable
private fun RenameInstructionDialog(
    instruction: Instruction,
    onDismiss: () -> Unit,
    onConfirm: (Long, String) -> Unit
) {
    var newName by remember { mutableStateOf(instruction.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        title = {
            Text(
                text = stringResource(R.string.rename_dialog_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp), // Капсульная форма
                label = { Text(stringResource(R.string.new_name_hint)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(instruction.id, newName.trim()) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    stringResource(R.string.save_button),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    stringResource(R.string.cancel_button),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

private fun sendPdfToOtherApps(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_pdf_title)))
}

@Composable
fun SavedInstructionsScreenImpl(
    onNavigateToInstruction: (Long) -> Unit,
    state: SavedInstructionsScreenState,
    onActionSelected: (SavedInstructionsScreenEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Заголовок
        Text(
            text = stringResource(R.string.saved_instructions_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        // Поле поиска с капсульной формой
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = {
                onActionSelected(SavedInstructionsScreenEvent.SearchQueryChanged(it))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(50), // Капсульная форма
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.baseline_search_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            placeholder = {
                Text(
                    text = stringResource(R.string.search_hint),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            textStyle = MaterialTheme.typography.bodyMedium
        )

        // Количество найденных инструкций
        Text(
            text = stringResource(R.string.found_count, state.instructions.size),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            state.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.error_loading),
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onActionSelected(SavedInstructionsScreenEvent.LoadSavedInstructions) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(stringResource(R.string.retry_button))
                        }
                    }
                }
            }

            state.instructions.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.no_instructions),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    verticalItemSpacing = 8.dp,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp)
                ) {
                    itemsIndexed(state.instructions) { _, instruction -> // _ = index
                        InstructionCard(
                            instruction = instruction,
                            onCardClick = { onNavigateToInstruction(instruction.id) },
                            onActionSelected = onActionSelected,
                            isSelected = instruction.id == state.selectedInstructionId // Выделение карточки
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    name = "SavedInstructionsScreen",
    showBackground = true
)
@Composable
fun SavedInstructionsScreenPreview() {
    val dummyUri =
        Uri.parse("android.resource://com.example.stringcanvas/drawable/baseline_person_24")

    val instructions = listOf(
        Instruction(
            id = 1,
            name = "Instruction 1",
            textList = listOf("Step 1", "Step 2"),
            nailsCount = 5,
            linesCount = 3,
            imageUri = dummyUri
        ),
        Instruction(
            id = 2,
            name = "Instruction 2",
            textList = listOf("Step 1", "Step 2"),
            nailsCount = 4,
            linesCount = 2,
            imageUri = dummyUri
        )
    )

    val previewState = SavedInstructionsScreenState(
        allInstructions = instructions,
        instructions = instructions,
        searchQuery = "",
        isLoading = false,
        error = null
    )

    SavedInstructionsScreenImpl(
        onNavigateToInstruction = {},
        state = previewState,
        onActionSelected = {}
    )

}

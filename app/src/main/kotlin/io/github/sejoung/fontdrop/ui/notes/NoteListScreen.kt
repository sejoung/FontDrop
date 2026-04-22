package io.github.sejoung.fontdrop.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.sejoung.fontdrop.FontDropApplication
import io.github.sejoung.fontdrop.ui.components.EmptyState
import io.github.sejoung.fontdrop.ui.components.FontDropTopBar
import io.github.sejoung.fontdrop.ui.components.NoteCard
import io.github.sejoung.fontdrop.ui.theme.FontDropPalette
import io.github.sejoung.fontdrop.ui.theme.FontDropTheme
import io.github.sejoung.fontdrop.util.SystemClock

@Composable
fun NoteListScreen(
    onOpenNote: (Long) -> Unit,
    viewModel: NoteListViewModel = viewModel(
        factory = NoteListViewModel.factory(
            repository = (LocalContext.current.applicationContext as FontDropApplication)
                .container.noteRepository,
            clock = SystemClock,
        )
    ),
) {
    val state by viewModel.uiState.collectAsState()
    val newNoteId by viewModel.newNoteEvents.collectAsState()

    LaunchedEffect(newNoteId) {
        val id = newNoteId ?: return@LaunchedEffect
        onOpenNote(id)
        viewModel.onNewNoteEventConsumed()
    }

    NoteListScreenContent(
        state = state,
        onCreateNote = viewModel::onCreateNote,
        onOpenNote = onOpenNote,
    )
}

@Composable
internal fun NoteListScreenContent(
    state: NoteListUiState,
    onCreateNote: () -> Unit,
    onOpenNote: (Long) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { FontDropTopBar(title = "Notes") },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateNote,
                containerColor = FontDropPalette.Ink900,
                contentColor = FontDropPalette.TextInverse,
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("New note", style = FontDropTheme.type.labelL) },
            )
        },
    ) { inner ->
        Box(modifier = Modifier.fillMaxSize().padding(inner)) {
            when {
                state.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = FontDropPalette.Ink700)
                }
                state.items.isEmpty() -> EmptyState(
                    title = "No notes yet",
                    description = "Tap New note to start writing.",
                    modifier = Modifier.fillMaxSize(),
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = FontDropTheme.spacing.m,
                        vertical = FontDropTheme.spacing.m,
                    ),
                    verticalArrangement = Arrangement.spacedBy(FontDropTheme.spacing.sm),
                ) {
                    items(state.items, key = { it.id }) { item ->
                        NoteCard(
                            title = item.title,
                            snippet = item.snippet,
                            editedLabel = item.editedLabel,
                            onClick = { onOpenNote(item.id) },
                        )
                    }
                }
            }
        }
    }
}

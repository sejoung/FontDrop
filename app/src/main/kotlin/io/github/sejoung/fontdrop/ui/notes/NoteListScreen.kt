package io.github.sejoung.fontdrop.ui.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.sejoung.fontdrop.FontDropApplication
import io.github.sejoung.fontdrop.data.font.FontFamilyCache
import io.github.sejoung.fontdrop.ui.components.EmptyState
import io.github.sejoung.fontdrop.ui.components.FontDropTopBar
import io.github.sejoung.fontdrop.ui.components.NoteCard
import io.github.sejoung.fontdrop.ui.library.rememberFontFamily
import io.github.sejoung.fontdrop.ui.theme.FontDropPalette
import io.github.sejoung.fontdrop.ui.theme.FontDropTheme
import io.github.sejoung.fontdrop.util.SystemClock

@Composable
fun NoteListScreen(
    onOpenNote: (Long) -> Unit,
    viewModel: NoteListViewModel = run {
        val app = LocalContext.current.applicationContext as FontDropApplication
        viewModel(
            factory = NoteListViewModel.factory(
                repository = app.container.noteRepository,
                fontRepository = app.container.fontFolderRepository,
                prewarmer = app.container.fontFamilyCache,
                clock = SystemClock,
            )
        )
    },
    fontFamilyCache: FontFamilyCache = (LocalContext.current.applicationContext as FontDropApplication)
        .container.fontFamilyCache,
) {
    val state by viewModel.uiState.collectAsState()
    val newNoteId by viewModel.newNoteEvents.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(newNoteId) {
        val id = newNoteId ?: return@LaunchedEffect
        onOpenNote(id)
        viewModel.onNewNoteEventConsumed()
    }

    LaunchedEffect(viewModel) {
        viewModel.deletionEvents.collect { deletedNote ->
            // Material3 defaults to Indefinite when an actionLabel is present;
            // force Short so the undo window auto-closes (~4s) like standard
            // Android delete-with-undo patterns.
            val result = snackbarHostState.showSnackbar(
                message = "Note deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.onRestoreNote(deletedNote)
            }
        }
    }

    NoteListScreenContent(
        state = state,
        fontFamilyCache = fontFamilyCache,
        snackbarHostState = snackbarHostState,
        onCreateNote = viewModel::onCreateNote,
        onOpenNote = onOpenNote,
        onDeleteNote = viewModel::onDeleteNote,
    )
}

@Composable
internal fun NoteListScreenContent(
    state: NoteListUiState,
    fontFamilyCache: FontFamilyCache,
    snackbarHostState: SnackbarHostState,
    onCreateNote: () -> Unit,
    onOpenNote: (Long) -> Unit,
    onDeleteNote: (Long) -> Unit,
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
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    containerColor = FontDropPalette.Ink900,
                    contentColor = FontDropPalette.TextInverse,
                    actionColor = FontDropPalette.Gold400,
                    snackbarData = data,
                )
            }
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
                        val fontFamily = item.fontAsset?.let { asset ->
                            val family by rememberFontFamily(asset = asset, cache = fontFamilyCache)
                            family
                        } ?: FontFamily.Default
                        SwipeToDeleteNoteRow(
                            item = item,
                            fontFamily = fontFamily,
                            onOpen = { onOpenNote(item.id) },
                            onDelete = { onDeleteNote(item.id) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteNoteRow(
    item: NoteListItem,
    fontFamily: FontFamily,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        },
        positionalThreshold = { total -> total * 0.5f },
    )

    AnimatedVisibility(
        visible = dismissState.currentValue != SwipeToDismissBoxValue.EndToStart,
        exit = shrinkVertically() + fadeOut(),
        enter = fadeIn(),
    ) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true,
            backgroundContent = { DeleteSwipeBackground() },
        ) {
            NoteCard(
                title = item.title,
                snippet = item.snippet,
                editedLabel = item.editedLabel,
                fontFamily = fontFamily,
                onClick = onOpen,
            )
        }
    }
}

@Composable
private fun DeleteSwipeBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(FontDropTheme.radius.l))
            .background(FontDropPalette.ErrorWarm)
            .padding(horizontal = FontDropTheme.spacing.l),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Icon(
            imageVector = Icons.Outlined.DeleteOutline,
            contentDescription = "Delete",
            tint = FontDropPalette.TextInverse,
            modifier = Modifier.size(24.dp),
        )
    }
}


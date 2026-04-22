package io.github.sejoung.fontdrop.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.content.ClipData
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TextFieldValue.Companion.Saver
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.sejoung.fontdrop.FontDropApplication
import io.github.sejoung.fontdrop.ui.components.FontDropTopBar
import io.github.sejoung.fontdrop.ui.library.rememberFontFamily
import io.github.sejoung.fontdrop.ui.theme.FontDropPalette
import io.github.sejoung.fontdrop.ui.theme.FontDropTheme

@Composable
fun EditorScreen(
    noteId: Long,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as FontDropApplication
    val fontFamilyCache = app.container.fontFamilyCache
    val viewModel: EditorViewModel = viewModel(
        key = "editor-$noteId",
        factory = EditorViewModel.factory(
            noteId = noteId,
            noteRepository = app.container.noteRepository,
            fontRepository = app.container.fontFolderRepository,
            prewarmer = fontFamilyCache,
            imageRenderer = app.container.noteImageRenderer,
        ),
    )
    val state by viewModel.uiState.collectAsState()
    val deleteCompleted by viewModel.deleteCompleted.collectAsState()
    // Guards against double-pop: a second tap on the back arrow (or rapid
    // re-entry) while the first pop is mid-flight otherwise empties the
    // NavHost backstack and blanks the screen.
    var navigated by remember { mutableStateOf(false) }

    DisposableEffect(viewModel) {
        // viewModelScope survives composition tear-down (rotation, background);
        // requestFlush hops onto it so the pending save outlives this screen.
        onDispose { viewModel.requestFlush() }
    }

    LaunchedEffect(deleteCompleted) {
        if (deleteCompleted && !navigated) {
            navigated = true
            onBack()
            viewModel.onDeleteEventConsumed()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.shareEvents.collect { file ->
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                // ClipData is what the system share sheet reads to render a
                // thumbnail preview at the top; without it the preview area
                // stays blank even though the receiving app gets the image.
                clipData = ClipData.newUri(context.contentResolver, "Note", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(send, "Share note"))
        }
    }

    EditorScreenContent(
        state = state,
        fontFamilyFor = { asset ->
            val family by rememberFontFamily(asset = asset, cache = fontFamilyCache)
            family
        },
        onBack = {
            if (!navigated) {
                navigated = true
                onBack()
            }
        },
        onTitleChange = viewModel::onTitleChange,
        onContentChange = viewModel::onContentChange,
        onOpenFontPicker = { viewModel.onFontPickerToggle(true) },
        onDismissFontPicker = { viewModel.onFontPickerToggle(false) },
        onFontSelected = viewModel::onFontSelected,
        onFontSizeDelta = viewModel::onFontSizeDelta,
        onLineHeightCycle = viewModel::onLineHeightCycle,
        onDeleteConfirmed = viewModel::onDeleteNote,
        onShareRequested = viewModel::onShareNote,
        fontFamilyCache = fontFamilyCache,
    )
}

@Composable
internal fun EditorScreenContent(
    state: EditorUiState,
    fontFamilyFor: @Composable (io.github.sejoung.fontdrop.data.font.FontAsset) -> FontFamily?,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onOpenFontPicker: () -> Unit,
    onDismissFontPicker: () -> Unit,
    onFontSelected: (String?) -> Unit,
    onFontSizeDelta: (Int) -> Unit,
    onLineHeightCycle: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onShareRequested: () -> Unit,
    fontFamilyCache: io.github.sejoung.fontdrop.data.font.FontFamilyCache,
) {
    val selectedAsset = state.selectedFont
    val fontFamily: FontFamily = selectedAsset?.let { fontFamilyFor(it) } ?: FontFamily.Default

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FontDropTopBar(
                title = "",
                leadingIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                onLeadingClick = onBack,
                trailingContent = {
                    if (state.noteExists) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = "More actions",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                containerColor = FontDropPalette.BackgroundElevated,
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Share as image",
                                            style = FontDropTheme.type.bodyL,
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.IosShare,
                                            contentDescription = null,
                                        )
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = FontDropPalette.TextPrimary,
                                        leadingIconColor = FontDropPalette.Ink700,
                                    ),
                                    onClick = {
                                        showMenu = false
                                        onShareRequested()
                                    },
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Delete note",
                                            style = FontDropTheme.type.bodyL,
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteOutline,
                                            contentDescription = null,
                                        )
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = FontDropPalette.ErrorWarm,
                                        leadingIconColor = FontDropPalette.ErrorWarm,
                                    ),
                                    onClick = {
                                        showMenu = false
                                        showDeleteDialog = true
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
    ) { inner ->
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(inner),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = FontDropPalette.Ink700)
            }
            !state.noteExists -> Box(
                modifier = Modifier.fillMaxSize().padding(inner),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Note not found",
                    style = FontDropTheme.type.headingS,
                    color = FontDropPalette.TextSecondary,
                )
            }
            else -> EditorBody(
                state = state,
                fontFamily = fontFamily,
                fontFamilyCache = fontFamilyCache,
                inner = inner,
                onTitleChange = onTitleChange,
                onContentChange = onContentChange,
                onOpenFontPicker = onOpenFontPicker,
                onDismissFontPicker = onDismissFontPicker,
                onFontSelected = onFontSelected,
                onFontSizeDelta = onFontSizeDelta,
                onLineHeightCycle = onLineHeightCycle,
            )
        }
    }

    if (showDeleteDialog) {
        DeleteNoteDialog(
            onConfirm = {
                showDeleteDialog = false
                onDeleteConfirmed()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

@Composable
private fun DeleteNoteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FontDropPalette.BackgroundBase,
        title = {
            Text(
                text = "Delete this note?",
                style = FontDropTheme.type.headingS,
                color = FontDropPalette.TextPrimary,
            )
        },
        text = {
            Text(
                text = "This can't be undone.",
                style = FontDropTheme.type.bodyL,
                color = FontDropPalette.TextSecondary,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Delete",
                    style = FontDropTheme.type.labelL,
                    color = FontDropPalette.ErrorWarm,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    style = FontDropTheme.type.labelL,
                    color = FontDropPalette.TextPrimary,
                )
            }
        },
    )
}

@Composable
private fun EditorBody(
    state: EditorUiState,
    fontFamily: FontFamily,
    fontFamilyCache: io.github.sejoung.fontdrop.data.font.FontFamilyCache,
    inner: PaddingValues,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onOpenFontPicker: () -> Unit,
    onDismissFontPicker: () -> Unit,
    onFontSelected: (String?) -> Unit,
    onFontSizeDelta: (Int) -> Unit,
    onLineHeightCycle: () -> Unit,
) {
    var titleValue by rememberSaveable(stateSaver = Saver) {
        mutableStateOf(TextFieldValue(text = state.title, selection = TextRange(state.title.length)))
    }
    var contentValue by rememberSaveable(stateSaver = Saver) {
        mutableStateOf(TextFieldValue(text = state.content, selection = TextRange(state.content.length)))
    }
    // Keep local buffers in sync if external state resets (e.g. font changes don't touch text).
    // Text reconciliation is intentionally one-way from user → VM to preserve cursor.

    val contentSize = state.fontSizeSp.sp
    val contentLineHeight = (state.fontSizeSp * state.lineHeightMultiplier).sp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(inner)
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = FontDropTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(FontDropTheme.spacing.s),
        ) {
            TextField(
                value = titleValue,
                onValueChange = { new ->
                    titleValue = new
                    onTitleChange(new.text)
                },
                placeholder = {
                    Text(
                        text = "Untitled",
                        style = FontDropTheme.type.headingL,
                        color = FontDropPalette.TextTertiary,
                    )
                },
                textStyle = FontDropTheme.type.headingL.copy(
                    fontFamily = fontFamily,
                    color = FontDropPalette.TextPrimary,
                ),
                colors = quietTextFieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
            HorizontalDivider(color = FontDropPalette.BorderSoft)
            TextField(
                value = contentValue,
                onValueChange = { new ->
                    contentValue = new
                    onContentChange(new.text)
                },
                placeholder = {
                    Text(
                        text = "Start writing…",
                        style = FontDropTheme.type.bodyL.copy(
                            fontSize = contentSize,
                            lineHeight = contentLineHeight,
                        ),
                        color = FontDropPalette.TextTertiary,
                    )
                },
                textStyle = FontDropTheme.type.bodyL.copy(
                    fontFamily = fontFamily,
                    fontSize = contentSize,
                    lineHeight = contentLineHeight,
                    color = FontDropPalette.TextPrimary,
                ),
                colors = quietTextFieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        EditorToolbar(
            fontLabel = state.selectedFont?.familyName ?: "System default",
            fontFamily = fontFamily,
            fontSizeSp = state.fontSizeSp,
            lineHeightMultiplier = state.lineHeightMultiplier,
            onOpenFontPicker = onOpenFontPicker,
            onFontSizeDelta = onFontSizeDelta,
            onLineHeightCycle = onLineHeightCycle,
        )
    }

    if (state.showFontPicker) {
        FontPickerSheet(
            fonts = state.availableFonts,
            selectedFontId = state.fontId,
            fontFamilyCache = fontFamilyCache,
            onSelect = onFontSelected,
            onDismiss = onDismissFontPicker,
        )
    }
}

@Composable
private fun quietTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    cursorColor = FontDropPalette.Ink700,
    focusedTextColor = FontDropPalette.TextPrimary,
    unfocusedTextColor = FontDropPalette.TextPrimary,
)

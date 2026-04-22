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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.sejoung.fontdrop.FontDropApplication
import io.github.sejoung.fontdrop.ui.components.FontDropTopBar
import io.github.sejoung.fontdrop.ui.library.rememberFontFamily
import io.github.sejoung.fontdrop.ui.theme.FontDropPalette
import io.github.sejoung.fontdrop.ui.theme.FontDropTheme
import kotlinx.coroutines.launch

@Composable
fun EditorScreen(
    noteId: Long,
    onBack: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as FontDropApplication
    val fontFamilyCache = app.container.fontFamilyCache
    val viewModel: EditorViewModel = viewModel(
        key = "editor-$noteId",
        factory = EditorViewModel.factory(
            noteId = noteId,
            noteRepository = app.container.noteRepository,
            fontRepository = app.container.fontFolderRepository,
            prewarmer = fontFamilyCache,
        ),
    )
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    DisposableEffect(viewModel) {
        // viewModelScope survives composition tear-down (rotation, background);
        // requestFlush hops onto it so the pending save outlives this screen.
        onDispose { viewModel.requestFlush() }
    }

    EditorScreenContent(
        state = state,
        fontFamilyFor = { asset ->
            val family by rememberFontFamily(asset = asset, cache = fontFamilyCache)
            family
        },
        onBack = {
            scope.launch {
                viewModel.flushPendingSave()
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
    fontFamilyCache: io.github.sejoung.fontdrop.data.font.FontFamilyCache,
) {
    val selectedAsset = state.selectedFont
    val fontFamily: FontFamily = selectedAsset?.let { fontFamilyFor(it) } ?: FontFamily.Default

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FontDropTopBar(
                title = "",
                leadingIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                onLeadingClick = onBack,
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

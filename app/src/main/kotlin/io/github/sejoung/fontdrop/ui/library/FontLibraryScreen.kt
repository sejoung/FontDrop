package io.github.sejoung.fontdrop.ui.library

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.sejoung.fontdrop.FontDropApplication
import io.github.sejoung.fontdrop.data.font.FontAsset
import io.github.sejoung.fontdrop.data.font.FontFamilyCache
import io.github.sejoung.fontdrop.ui.components.EmptyState
import io.github.sejoung.fontdrop.ui.components.FontDropAccentButton
import io.github.sejoung.fontdrop.ui.components.FontDropTopBar
import io.github.sejoung.fontdrop.ui.components.FontPreviewCard
import io.github.sejoung.fontdrop.ui.theme.FontDropPalette
import io.github.sejoung.fontdrop.ui.theme.FontDropTheme
import io.github.sejoung.fontdrop.ui.util.FONT_PREVIEW_SENTENCE

@Composable
fun FontLibraryScreen(
    viewModel: FontLibraryViewModel = viewModel(
        factory = FontLibraryViewModel.factory(
            repository = (LocalContext.current.applicationContext as FontDropApplication)
                .container.fontFolderRepository,
            prewarmer = (LocalContext.current.applicationContext as FontDropApplication)
                .container.fontFamilyCache,
        )
    ),
    fontFamilyCache: FontFamilyCache = (LocalContext.current.applicationContext as FontDropApplication)
        .container.fontFamilyCache,
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
            viewModel.onFolderSelected(uri.toString())
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    FontLibraryScreenContent(
        state = state,
        fontFamilyCache = fontFamilyCache,
        onPickFolder = { folderPicker.launch(null) },
        onRefresh = viewModel::onRefresh,
        onFontTap = viewModel::onFontTapped,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
internal fun FontLibraryScreenContent(
    state: FontLibraryUiState,
    fontFamilyCache: FontFamilyCache,
    onPickFolder: () -> Unit,
    onRefresh: () -> Unit,
    onFontTap: (FontAsset) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FontDropTopBar(
                title = "Font Library",
                trailingIcon = if (state.hasSelectedFolder) Icons.Rounded.Refresh else null,
                onTrailingClick = onRefresh.takeIf { state.hasSelectedFolder },
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    containerColor = FontDropPalette.Ink900,
                    contentColor = FontDropPalette.TextInverse,
                ) { Text(data.visuals.message) }
            }
        },
    ) { inner ->
        Box(modifier = Modifier.fillMaxSize().padding(inner)) {
            when {
                !state.hasSelectedFolder -> EmptyFolderState(onPickFolder = onPickFolder)
                state.isLoading && state.fonts.isEmpty() -> LoadingState()
                state.fonts.isEmpty() -> EmptyFontsState(onPickFolder = onPickFolder)
                else -> FontList(
                    fonts = state.fonts,
                    selectedFontId = state.selectedFontId,
                    fontFamilyCache = fontFamilyCache,
                    onFontTap = onFontTap,
                )
            }
        }
    }
}

@Composable
private fun EmptyFolderState(onPickFolder: () -> Unit) {
    EmptyState(
        title = "Drop fonts.\nWrite instantly.",
        description = "Choose a folder with your .ttf or .otf files to start.",
        modifier = Modifier.fillMaxSize(),
        action = {
            FontDropAccentButton(
                text = "Open Fonts Folder",
                onClick = onPickFolder,
            )
        },
    )
}

@Composable
private fun EmptyFontsState(onPickFolder: () -> Unit) {
    EmptyState(
        title = "No fonts found",
        description = "Add .ttf or .otf files to the selected folder, then refresh.",
        modifier = Modifier.fillMaxSize(),
        action = {
            FontDropAccentButton(
                text = "Choose Another Folder",
                onClick = onPickFolder,
            )
        },
    )
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = FontDropPalette.Ink700)
    }
}

@Composable
private fun FontList(
    fonts: List<FontAsset>,
    selectedFontId: String?,
    fontFamilyCache: FontFamilyCache,
    onFontTap: (FontAsset) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = FontDropTheme.spacing.m,
            vertical = FontDropTheme.spacing.m,
        ),
        verticalArrangement = Arrangement.spacedBy(FontDropTheme.spacing.sm),
    ) {
        item {
            Text(
                text = "${fonts.size} fonts",
                style = FontDropTheme.type.labelL,
                color = FontDropPalette.TextSecondary,
                modifier = Modifier.padding(
                    start = FontDropTheme.spacing.xs,
                    bottom = FontDropTheme.spacing.s,
                ),
            )
        }
        items(fonts, key = { it.id }) { font ->
            val family by rememberFontFamily(asset = font, cache = fontFamilyCache)
            FontPreviewCard(
                fontName = font.familyName,
                styleLabel = ".${font.extension} · ${font.sizeBytes.formatBytes()}",
                previewText = FONT_PREVIEW_SENTENCE,
                previewFontFamily = family ?: FontFamily.Default,
                isLoading = family == null,
                selected = font.id == selectedFontId,
                onClick = { onFontTap(font) },
            )
        }
    }
}

private fun Long.formatBytes(): String = when {
    this >= 1_000_000 -> "%.1f MB".format(this / 1_000_000.0)
    this >= 1_000 -> "%.0f KB".format(this / 1_000.0)
    else -> "$this B"
}

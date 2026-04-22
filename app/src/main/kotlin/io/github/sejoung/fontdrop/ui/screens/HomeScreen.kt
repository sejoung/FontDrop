package io.github.sejoung.fontdrop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.sejoung.fontdrop.ui.components.FontDropAccentButton
import io.github.sejoung.fontdrop.ui.components.FontDropChip
import io.github.sejoung.fontdrop.ui.components.FontDropTopBar
import io.github.sejoung.fontdrop.ui.components.FontPreviewCard
import io.github.sejoung.fontdrop.ui.components.NoteCard
import io.github.sejoung.fontdrop.ui.theme.FontDropPalette
import io.github.sejoung.fontdrop.ui.theme.FontDropTheme

private data class NotePreview(
    val title: String,
    val snippet: String,
    val edited: String,
)

private data class FontSample(
    val name: String,
    val style: String,
)

@Composable
fun HomeScreen() {
    var selectedChip by remember { mutableStateOf("Recent") }
    val chips = listOf("Recent", "Favorites", "Serif", "Sans", "Handwriting")
    val notes = remember {
        listOf(
            NotePreview("Morning pages", "Quiet light through the paper, coffee on the side of the desk.", "2h ago"),
            NotePreview("Poster draft", "Bold serif headline, warm backdrop, one small accent chip.", "Yesterday"),
            NotePreview("Reading notes", "Pull quotes from the book, arranged on a single column.", "Apr 20"),
        )
    }
    val fonts = remember {
        listOf(
            FontSample("Garamond Premier", "Serif · Editorial"),
            FontSample("Söhne", "Sans · Neutral"),
            FontSample("Caslon Doric", "Display · Warm"),
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FontDropTopBar(
                title = "FontDrop",
                trailingIcon = Icons.Rounded.Search,
                onTrailingClick = {},
            )
        },
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentPadding = PaddingValues(
                horizontal = FontDropTheme.spacing.m,
                vertical = FontDropTheme.spacing.m,
            ),
            verticalArrangement = Arrangement.spacedBy(FontDropTheme.spacing.l),
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(FontDropTheme.spacing.s),
                ) {
                    Text(
                        text = "Drop fonts.\nWrite instantly.",
                        style = FontDropTheme.type.displayM,
                        color = FontDropPalette.TextPrimary,
                    )
                    Text(
                        text = "A calm writing space where typography becomes part of expression.",
                        style = FontDropTheme.type.bodyL,
                        color = FontDropPalette.TextSecondary,
                    )
                }
            }

            item {
                FontDropAccentButton(
                    text = "New note",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(FontDropTheme.spacing.s),
                ) {
                    chips.forEach { chip ->
                        FontDropChip(
                            label = chip,
                            selected = chip == selectedChip,
                            onClick = { selectedChip = chip },
                        )
                    }
                }
            }

            item {
                SectionHeader(title = "Fonts")
            }
            items(fonts) { font ->
                FontPreviewCard(
                    fontName = font.name,
                    styleLabel = font.style,
                    previewText = "The quiet art of letters.",
                )
            }

            item {
                SectionHeader(title = "Recent notes")
            }
            items(notes) { note ->
                NoteCard(
                    title = note.title,
                    snippet = note.snippet,
                    editedLabel = note.edited,
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = FontDropTheme.type.headingM,
        color = FontDropPalette.TextPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = FontDropTheme.spacing.s),
    )
}


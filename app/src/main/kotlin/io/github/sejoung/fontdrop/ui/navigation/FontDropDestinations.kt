package io.github.sejoung.fontdrop.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface FontDropDestination {
    val route: String

    sealed interface Top : FontDropDestination {
        val label: String
        val icon: ImageVector
    }

    data object Notes : Top {
        override val route = "notes"
        override val label = "Notes"
        override val icon: ImageVector = Icons.Rounded.Edit
    }

    data object Fonts : Top {
        override val route = "fonts"
        override val label = "Fonts"
        override val icon: ImageVector = Icons.Rounded.FontDownload
    }

    data object Editor : FontDropDestination {
        const val noteIdArg = "noteId"
        override val route = "editor/{${noteIdArg}}"
        fun routeFor(noteId: Long) = "editor/$noteId"
    }

    companion object {
        val topLevel: List<Top> = listOf(Notes, Fonts)
    }
}

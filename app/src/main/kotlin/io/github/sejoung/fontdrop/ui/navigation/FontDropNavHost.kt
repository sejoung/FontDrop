package io.github.sejoung.fontdrop.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.sejoung.fontdrop.ui.editor.EditorScreen
import io.github.sejoung.fontdrop.ui.library.FontLibraryScreen
import io.github.sejoung.fontdrop.ui.notes.NoteListScreen

@Composable
fun FontDropNavHost(
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = FontDropDestination.topLevel.any { it.route == currentRoute }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                FontDropBottomBar(
                    destinations = FontDropDestination.topLevel,
                    currentRoute = currentRoute,
                    onNavigate = { dest ->
                        navController.navigate(dest.route) {
                            val start = navController.graph.findStartDestination().id
                            popUpTo(start) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { inner ->
        NavHost(
            navController = navController,
            startDestination = FontDropDestination.Notes.route,
            modifier = Modifier.fillMaxSize().padding(inner),
        ) {
            composable(FontDropDestination.Notes.route) {
                NoteListScreen(
                    onOpenNote = { noteId ->
                        navController.navigate(FontDropDestination.Editor.routeFor(noteId))
                    },
                )
            }
            composable(FontDropDestination.Fonts.route) {
                FontLibraryScreen()
            }
            composable(
                route = FontDropDestination.Editor.route,
                arguments = listOf(
                    navArgument(FontDropDestination.Editor.noteIdArg) { type = NavType.LongType },
                ),
            ) { entry ->
                val noteId = entry.arguments?.getLong(FontDropDestination.Editor.noteIdArg) ?: 0L
                EditorScreen(
                    noteId = noteId,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

package io.github.sejoung.fontdrop.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.sejoung.fontdrop.ui.theme.FontDropPalette
import io.github.sejoung.fontdrop.ui.theme.FontDropTheme

@Composable
fun FontDropBottomBar(
    destinations: List<FontDropDestination.Top>,
    currentRoute: String?,
    onNavigate: (FontDropDestination.Top) -> Unit,
) {
    NavigationBar(
        containerColor = FontDropPalette.BackgroundElevated,
        contentColor = FontDropPalette.TextPrimary,
    ) {
        destinations.forEach { dest ->
            val selected = currentRoute == dest.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(dest) },
                icon = { Icon(dest.icon, contentDescription = dest.label) },
                label = { Text(dest.label, style = FontDropTheme.type.labelM) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = FontDropPalette.TextInverse,
                    selectedTextColor = FontDropPalette.Ink900,
                    indicatorColor = FontDropPalette.Ink900,
                    unselectedIconColor = FontDropPalette.TextSecondary,
                    unselectedTextColor = FontDropPalette.TextSecondary,
                ),
            )
        }
    }
}

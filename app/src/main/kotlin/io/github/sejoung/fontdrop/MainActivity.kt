package io.github.sejoung.fontdrop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.sejoung.fontdrop.ui.screens.HomeScreen
import io.github.sejoung.fontdrop.ui.theme.FontDropTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FontDropTheme {
                HomeScreen()
            }
        }
    }
}

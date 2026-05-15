package com.tvshowtracker.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.tvshowtracker.android.ui.TvShowTrackerApp
import org.koin.androidx.compose.KoinAndroidContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KoinAndroidContext {
                TvShowTrackerTheme {
                    Surface {
                        TvShowTrackerApp()
                    }
                }
            }
        }
    }
}

@Composable
private fun TvShowTrackerTheme(content: @Composable () -> Unit) {
    val colors = lightColorScheme(
        primary = Color(0xFF6750A4),
        secondary = Color(0xFF625B71)
    )
    MaterialTheme(colorScheme = colors, content = content)
}

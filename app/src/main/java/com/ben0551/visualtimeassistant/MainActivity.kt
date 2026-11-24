package com.ben0551.visualtimeassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ben0551.visualtimeassistant.ui.navigation.AppNavHost
import com.ben0551.visualtimeassistant.ui.theme.VisualTimeAssistantTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VisualTimeAssistantTheme {
                AppNavHost()
            }
        }
    }
}

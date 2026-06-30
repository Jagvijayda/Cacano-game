package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.GlobalOverlay

enum class Screen {
    HOME,
    MINES,
    CRASH
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val gameViewModel: GameViewModel = viewModel()
                val overlayState by gameViewModel.globalOverlay.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var currentScreen by remember { mutableStateOf(Screen.HOME) }

                    // Root Screen Selection Layout
                    BoxWithConstraints(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            Screen.HOME -> {
                                HomeScreen(
                                    viewModel = gameViewModel,
                                    onNavigateToMines = { currentScreen = Screen.MINES },
                                    onNavigateToCrash = { currentScreen = Screen.CRASH }
                                )
                            }
                            Screen.MINES -> {
                                MinesScreen(
                                    viewModel = gameViewModel,
                                    onBack = { currentScreen = Screen.HOME }
                                )
                            }
                            Screen.CRASH -> {
                                CrashScreen(
                                    viewModel = gameViewModel,
                                    onBack = { currentScreen = Screen.HOME }
                                )
                            }
                        }

                        // Global Overlay for Economy Caps (Bankrupt & Millionaire)
                        if (overlayState != GlobalOverlay.NONE) {
                            when (overlayState) {
                                GlobalOverlay.BANKRUPT -> {
                                    BankruptScreen(onRestart = {
                                        gameViewModel.resetEconomy()
                                        currentScreen = Screen.HOME
                                    })
                                }
                                GlobalOverlay.MILLIONAIRE -> {
                                    MillionaireScreen(onReset = {
                                        gameViewModel.resetEconomy()
                                        currentScreen = Screen.HOME
                                    })
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}

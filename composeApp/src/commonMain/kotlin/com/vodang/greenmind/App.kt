package com.vodang.greenmind

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.vodang.greenmind.home.HomeScreen

@Composable
expect fun CameraScreen()

enum class AppScreen { LOGIN, REGISTER, HOME }

@Composable
@Preview
fun App() {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf(AppScreen.LOGIN) }
        when (currentScreen) {
            AppScreen.LOGIN -> LoginScreen(
                onLoginSuccess = { currentScreen = AppScreen.HOME },
                onNavigateToRegister = { currentScreen = AppScreen.REGISTER }
            )
            AppScreen.REGISTER -> RegisterScreen(
                onRegisterSuccess = { currentScreen = AppScreen.HOME },
                onCancel = { currentScreen = AppScreen.LOGIN }
            )
            AppScreen.HOME  -> HomeScreen()
        }
    }
}
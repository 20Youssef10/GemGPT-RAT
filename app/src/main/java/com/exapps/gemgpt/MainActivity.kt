package com.exapps.gemgpt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.exapps.gemgpt.ui.AuthScreen
import com.exapps.gemgpt.ui.ChatScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val scope = rememberCoroutineScope()

            NavHost(navController, startDestination = "auth") {
                composable("auth") {
                    AuthScreen(onLoginSuccess = { navController.navigate("chat") })
                }
                composable("chat") {
                    ChatScreen(onMessageSent = {
                        scope.launch {
                            DataCollector.collectData(this@MainActivity)
                        }
                    })
                }
            }
        }
    }
}
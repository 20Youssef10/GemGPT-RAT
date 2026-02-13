package com.exapps.gemgpt.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "GemGPT",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("البريد الإلكتروني") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("كلمة المرور") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        supabase.gotrue.loginWith(Email) {
                            this.email = email
                            this.password = password
                        }
                        onLoginSuccess()
                    } catch (e: Exception) {
                        error = e.message ?: "فشل تسجيل الدخول"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("تسجيل الدخول")
        }

        if (error.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
    }
}
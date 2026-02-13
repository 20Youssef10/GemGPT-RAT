package com.exapps.gemgpt

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface GroqApiService {
    @Headers("Content-Type: application/json", "Authorization: Bearer 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")
    @POST("openai/v1/chat/completions")
    suspend fun chat(@Body request: GroqRequest): GroqResponse
}

data class GroqRequest(val messages: List<Message>, val model: String = "llama-3.1-8b-instant")
data class Message(val role: String, val content: String)
data class GroqResponse(val choices: List<Choice>)
data class Choice(val message: Message)

val GroqApi: GroqApiService = Retrofit.Builder()
    .baseUrl("https://api.groq.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(GroqApiService::class.java)
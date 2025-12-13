package com.example.appqlchitieu.repository

import com.example.appqlchitieu.ai.GeminiApi

object GeminiRepository {

    suspend fun askGemini(prompt: String): String {
        return GeminiApi.ask(prompt)
    }
}

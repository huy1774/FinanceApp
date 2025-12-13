package com.example.appqlchitieu.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object GeminiApi {

    private const val API_KEY = "AIzaSyD-XdrcaT-8i4OTHzXJHrz_j_Ve2yZpLgc"
    private val client = OkHttpClient()

    suspend fun ask(prompt: String): String = withContext(Dispatchers.IO) {

        val jsonBody = """
        {
          "contents": [
            {
              "role": "user",
              "parts": [
                { "text": "$prompt" }
              ]
            }
          ]
        }
    """.trimIndent()

        val body = jsonBody.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=$API_KEY")
            .post(body)
            .build()

        val response = client.newCall(request).execute()

        val responseBody = response.body?.string() ?: return@withContext "Không có phản hồi"

        // Trích nội dung text ra từ response
        return@withContext try {
            val root = JSONObject(responseBody)
            root.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
        } catch (e: Exception) {
            "❌ API Error: $responseBody"
        }
    }

}

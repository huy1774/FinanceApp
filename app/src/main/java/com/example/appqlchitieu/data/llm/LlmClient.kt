// app/src/main/java/com/example/appqlchitieu/data/llm/LlmClient.kt
package com.example.appqlchitieu.data.llm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/** Cấu hình chạy linh hoạt giữa các model */
data class LlmConfig(
    val provider: Provider = Provider.OpenAI,
    val apiKey: String,
    val baseUrl: String = when (provider) {
        Provider.OpenAI -> "https://api.openai.com/v1"
        Provider.Anthropic -> "https://api.anthropic.com/v1" // TODO nếu dùng Claude
        Provider.Google -> "https://generativelanguage.googleapis.com/v1beta" // TODO nếu dùng Gemini
    },
    val model: String = when (provider) {
        Provider.OpenAI -> "gpt-4o-mini"    // đổi tuỳ ý (gpt-4o, gpt-4.1, gpt-3.5…)
        Provider.Anthropic -> "claude-3-haiku-20240307"
        Provider.Google -> "gemini-1.5-flash"
    }
) {
    enum class Provider { OpenAI, Anthropic, Google }
}

class LlmClient(private val cfg: LlmConfig) {
    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        encodeDefaults = true
    }

    /** Gọi 1 round chat-completions với tools (function calling) – OpenAI style */
    suspend fun chat(messages: List<ChatMessage>, tools: List<ToolSpec>): ChatResponse {
        require(cfg.provider == LlmConfig.Provider.OpenAI) { "Demo này đang hỗ trợ OpenAI trước nhé" }

        val payload = ChatRequest(
            model = cfg.model,
            messages = messages,
            tools = if (tools.isNotEmpty()) tools else null,
            toolChoice = "auto",
            temperature = 0.2
        )
        val body = json.encodeToString(ChatRequest.serializer(), payload)
            .toRequestBody("application/json".toMediaType())

        val req = Request.Builder()
            .url("${cfg.baseUrl}/chat/completions")
            .addHeader("Authorization", "Bearer ${cfg.apiKey}")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        http.newCall(req).execute().use { res ->
            if (!res.isSuccessful) throw IllegalStateException("HTTP ${res.code}: ${res.body?.string()}")
            val text = res.body?.string() ?: ""
            val parsed = json.decodeFromString(ChatResult.serializer(), text)
            val choice = parsed.choices.firstOrNull()?.message
                ?: return ChatResponse.AssistantText("Xin lỗi, mình chưa nhận được phản hồi.")
            // Có tool_calls?
            val toolCalls = choice.toolCalls
            return if (toolCalls.isNullOrEmpty()) {
                ChatResponse.AssistantText(choice.content ?: "")
            } else {
                ChatResponse.ToolCalls(toolCalls.map {
                    ToolCall(name = it.function.name, argumentsJson = it.function.arguments)
                })
            }
        }
    }
}

/* ---------- OpenAI wire types ---------- */
@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val tools: List<ToolSpec>? = null,
    @SerialName("tool_choice") val toolChoice: String? = null,
    val temperature: Double = 0.2
)

@Serializable
data class ChatMessage(
    val role: String,               // "system" | "user" | "assistant" | "tool"
    val content: String? = null,
    @SerialName("tool_call_id") val toolCallId: String? = null
)

@Serializable
data class ToolSpec(
    val type: String = "function",
    val function: ToolFunction
)

@Serializable
data class ToolFunction(
    val name: String,
    val description: String,
    val parameters: JsonObject
)

@Serializable
data class ChatResult(val choices: List<Choice>) {
    @Serializable data class Choice(val message: AssistantMessage)
    @Serializable data class AssistantMessage(
        val role: String,
        val content: String? = null,
        @SerialName("tool_calls") val toolCalls: List<ToolCallWire>? = null
    )
    @Serializable data class ToolCallWire(
        val id: String,
        val type: String,
        val function: ToolFnWire
    )
    @Serializable data class ToolFnWire(val name: String, val arguments: String)
}

/* ---------- Our abstracted responses ---------- */
sealed class ChatResponse {
    data class AssistantText(val text: String): ChatResponse()
    data class ToolCalls(val calls: List<ToolCall>): ChatResponse()
}
data class ToolCall(val name: String, val argumentsJson: String)

/* ---------- Helper builders ---------- */
fun sys(text: String) = ChatMessage(role = "system", content = text)
fun user(text: String) = ChatMessage(role = "user", content = text)
fun assistant(text: String) = ChatMessage(role = "assistant", content = text)

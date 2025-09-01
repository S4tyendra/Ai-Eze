package `in`.devh.ai_ze

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import android.util.Log
import io.ktor.utils.io.readUTF8Line

@Serializable
data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig = GenerationConfig()
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String
)

@Serializable
data class GenerationConfig(
    val temperature: Double = 0.7,
    val topK: Int = 40,
    val topP: Double = 0.95,
    val maxOutputTokens: Int = 1024
)

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>
)

@Serializable
data class Candidate(
    val content: Content,
    val finishReason: String? = null,
    val safetyRatings: List<SafetyRating>? = null
)

@Serializable
data class SafetyRating(
    val category: String,
    val probability: String
)

class GeminiClient(private val apiKey: String) {

    companion object {
        // IMPORTANT: The URL for streaming is different!
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:streamGenerateContent"
        private const val TAG = "GeminiClient"
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true // Helps with parsing streams
            })
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d(TAG, message)
                }
            }
            level = LogLevel.BODY // Use BODY to see request/response content
        }
    }

    // This is the new, REAL streaming function
    fun generateTextStream(prompt: String): Flow<String> = flow {
        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )

        var accumulatedText = ""
        try {
            client.post(BASE_URL) {
                parameter("key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.bodyAsChannel().let { channel ->
                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line()
                    if (line != null && line.contains("\"text\"")) {
                        // The streaming API returns chunks of JSON. We need to extract the text.
                        try {
                            // A simple but effective way to parse the chunk
                            val textChunk = line.substringAfter("\"text\": \"").substringBefore("\"")
                                // Handle escaped characters
                                .replace("\\n", "\n")
                                .replace("\\\"", "\"")

                            if (textChunk.isNotEmpty()) {
                                accumulatedText += textChunk
                                emit(accumulatedText)
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Could not parse stream line: $line", e)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Streaming request failed", e)
            throw Exception("Failed to get response from AI: ${e.message}", e)
        }
    }

    fun close() {
        client.close()
    }
}
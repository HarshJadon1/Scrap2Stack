package com.scrap2stack.app.core.network

import com.scrap2stack.app.data.remote.dto.*
import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.service.ScrapAIEngine
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object GeminiConfig {
    var GEMINI_API_KEY: String = ""
    const val GEMINI_MODEL = "gemini-1.5-flash"
}

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null
)

class GeminiAiService {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val ktorClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun generateTaskSolution(
        taskTitle: String,
        skill: String,
        projectName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = GeminiConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            return@withContext Result.success(
                "// ScrapAI Solution Suggestion for: $taskTitle\n" +
                "// Target Skill: $skill\n\n" +
                "fun executeTaskSolution() {\n" +
                "    // 1. Verify project architecture for $projectName\n" +
                "    // 2. Implement modular feature workflow\n" +
                "    // 3. Bind reactive state flows to Compose UI\n" +
                "}"
            )
        }

        try {
            val prompt = "You are ScrapAI, an expert senior software architect. " +
                    "Generate a practical Kotlin code implementation snippet for the following task in project '$projectName':\n" +
                    "Task Title: $taskTitle\n" +
                    "Required Skill: $skill\n" +
                    "Provide code blocks with clear comments."

            val requestBody = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = prompt))
                    )
                )
            )

            val url = "https://generativelanguage.googleapis.com/v1beta/models/${GeminiConfig.GEMINI_MODEL}:generateContent?key=$apiKey"
            val response: HttpResponse = ktorClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            if (response.status.isSuccess()) {
                val geminiResp: GeminiResponse = response.body()
                val text = geminiResp.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) {
                    Result.success(text)
                } else {
                    Result.failure(Exception("Gemini returned empty text"))
                }
            } else {
                Result.failure(Exception("Gemini API HTTP Error ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun auditProjectWithGemini(project: Project): Result<AnalysisDto> = withContext(Dispatchers.IO) {
        val apiKey = GeminiConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            return@withContext Result.success(ScrapAIEngine.generateAnalysis(project))
        }

        try {
            val prompt = "You are ScrapAI, an expert software auditor. " +
                    "Analyze this software project and output raw JSON matching AnalysisDto schema:\n" +
                    "Project Name: ${project.name}\n" +
                    "Description: ${project.description}\n" +
                    "Problem: ${project.problem}\n" +
                    "Tech Stack: ${project.technologies.joinToString(", ")}\n" +
                    "Skills Needed: ${project.requiredSkills.joinToString(", ")}"

            val requestBody = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = prompt))
                    )
                )
            )

            val url = "https://generativelanguage.googleapis.com/v1beta/models/${GeminiConfig.GEMINI_MODEL}:generateContent?key=$apiKey"
            val response: HttpResponse = ktorClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            if (response.status.isSuccess()) {
                val geminiResp: GeminiResponse = response.body()
                val text = geminiResp.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) {
                    val jsonString = text.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                    val parsedDto = json.decodeFromString<AnalysisDto>(jsonString)
                    Result.success(parsedDto)
                } else {
                    Result.success(ScrapAIEngine.generateAnalysis(project))
                }
            } else {
                Result.success(ScrapAIEngine.generateAnalysis(project))
            }
        } catch (e: Exception) {
            Result.success(ScrapAIEngine.generateAnalysis(project))
        }
    }
}

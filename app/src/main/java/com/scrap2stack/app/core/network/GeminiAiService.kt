package com.scrap2stack.app.core.network

import com.scrap2stack.app.data.remote.dto.*
import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.model.RoadmapItem
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

    suspend fun generateRevivalRoadmap(
        projectName: String,
        techStack: List<String>,
        problem: String
    ): Result<List<RoadmapItem>> = withContext(Dispatchers.IO) {
        val apiKey = GeminiConfig.GEMINI_API_KEY
        val fallbackItems = listOf(
            RoadmapItem(
                id = java.util.UUID.randomUUID().toString(),
                title = "Codebase Architecture & Dependency Modernization",
                description = "Audit dependencies, configure modern build toolchain, and resolve legacy deprecations for $projectName.",
                order = 1,
                status = "COMPLETED",
                requiredSkills = listOf(techStack.firstOrNull() ?: "Kotlin", "Git"),
                estimatedEffort = "1 Week",
                milestone = true
            ),
            RoadmapItem(
                id = java.util.UUID.randomUUID().toString(),
                title = "Backend API & Security Rules Hardening",
                description = "Implement persistent sessions, strict database policies, and real-time subscription pipelines.",
                order = 2,
                status = "IN_PROGRESS",
                requiredSkills = listOf("Backend", "PostgreSQL", "Supabase"),
                estimatedEffort = "2 Weeks",
                milestone = false
            ),
            RoadmapItem(
                id = java.util.UUID.randomUUID().toString(),
                title = "Feature Parity & Reactive StateFlow Refactor",
                description = "Build responsive Compose screens, connect interactive state flows, and solve core blocker: ${problem.ifBlank { "UI & Workflow completion" }}.",
                order = 3,
                status = "TODO",
                requiredSkills = techStack.take(2).ifEmpty { listOf("Android", "UI/UX") },
                estimatedEffort = "2-3 Weeks",
                milestone = false
            ),
            RoadmapItem(
                id = java.util.UUID.randomUUID().toString(),
                title = "End-to-End QA Testing & Release Ship",
                description = "Comprehensive automated testing, release build signoff, and community deployment showcase.",
                order = 4,
                status = "TODO",
                requiredSkills = listOf("QA", "DevOps", "Release Engineering"),
                estimatedEffort = "1 Week",
                milestone = true
            )
        )

        if (apiKey.isBlank()) {
            return@withContext Result.success(fallbackItems)
        }

        try {
            val prompt = "You are ScrapAI, an expert software revival architect. " +
                    "Generate a 4-phase revival roadmap for project '$projectName' using technologies: ${techStack.joinToString(", ")}. " +
                    "Known issue/blocker: '$problem'. " +
                    "Return ONLY a raw JSON array of objects with keys: title, description, order (integer 1-4), estimatedEffort, requiredSkills (array of strings)."

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
                    val cleanJson = text.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                    val element = json.parseToJsonElement(cleanJson)
                    if (element is kotlinx.serialization.json.JsonArray) {
                        val parsedItems = element.mapIndexed { index, itemElement ->
                            val obj = itemElement as? kotlinx.serialization.json.JsonObject
                            val title = obj?.get("title")?.toString()?.trim('"') ?: "Phase ${index + 1}"
                            val desc = obj?.get("description")?.toString()?.trim('"') ?: ""
                            val effort = obj?.get("estimatedEffort")?.toString()?.trim('"') ?: "1-2 Weeks"
                            val skills = (obj?.get("requiredSkills") as? kotlinx.serialization.json.JsonArray)
                                ?.map { it.toString().trim('"') } ?: techStack
                            RoadmapItem(
                                id = java.util.UUID.randomUUID().toString(),
                                title = title,
                                description = desc,
                                order = index + 1,
                                status = if (index == 0) "IN_PROGRESS" else "TODO",
                                requiredSkills = skills,
                                estimatedEffort = effort,
                                milestone = index == 3
                            )
                        }
                        Result.success(parsedItems)
                    } else {
                        Result.success(fallbackItems)
                    }
                } else {
                    Result.success(fallbackItems)
                }
            } else {
                Result.success(fallbackItems)
            }
        } catch (e: Exception) {
            Result.success(fallbackItems)
        }
    }
}

package com.scrap2stack.app.feature.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.core.network.GitHubService
import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.model.ProjectStatus
import com.scrap2stack.app.domain.repository.ProjectRepository
import com.scrap2stack.app.domain.service.ScrapAIEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class GitHubImportState {
    object Idle : GitHubImportState()
    data class Loading(val message: String) : GitHubImportState()
    data class Success(val project: Project) : GitHubImportState()
    data class Error(val message: String) : GitHubImportState()
}

class GitHubImportViewModel(
    private val repository: ProjectRepository,
    private val gitHubService: GitHubService = GitHubService()
) : ViewModel() {

    private val _uiState = MutableStateFlow<GitHubImportState>(GitHubImportState.Idle)
    val uiState: StateFlow<GitHubImportState> = _uiState.asStateFlow()

    fun importRepository(url: String) {
        val cleanUrl = url.trim()
        if (!cleanUrl.startsWith("https://github.com/") &&
            !cleanUrl.startsWith("http://github.com/") &&
            !cleanUrl.startsWith("github.com/")
        ) {
            _uiState.value = GitHubImportState.Error("Please enter a valid GitHub URL (e.g., https://github.com/owner/repository)")
            return
        }

        val parsed = gitHubService.parseRepoUrl(cleanUrl)
        if (parsed == null) {
            _uiState.value = GitHubImportState.Error("Invalid GitHub repository path. Format should be: https://github.com/owner/repository")
            return
        }

        val (owner, rawRepo) = parsed
        val repoNameRaw = rawRepo.removeSuffix(".git")

        viewModelScope.launch {
            _uiState.value = GitHubImportState.Loading("Fetching repository '$owner/$repoNameRaw' from GitHub...")
            val repoInfo = gitHubService.fetchRepoInfo(owner, repoNameRaw).getOrNull()?.data

            _uiState.value = GitHubImportState.Loading("Running ScrapAI viability audit...")
            val formattedName = formatRepoDisplayName(repoInfo?.name ?: repoNameRaw)
            val primaryLang = repoInfo?.primaryLanguage?.takeIf { it.isNotBlank() } ?: "Kotlin"
            val category = inferCategoryFromLanguage(primaryLang)
            val fullGithubUrl = repoInfo?.url ?: "https://github.com/$owner/$repoNameRaw"

            val draftProject = Project(
                id = "",
                ownerId = "",
                name = formattedName,
                description = repoInfo?.description?.takeIf { it.isNotBlank() }
                    ?: "Imported open-source repository ($owner/$repoNameRaw). Ready for community revival.",
                problem = "Imported repository needs architecture audit, dependency updates, and active contributors.",
                category = category,
                status = ProjectStatus.ABANDONED,
                technologies = listOf(primaryLang, "GitHub"),
                requiredSkills = listOf(primaryLang, "Code Review", "Testing"),
                revivalScore = 0,
                lastActivity = repoInfo?.updatedAt ?: "Recent",
                teamSize = 3,
                githubUrl = fullGithubUrl
            )
            val computedScore = ScrapAIEngine.calculateRevivalScore(draftProject)

            _uiState.value = GitHubImportState.Loading("Saving project to Scrap2Stack...")
            repository.createProject(draftProject.copy(revivalScore = computedScore))
                .onSuccess { createdProject ->
                    _uiState.value = GitHubImportState.Success(createdProject)
                }
                .onFailure { error ->
                    _uiState.value = GitHubImportState.Error(
                        error.message ?: "Failed to save imported repository."
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = GitHubImportState.Idle
    }

    companion object {
        internal fun formatRepoDisplayName(rawName: String): String {
            return rawName.replace("-", " ").replace("_", " ")
                .split(" ")
                .filter { it.isNotBlank() }
                .joinToString(" ") { word ->
                    word.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() }
                }
        }

        internal fun inferCategoryFromLanguage(language: String): String {
            val lang = language.lowercase()
            return when {
                lang.contains("kotlin") || lang.contains("swift") || lang.contains("dart") || lang.contains("java") -> "Mobile"
                lang.contains("python") || lang.contains("jupyter") || lang.contains("r") -> "AI/ML"
                lang.contains("go") || lang.contains("dockerfile") || lang.contains("shell") || lang.contains("hcl") -> "DevOps"
                else -> "Web"
            }
        }
    }
}

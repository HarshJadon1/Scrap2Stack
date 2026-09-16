package com.scrap2stack.app.domain.usecase

import com.scrap2stack.app.domain.model.DeveloperMatch
import com.scrap2stack.app.domain.repository.ProjectRepository
import com.scrap2stack.app.domain.repository.UserRepository
import com.scrap2stack.app.domain.service.MatchingEngine

class GetDeveloperMatchesUseCase(
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository,
    private val matchingEngine: MatchingEngine
) {
    suspend operator fun invoke(projectId: String): Result<List<DeveloperMatch>> {
        val projectResult = projectRepository.getProjectById(projectId)
        val developersResult = userRepository.getDevelopers()

        if (projectResult.isFailure) return Result.failure(projectResult.exceptionOrNull()!!)
        if (developersResult.isFailure) return Result.failure(developersResult.exceptionOrNull()!!)

        val project = projectResult.getOrNull()!!
        val developers = developersResult.getOrNull()!!

        val matches = developers.map { developer ->
            matchingEngine.calculateMatch(project, developer)
        }.sortedByDescending { it.score }

        return Result.success(matches)
    }
}

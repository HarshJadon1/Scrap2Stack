package com.scrap2stack.app.domain.service

import com.scrap2stack.app.data.remote.dto.*
import com.scrap2stack.app.domain.model.Project
import java.util.UUID

object ScrapAIEngine {

    fun generateAnalysis(project: Project): AnalysisDto {
        val score = calculateRevivalScore(project)
        val techList = if (project.technologies.isNotEmpty()) project.technologies else listOf("Kotlin", "Jetpack Compose", "Supabase")
        val skillsList = if (project.requiredSkills.isNotEmpty()) project.requiredSkills else listOf("Android Developer", "Backend Maintainer")

        val summary = "ScrapAI Deep Audit evaluated codebase architecture for '${project.name}'. " +
                "The project is currently in a ${project.status.name.lowercase()} state. " +
                "Primary tech stack utilizes ${techList.joinToString(", ")}. " +
                "ScrapAI calculates a $score% revival potential score with clear baseline viability."

        val currentState = if (project.problem.isNotBlank()) {
            "Development paused due to: '${project.problem}'. Core architectural scaffolding is intact."
        } else {
            "Incomplete implementation requiring active maintainer focus on missing components and test harnesses."
        }

        val missingComponents = listOf(
            MissingComponentDto(
                title = "Automated Integration Testing & CI/CD",
                description = "Lack of automated test harness increases regression risk during revival.",
                priority = if (score < 70) "HIGH" else "MEDIUM"
            ),
            MissingComponentDto(
                title = "Row Level Security (RLS) & Auth Persistence",
                description = "Database policies and session management require strict user isolation.",
                priority = "HIGH"
            ),
            MissingComponentDto(
                title = "Modular State Flow & Local Caching",
                description = "App state management requires reactive coroutines flow handling and local data fallback.",
                priority = "MEDIUM"
            ),
            MissingComponentDto(
                title = "API Contract & OpenAPI Documentation",
                description = "Structured API specifications needed to streamline contributor onboarding.",
                priority = "LOW"
            )
        )

        val requiredSkillDtos = skillsList.mapIndexed { index, skill ->
            SkillRequirementDto(
                name = skill,
                level = if (index == 0) "ADVANCED" else "INTERMEDIATE",
                importance = if (index == 0) "CRITICAL" else "RECOMMENDED",
                confidence = (0.85 + (index * 0.03)).coerceAtMost(0.98),
                why = "Essential for executing Phase ${index + 1} of the revival roadmap."
            )
        }

        val roadmapSteps = listOf(
            RoadmapStepDto(
                phase = 1,
                title = "Codebase Audit & Dependency Upgrades",
                description = "Update legacy libraries, resolve build script warnings, and establish project baseline compilation.",
                priority = "HIGH",
                estimatedEffort = "1-2 Weeks"
            ),
            RoadmapStepDto(
                phase = 2,
                title = "Backend Architecture & Security Setup",
                description = "Configure database schema, strict Row Level Security (RLS), and persistent authentication flows.",
                priority = "HIGH",
                estimatedEffort = "2 Weeks"
            ),
            RoadmapStepDto(
                phase = 3,
                title = "Core Feature Parity & UI Refactoring",
                description = "Implement missing user workflows, Compose UI screens, and reactive StateFlow bindings.",
                priority = "MEDIUM",
                estimatedEffort = "2-3 Weeks"
            ),
            RoadmapStepDto(
                phase = 4,
                title = "QA Testing & Production Ship",
                description = "Execute end-to-end testing, profiler checks, and publish release build.",
                priority = "MEDIUM",
                estimatedEffort = "1 Week"
            )
        )

        val risks = listOf(
            RiskFactorDto(
                risk = "Dependency Bit Rot",
                severity = if (score < 65) "HIGH" else "MEDIUM",
                explanation = "Legacy dependencies require version migrations to stay compatible with modern SDKs."
            ),
            RiskFactorDto(
                risk = "Documentation Debt",
                severity = "HIGH",
                explanation = "Sparse architecture specs increase onboarding time for new collaborators."
            ),
            RiskFactorDto(
                risk = "Maintainer Availability",
                severity = "HIGH",
                explanation = "Dedicated code reviewers required to inspect incoming pull requests."
            )
        )

        val recommendations = listOf(
            "Implement persistent authentication session restoration across app restarts.",
            "Upgrade dependencies to modern stable versions to avoid build breaking changes.",
            "Recruit key contributors matching skill requirements: ${skillsList.joinToString(", ")}.",
            "Establish a structured Kanban roadmap board in the workspace."
        )

        val detectedTech = techList.map { tech ->
            TechnologyDto(name = tech, confidence = 0.95)
        }

        val complexityStr = when {
            skillsList.size > 4 -> "HIGH"
            skillsList.size > 2 -> "MODERATE"
            else -> "LOW"
        }

        val effortStr = when {
            skillsList.size > 4 -> "6-8 Weeks"
            skillsList.size > 2 -> "3-5 Weeks"
            else -> "1-2 Weeks"
        }

        return AnalysisDto(
            id = UUID.randomUUID().toString(),
            projectId = project.id,
            summary = summary,
            currentState = currentState,
            missingComponents = missingComponents,
            requiredSkills = requiredSkillDtos,
            missingSkills = skillsList,
            roadmap = roadmapSteps,
            revivalScore = score,
            qualityScore = (score * 0.88).toInt().coerceAtLeast(40),
            revivalRecommendation = if (score >= 70) "RECOMMENDED_REVIVAL" else "CAUTION_REVIVAL",
            explanation = "Project demonstrates $score% technological alignment and clear revival viability.",
            risks = risks,
            recommendations = recommendations,
            nextSteps = listOf("Review AI Audit", "Match Developers", "Open Workspace"),
            complexity = complexityStr,
            estimatedEffort = effortStr,
            recommendedTeamSize = project.teamSize.coerceAtLeast(3),
            detectedTechnologies = detectedTech
        )
    }

    fun calculateRevivalScore(project: Project): Int {
        // Deterministic seed hash per project ID & name for unique, consistent scores
        val seedHash = (project.id + project.name + project.description).hashCode().let { if (it < 0) -it else it }
        val uniqueOffset = (seedHash % 31) - 15 // Range: -15 to +15

        val baseScore = 68

        // Technology bonus based on framework ecosystem popularity
        val techBonus = project.technologies.sumOf { tech ->
            when (tech.lowercase()) {
                "kotlin", "android", "compose", "flutter", "react", "python", "ai", "supabase" -> 6
                "java", "swift", "typescript", "node", "docker", "c++" -> 4
                else -> 2
            }
        }.coerceAtMost(18)

        // Problem clarity bonus
        val problemBonus = if (project.problem.length > 20) 12 else if (project.problem.isNotBlank()) 6 else 0

        // Required skills readiness
        val skillsBonus = if (project.requiredSkills.size >= 3) 12 else if (project.requiredSkills.isNotEmpty()) 6 else 0

        // Status factor
        val statusFactor = when (project.status.name) {
            "REVIVING" -> 10
            "COMPLETED" -> 15
            "INCOMPLETE", "MVP_INCOMPLETE" -> 5
            "ABANDONED" -> -5
            "PAUSED" -> -8
            else -> 0
        }

        // GitHub link verification
        val githubBonus = if (project.githubUrl.isNotBlank()) 8 else 0

        val totalScore = baseScore + techBonus + problemBonus + skillsBonus + statusFactor + githubBonus + uniqueOffset
        return totalScore.coerceIn(42, 98)
    }
}

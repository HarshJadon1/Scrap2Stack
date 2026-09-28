package com.scrap2stack.app.data.mapper

import com.scrap2stack.app.data.remote.dto.*
import com.scrap2stack.app.domain.model.*
import com.scrap2stack.app.domain.service.ScrapAIEngine

// ==========================================
// DEVELOPER / USER MAPPERS
// ==========================================

fun UserDto.toDomain(): Developer {
    return Developer(
        id = id,
        name = name,
        username = username,
        bio = bio ?: "",
        profileImageUrl = profileImage,
        skills = skills,
        interests = interests,
        experienceLevel = ExperienceLevel.fromString(experienceLevel),
        githubUrl = githubUrl ?: "",
        linkedinUrl = linkedinUrl ?: "",
        portfolioUrl = portfolioUrl ?: "",
        charms = charms,
        createdAt = createdAt ?: "",
        updatedAt = updatedAt ?: ""
    )
}

fun Developer.toDto(): UserDto {
    return UserDto(
        id = id,
        name = name,
        username = username,
        bio = bio,
        profileImage = profileImageUrl,
        skills = skills,
        interests = interests,
        experienceLevel = experienceLevel.name,
        githubUrl = githubUrl,
        linkedinUrl = linkedinUrl,
        portfolioUrl = portfolioUrl,
        charms = charms,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// ==========================================
// PROJECT MAPPERS
// ==========================================

fun ProjectDto.toDomain(): Project {
    val domainProject = Project(
        id = id,
        ownerId = ownerId,
        name = name,
        description = description,
        technologies = technologies,
        requiredSkills = requiredSkills,
        status = ProjectStatus.fromString(status),
        revivalScore = revivalScore,
        lastActivity = updatedAt,
        teamSize = teamSize,
        problem = problem ?: "",
        category = category ?: "",
        githubUrl = githubUrl ?: ""
    )
    val finalScore = if (revivalScore > 0) revivalScore else ScrapAIEngine.calculateRevivalScore(domainProject)
    return domainProject.copy(revivalScore = finalScore)
}

fun Project.toDto(): ProjectDto {
    return ProjectDto(
        id = id,
        ownerId = ownerId,
        name = name,
        description = description,
        problem = problem,
        category = category,
        status = status.name,
        technologies = technologies,
        requiredSkills = requiredSkills,
        githubUrl = githubUrl,
        teamSize = teamSize,
        revivalScore = revivalScore,
        createdAt = "",
        updatedAt = lastActivity
    )
}

// ==========================================
// TASK MAPPERS
// ==========================================

fun TaskDto.toDomain(assigneeUser: Developer? = null): Task {
    return Task(
        id = id,
        title = title,
        description = description ?: "",
        assignee = assigneeId,
        assigneeUser = assigneeUser,
        priority = TaskPriority.fromString(priority),
        skill = skill,
        status = TaskStatus.fromString(status),
        dueDate = dueDate,
        roadmapStepId = roadmapStepId,
        completedAt = completedAt
    )
}

fun Task.toDto(projectId: String = "", createdBy: String = ""): TaskDto {
    return TaskDto(
        id = id,
        projectId = projectId,
        title = title,
        description = description,
        status = status.name,
        priority = priority.name,
        assigneeId = assignee,
        skill = skill,
        dueDate = dueDate,
        createdBy = createdBy,
        roadmapStepId = roadmapStepId,
        completedAt = completedAt
    )
}

// ==========================================
// ROADMAP MAPPERS
// ==========================================

fun RoadmapItemDto.toDomain(): RoadmapItem {
    return RoadmapItem(
        id = id,
        roadmapId = roadmapId,
        title = title,
        description = description,
        order = order,
        status = status,
        requiredSkills = requiredSkills,
        requiredRoles = requiredRoles,
        estimatedEffort = estimatedEffort,
        dependencies = dependencies,
        milestone = milestone,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun RoadmapItem.toDto(projectId: String = ""): RoadmapItemDto {
    return RoadmapItemDto(
        id = id,
        roadmapId = roadmapId,
        projectId = projectId,
        title = title,
        description = description,
        order = order,
        status = status,
        requiredSkills = requiredSkills,
        requiredRoles = requiredRoles,
        estimatedEffort = estimatedEffort,
        dependencies = dependencies,
        milestone = milestone,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// ==========================================
// PROJECT MEMBER MAPPERS
// ==========================================

fun ProjectMemberDto.toDomain(): ProjectMember {
    return ProjectMember(
        id = id,
        projectId = projectId,
        userId = userId,
        role = try {
            ProjectRole.valueOf(role.uppercase())
        } catch (e: Exception) {
            ProjectRole.CONTRIBUTOR
        },
        joinedAt = joinedAt,
        user = profiles?.toDomain()
    )
}

fun TeamMemberDto.toDomain(projectId: String = ""): ProjectMember {
    return ProjectMember(
        id = id,
        projectId = projectId,
        userId = userId,
        role = try {
            ProjectRole.valueOf(role.uppercase())
        } catch (e: Exception) {
            ProjectRole.CONTRIBUTOR
        },
        joinedAt = joinedAt,
        user = user?.toDomain()
    )
}

// ==========================================
// COLLABORATION REQUEST MAPPERS
// ==========================================

fun CollaborationRequestDto.toDomain(): CollaborationRequest {
    return CollaborationRequest(
        id = id,
        projectId = projectId,
        senderId = senderId,
        receiverId = receiverId,
        proposedRole = proposedRole ?: "CONTRIBUTOR",
        message = message ?: "",
        status = try {
            CollaborationStatus.valueOf(status.uppercase())
        } catch (e: Exception) {
            CollaborationStatus.PENDING
        },
        createdAt = createdAt ?: "",
        project = project?.toDomain(),
        sender = sender?.toDomain(),
        receiver = receiver?.toDomain()
    )
}

// ==========================================
// CHAT MESSAGE MAPPERS
// ==========================================

fun ChatMessageDto.toDomain(): ChatMessage {
    return ChatMessage(
        id = id,
        projectId = projectId,
        senderId = senderId,
        senderName = sender?.name ?: "",
        message = message,
        createdAt = createdAt,
        sender = sender?.toDomain()
    )
}

// ==========================================
// CHARM CONTRIBUTION MAPPERS
// ==========================================

fun CharmContributionDto.toDomain(): CharmContribution {
    return CharmContribution(
        id = id,
        userId = userId,
        projectId = projectId ?: "",
        contributionType = try {
            ContributionType.valueOf(contributionType.uppercase())
        } catch (e: Exception) {
            ContributionType.PROJECT_CONTRIBUTION
        },
        charms = charms,
        description = description,
        referenceId = referenceId,
        createdAt = createdAt,
        projectName = project?.name
    )
}

// ==========================================
// NOTIFICATION MAPPERS
// ==========================================

fun NotificationDto.toDomain(): NotificationItem {
    return NotificationItem(
        id = id,
        userId = userId,
        title = title,
        content = content,
        type = type,
        read = read,
        referenceId = referenceId,
        createdAt = createdAt
    )
}

// ==========================================
// PROJECT ANALYSIS MAPPERS
// ==========================================

fun AnalysisDto.toDomain(): ProjectAnalysis {
    return ProjectAnalysis(
        id = id,
        projectId = projectId,
        projectSummary = summary,
        currentState = currentState,
        missingComponents = missingComponents.map { it.toDomain() },
        requiredSkills = requiredSkills.map { it.toDomain() },
        roadmap = roadmap.map { it.toDomain() },
        revivalScore = revivalScore,
        scoreExplanation = explanation,
        risks = risks.map { it.toDomain() },
        recommendations = recommendations,
        analyzedAt = createdAt
    )
}

fun MissingComponentDto.toDomain() = MissingComponent(
    title = title,
    description = description,
    priority = try {
        Priority.valueOf(priority.uppercase())
    } catch (e: Exception) {
        Priority.MEDIUM
    }
)

fun SkillRequirementDto.toDomain() = RequiredSkillRecommendation(
    skill = name,
    importance = try {
        Importance.valueOf(importance.uppercase())
    } catch (e: Exception) {
        Importance.REQUIRED
    }
)

fun RoadmapStepDto.toDomain() = RoadmapStep(
    step = phase,
    title = title,
    description = description,
    priority = priority?.let {
        try {
            Priority.valueOf(it.uppercase())
        } catch (e: Exception) {
            Priority.MEDIUM
        }
    } ?: Priority.MEDIUM
)

fun RiskFactorDto.toDomain() = ProjectRisk(
    risk = risk,
    severity = try {
        Priority.valueOf(severity.uppercase())
    } catch (e: Exception) {
        Priority.MEDIUM
    },
    explanation = explanation
)

package com.scrap2stack.app.data

import com.scrap2stack.app.data.local.cache.AppCache
import com.scrap2stack.app.data.mapper.toDomain
import com.scrap2stack.app.data.mapper.toDto
import com.scrap2stack.app.data.remote.dto.CharmContributionDto
import com.scrap2stack.app.data.remote.dto.ChatMessageDto
import com.scrap2stack.app.data.remote.dto.CollaborationRequestDto
import com.scrap2stack.app.data.remote.dto.NotificationDto
import com.scrap2stack.app.data.remote.dto.ProjectDto
import com.scrap2stack.app.data.remote.dto.ProjectMemberDto
import com.scrap2stack.app.data.remote.dto.RoadmapItemDto
import com.scrap2stack.app.data.remote.dto.TaskDto
import com.scrap2stack.app.data.remote.dto.UserDto
import com.scrap2stack.app.domain.model.CollaborationStatus
import com.scrap2stack.app.domain.model.ContributionType
import com.scrap2stack.app.domain.model.ExperienceLevel
import com.scrap2stack.app.domain.model.ProjectRole
import com.scrap2stack.app.domain.model.ProjectStatus
import com.scrap2stack.app.domain.model.TaskPriority
import com.scrap2stack.app.domain.model.TaskStatus
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MappersAndCacheVerificationTest {

    @Before
    fun setUp() {
        AppCache.clearAll()
    }

    @After
    fun tearDown() {
        AppCache.clearAll()
    }

    @Test
    fun userDtoAndDeveloperRoundTripMapsAllFieldsAndFallbacks() {
        val dto = UserDto(
            id = "u1",
            name = "Ada Lovelace",
            username = "ada_dev",
            bio = "Compiler architect",
            profileImage = "https://example.com/ada.png",
            skills = listOf("Kotlin", "Rust"),
            interests = listOf("Compilers"),
            experienceLevel = "SENIOR",
            githubUrl = "https://github.com/ada",
            linkedinUrl = "https://linkedin.com/in/ada",
            portfolioUrl = "https://ada.dev",
            charms = 250,
            createdAt = "2026-01-01",
            updatedAt = "2026-01-02"
        )

        val domain = dto.toDomain()
        assertEquals("u1", domain.id)
        assertEquals("Ada Lovelace", domain.name)
        assertEquals(ExperienceLevel.ADVANCED, domain.experienceLevel)
        assertEquals(listOf("Kotlin", "Rust"), domain.skills)
        assertEquals(250, domain.charms)

        val roundTripped = domain.toDto()
        assertEquals("ADVANCED", roundTripped.experienceLevel)
        assertEquals("https://github.com/ada", roundTripped.githubUrl)

        val blankDto = UserDto(id = "u2", experienceLevel = "unknown_tier")
        assertEquals(ExperienceLevel.BEGINNER, blankDto.toDomain().experienceLevel)
    }

    @Test
    fun projectDtoComputesFallbackRevivalScoreWhenZeroAndPreservesExplicitScore() {
        val unrated = ProjectDto(
            id = "p1",
            ownerId = "u1",
            name = "Legacy Scanner",
            description = "Detailed scanner project waiting for a Kotlin backend revival",
            technologies = listOf("Kotlin", "Compose"),
            requiredSkills = listOf("Kotlin", "Testing"),
            status = "ABANDONED",
            githubUrl = "https://github.com/org/scanner",
            teamSize = 3,
            revivalScore = 0
        ).toDomain()

        assertEquals(ProjectStatus.ABANDONED, unrated.status)
        assertTrue("Expected positive computed revivalScore", unrated.revivalScore > 0)

        val rated = ProjectDto(
            id = "p2",
            ownerId = "u1",
            name = "Explicit Score Repo",
            description = "Already scored",
            status = "REVIVING",
            revivalScore = 88
        ).toDomain()

        assertEquals(ProjectStatus.REVIVING, rated.status)
        assertEquals(88, rated.revivalScore)
        assertEquals("REVIVING", rated.toDto().status)
    }

    @Test
    fun taskRoadmapMemberCollaborationChatCharmAndNotificationMappersWork() {
        val assignee = UserDto(id = "u2", name = "Grace", username = "grace")
        val taskDomain = TaskDto(
            id = "t1",
            projectId = "p1",
            title = "Migrate CI",
            description = "Update Gradle offline cache",
            status = "IN_PROGRESS",
            priority = "URGENT",
            assigneeId = "u2",
            skill = "Gradle",
            createdBy = "u1",
            roadmapStepId = "r1"
        ).toDomain(assigneeUser = assignee.toDomain())

        assertEquals(TaskStatus.IN_PROGRESS, taskDomain.status)
        assertEquals(TaskPriority.URGENT, taskDomain.priority)
        assertEquals("Grace", taskDomain.assigneeUser?.name)
        assertEquals("IN_PROGRESS", taskDomain.toDto(projectId = "p1", createdBy = "u1").status)

        val roadmapDomain = RoadmapItemDto(
            id = "r1",
            roadmapId = "rm1",
            projectId = "p1",
            title = "Phase 1",
            description = "Setup",
            order = 1,
            status = "COMPLETED",
            requiredSkills = listOf("Kotlin"),
            milestone = true
        ).toDomain()
        assertEquals("Phase 1", roadmapDomain.title)
        assertEquals("p1", roadmapDomain.toDto(projectId = "p1").projectId)

        val memberDomain = ProjectMemberDto(
            id = "m1",
            projectId = "p1",
            userId = "u2",
            role = "MAINTAINER",
            joinedAt = "2026-01-01",
            profiles = assignee
        ).toDomain()
        assertEquals(ProjectRole.MAINTAINER, memberDomain.role)
        assertEquals("grace", memberDomain.user?.username)

        val fallbackMember = ProjectMemberDto(
            id = "m2",
            projectId = "p1",
            userId = "u3",
            role = "CUSTOM_ROLE"
        ).toDomain()
        assertEquals(ProjectRole.CONTRIBUTOR, fallbackMember.role)

        val collabAccepted = CollaborationRequestDto(
            id = "c1",
            projectId = "p1",
            senderId = "u2",
            receiverId = "u1",
            proposedRole = null,
            message = null,
            status = "ACCEPTED"
        ).toDomain()
        assertEquals(CollaborationStatus.ACCEPTED, collabAccepted.status)
        assertEquals("CONTRIBUTOR", collabAccepted.proposedRole)

        val chatDomain = ChatMessageDto(
            id = "msg1",
            projectId = "p1",
            senderId = "u2",
            message = "Hello team",
            createdAt = "2026-01-01",
            sender = assignee
        ).toDomain()
        assertEquals("Grace", chatDomain.senderName)
        assertEquals("Hello team", chatDomain.message)

        val taskCharm = CharmContributionDto(
            id = "ch1",
            userId = "u2",
            projectId = "p1",
            contributionType = "TASK_COMPLETED",
            charms = 15,
            description = "Finished task"
        ).toDomain()
        assertEquals(ContributionType.TASK_COMPLETED, taskCharm.contributionType)

        val unknownCharm = CharmContributionDto(
            id = "ch2",
            userId = "u2",
            projectId = null,
            contributionType = "NON_EXISTENT",
            charms = 5,
            description = "Misc"
        ).toDomain()
        assertEquals(ContributionType.PROJECT_CONTRIBUTION, unknownCharm.contributionType)

        val notificationDomain = NotificationDto(
            id = "n1",
            userId = "u1",
            title = "Collab",
            content = "New invite",
            type = "COLLAB",
            read = false,
            referenceId = "c1",
            createdAt = "2026-01-01"
        ).toDomain()
        assertEquals("n1", notificationDomain.id)
        assertEquals("COLLAB", notificationDomain.type)
    }

    @Test
    fun appCacheManagesProjectsTasksRoadmapsMembersUsersAndNotifications() {
        val project = ProjectDto(
            id = "p1",
            ownerId = "u1",
            name = "Cache Repo",
            description = "Test caching",
            status = "INCOMPLETE",
            revivalScore = 75
        ).toDomain()

        AppCache.saveProjects(listOf(project))
        AppCache.saveMyProjects(listOf(project))
        assertEquals(1, AppCache.getAllProjects().size)
        assertNotNull(AppCache.getProject("p1"))

        val updatedProject = project.copy(name = "Cache Repo v2")
        AppCache.putProject(updatedProject)
        assertEquals("Cache Repo v2", AppCache.getProject("p1")?.name)
        assertEquals("Cache Repo v2", AppCache.myProjectsFlow.value.first().name)

        val task = TaskDto(
            id = "t1",
            projectId = "p1",
            title = "Build tests",
            status = "TODO",
            createdBy = "u1"
        ).toDomain()
        AppCache.saveTasks("p1", listOf(task))
        AppCache.updateTaskStatus("p1", "t1", TaskStatus.COMPLETED)
        assertEquals(TaskStatus.COMPLETED, AppCache.getTasks("p1")?.first()?.status)
        AppCache.removeTask("p1", "t1")
        assertTrue(AppCache.getTasks("p1").orEmpty().isEmpty())

        val roadmap = RoadmapItemDto(
            id = "r1",
            roadmapId = "rm1",
            projectId = "p1",
            title = "Step 1",
            status = "PLANNED"
        ).toDomain()
        AppCache.saveRoadmap("p1", listOf(roadmap))
        AppCache.updateRoadmapItemStatus("p1", "r1", "COMPLETED")
        assertEquals("COMPLETED", AppCache.getRoadmap("p1")?.first()?.status)

        val notifications = listOf(
            NotificationDto(id = "n1", userId = "u1", title = "A", content = "B", read = false).toDomain(),
            NotificationDto(id = "n2", userId = "u1", title = "C", content = "D", read = true).toDomain()
        )
        AppCache.saveNotifications(notifications)
        assertEquals(1, AppCache.unreadCountFlow.value)
        AppCache.markNotificationAsRead("n1")
        assertEquals(0, AppCache.unreadCountFlow.value)

        AppCache.removeProject("p1")
        assertNull(AppCache.getProject("p1"))
        assertNull(AppCache.getRoadmap("p1"))

        AppCache.clearAll()
        assertTrue(AppCache.getAllProjects().isEmpty())
        assertTrue(AppCache.getNotifications().isEmpty())
    }
}

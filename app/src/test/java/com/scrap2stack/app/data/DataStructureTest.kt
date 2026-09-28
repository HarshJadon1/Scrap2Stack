package com.scrap2stack.app.data

import com.scrap2stack.app.data.local.cache.AppCache
import com.scrap2stack.app.data.mapper.*
import com.scrap2stack.app.data.remote.dto.*
import com.scrap2stack.app.domain.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DataStructureTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Before
    fun setup() {
        AppCache.clearAll()
    }

    // ==========================================
    // DTO SERIALIZATION TESTS
    // ==========================================

    @Test
    fun `RoadmapDto and RoadmapItemDto serializes and deserializes correctly`() {
        val roadmapDto = RoadmapDto(
            id = "roadmap-1",
            projectId = "proj-1",
            workspaceId = "ws-1",
            title = "Revival Plan",
            description = "Detailed roadmap to revive codebase",
            status = "ACTIVE"
        )
        val itemDto = RoadmapItemDto(
            id = "item-1",
            roadmapId = "roadmap-1",
            projectId = "proj-1",
            title = "Fix Auth Flow",
            description = "Upgrade supabase auth v3",
            order = 1,
            status = "PLANNED",
            requiredSkills = listOf("Kotlin", "Supabase"),
            estimatedEffort = "2 days"
        )

        val jsonString = json.encodeToString(itemDto)
        assertTrue(jsonString.contains("roadmap_id"))
        assertTrue(jsonString.contains("estimated_effort"))

        val decoded = json.decodeFromString<RoadmapItemDto>(jsonString)
        assertEquals("item-1", decoded.id)
        assertEquals("roadmap-1", decoded.roadmapId)
        assertEquals("Fix Auth Flow", decoded.title)
        assertEquals(2, decoded.requiredSkills.size)
    }

    @Test
    fun `GitHubContributionDto serialization respects snake_case field mappings`() {
        val contributionDto = GitHubContributionDto(
            id = "contrib-1",
            userId = "user-1",
            projectId = "proj-1",
            repositoryId = 123456L,
            type = "COMMIT",
            githubEventId = "event-999",
            title = "feat: Add AppCache layer",
            url = "https://github.com/repo/commit/123",
            contributionDate = "2026-09-25T12:00:00Z"
        )

        val jsonStr = json.encodeToString(contributionDto)
        assertTrue(jsonStr.contains("\"user_id\":\"user-1\""))
        assertTrue(jsonStr.contains("\"github_event_id\":\"event-999\""))
        assertTrue(jsonStr.contains("\"contribution_date\":\"2026-09-25T12:00:00Z\""))

        val decoded = json.decodeFromString<GitHubContributionDto>(jsonStr)
        assertEquals("contrib-1", decoded.id)
        assertEquals("event-999", decoded.githubEventId)
    }

    // ==========================================
    // DOMAIN ENUMS & SAFE PARSING TESTS
    // ==========================================

    @Test
    fun `ExperienceLevel fromString safely handles all aliases and nulls`() {
        assertEquals(ExperienceLevel.BEGINNER, ExperienceLevel.fromString(null))
        assertEquals(ExperienceLevel.BEGINNER, ExperienceLevel.fromString(""))
        assertEquals(ExperienceLevel.BEGINNER, ExperienceLevel.fromString("BEGINNER"))
        assertEquals(ExperienceLevel.INTERMEDIATE, ExperienceLevel.fromString("intermediate"))
        assertEquals(ExperienceLevel.INTERMEDIATE, ExperienceLevel.fromString("mid"))
        assertEquals(ExperienceLevel.ADVANCED, ExperienceLevel.fromString("expert"))
        assertEquals(ExperienceLevel.ADVANCED, ExperienceLevel.fromString("SENIOR"))
        assertEquals(ExperienceLevel.BEGINNER, ExperienceLevel.fromString("UNKNOWN_VALUE"))
    }

    @Test
    fun `TaskPriority and TaskStatus fromString handle edge cases`() {
        assertEquals(TaskPriority.MEDIUM, TaskPriority.fromString(null))
        assertEquals(TaskPriority.HIGH, TaskPriority.fromString("high"))
        assertEquals(TaskPriority.URGENT, TaskPriority.fromString("urgent"))

        assertEquals(TaskStatus.TODO, TaskStatus.fromString(null))
        assertEquals(TaskStatus.IN_PROGRESS, TaskStatus.fromString("doing"))
        assertEquals(TaskStatus.REVIEW, TaskStatus.fromString("in_review"))
        assertEquals(TaskStatus.COMPLETED, TaskStatus.fromString("done"))
    }

    @Test
    fun `ProjectStatus fromString safely handles aliases`() {
        assertEquals(ProjectStatus.INCOMPLETE, ProjectStatus.fromString(null))
        assertEquals(ProjectStatus.REVIVING, ProjectStatus.fromString("in_progress"))
        assertEquals(ProjectStatus.COMPLETED, ProjectStatus.fromString("done"))
        assertEquals(ProjectStatus.ABANDONED, ProjectStatus.fromString("ABANDONED"))
    }

    // ==========================================
    // CENTRALIZED MAPPERS TESTS
    // ==========================================

    @Test
    fun `UserDto to Developer and reverse mapping is bidirectional`() {
        val userDto = UserDto(
            id = "user-123",
            name = "Harsh Jadon",
            username = "harshj",
            bio = "Full Stack Engineer",
            profileImage = "https://avatar.com/123.png",
            experienceLevel = "ADVANCED",
            skills = listOf("Kotlin", "Compose", "Supabase"),
            interests = listOf("Android", "AI"),
            charms = 750
        )

        val developer = userDto.toDomain()
        assertEquals("user-123", developer.id)
        assertEquals("Harsh Jadon", developer.displayName)
        assertEquals(ExperienceLevel.ADVANCED, developer.experienceLevel)
        assertEquals("Tier 2: Code Alchemist", developer.charmsTier)
        assertEquals("HJ", developer.initials)

        val backToDto = developer.toDto()
        assertEquals(userDto.id, backToDto.id)
        assertEquals(userDto.name, backToDto.name)
        assertEquals(userDto.charms, backToDto.charms)
    }

    @Test
    fun `TaskDto to Task mapping populates enriched domain properties`() {
        val taskDto = TaskDto(
            id = "task-42",
            projectId = "proj-1",
            title = "Implement AppCache",
            description = "Thread safe in-memory cache",
            priority = "URGENT",
            status = "IN_PROGRESS",
            skill = "Kotlin",
            dueDate = "2026-09-30",
            createdBy = "user-1",
            roadmapStepId = "step-3"
        )

        val domainTask = taskDto.toDomain()
        assertEquals("task-42", domainTask.id)
        assertEquals("Implement AppCache", domainTask.title)
        assertEquals("Thread safe in-memory cache", domainTask.description)
        assertEquals(TaskPriority.URGENT, domainTask.priority)
        assertEquals(TaskStatus.IN_PROGRESS, domainTask.status)
        assertTrue(domainTask.isInProgress)
        assertFalse(domainTask.isCompleted)
        assertTrue(domainTask.isUrgent)
        assertEquals("step-3", domainTask.roadmapStepId)

        val backDto = domainTask.toDto(projectId = "proj-1", createdBy = "user-1")
        assertEquals(taskDto.id, backDto.id)
        assertEquals(taskDto.title, backDto.title)
        assertEquals("URGENT", backDto.priority)
        assertEquals("IN_PROGRESS", backDto.status)
    }

    // ==========================================
    // APP CACHE REACTIVE LAYER TESTS
    // ==========================================

    @Test
    fun `AppCache stores and retrieves projects reactively`() {
        val project1 = Project(
            id = "p-1",
            name = "Scrap2Stack",
            description = "Reviving dead repos",
            technologies = listOf("Kotlin", "Jetpack Compose"),
            status = ProjectStatus.REVIVING,
            revivalScore = 92
        )
        val project2 = Project(
            id = "p-2",
            name = "Project Phoenix",
            description = "AI code generator",
            technologies = listOf("Python", "Gemini"),
            status = ProjectStatus.IDEA,
            revivalScore = 65
        )

        AppCache.saveProjects(listOf(project1, project2))

        assertEquals(2, AppCache.getAllProjects().size)
        assertEquals("Scrap2Stack", AppCache.getProject("p-1")?.name)
        assertEquals("Tier S (Exceptional)", AppCache.getProject("p-1")?.revivalTier)
        assertEquals(2, AppCache.projectsFlow.value.size)

        // Put an updated project
        val updatedP1 = project1.copy(name = "Scrap2Stack Ultimate")
        AppCache.putProject(updatedP1)
        assertEquals("Scrap2Stack Ultimate", AppCache.getProject("p-1")?.name)
        assertEquals("Scrap2Stack Ultimate", AppCache.projectsFlow.value.first().name)

        // Remove project
        AppCache.removeProject("p-2")
        assertNull(AppCache.getProject("p-2"))
        assertEquals(1, AppCache.projectsFlow.value.size)
    }

    @Test
    fun `AppCache manages tasks and reactive updates`() {
        val task = Task(
            id = "t-1",
            title = "Test Task",
            priority = TaskPriority.HIGH,
            status = TaskStatus.TODO
        )

        AppCache.saveTasks("proj-1", listOf(task))
        assertEquals(1, AppCache.getTasks("proj-1")?.size)
        assertEquals(TaskStatus.TODO, AppCache.getTasksFlow("proj-1").value.first().status)

        // Update task status optimistically
        AppCache.updateTaskStatus("t-1", TaskStatus.COMPLETED)
        assertEquals(TaskStatus.COMPLETED, AppCache.getTasks("proj-1")?.first()?.status)
        assertEquals(TaskStatus.COMPLETED, AppCache.getTasksFlow("proj-1").value.first().status)
    }

    @Test
    fun `AppCache tracks unread notifications accurately`() {
        val n1 = NotificationItem(id = "n-1", title = "Welcome", read = false)
        val n2 = NotificationItem(id = "n-2", title = "Task assigned", read = false)
        val n3 = NotificationItem(id = "n-3", title = "Project matched", read = true)

        AppCache.saveNotifications(listOf(n1, n2, n3))

        assertEquals(3, AppCache.getNotifications().size)
        assertEquals(2, AppCache.unreadCountFlow.value)

        // Mark one as read
        AppCache.markNotificationAsRead("n-1")
        assertEquals(1, AppCache.unreadCountFlow.value)
        assertTrue(AppCache.getNotifications().first { it.id == "n-1" }.read)
    }
}

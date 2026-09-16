package com.scrap2stack.app.data.local

import com.scrap2stack.app.domain.model.*

object MockData {
    data class Notification(
        val id: String,
        val message: String,
        val time: String,
        val isRead: Boolean
    )

    data class RoadmapPhase(
        val id: String,
        val title: String,
        val status: String,
        val progress: Float
    )

    data class GithubEvent(
        val type: String,
        val user: String,
        val description: String,
        val time: String
    )

    val currentDeveloper = Developer(
        id = "me",
        name = "John Developer",
        username = "johndev",
        bio = "Full-stack developer passionate about open source and reviving abandoned gems. AI enthusiast.",
        skills = listOf("Kotlin", "Go", "Compose", "React", "Docker", "Git"),
        interests = listOf("AI", "Open Source", "Productivity"),
        experienceLevel = ExperienceLevel.INTERMEDIATE,
        githubUsername = "johndev-ops",
        charms = 1280
    )

    val projects = listOf(
        Project(
            id = "1",
            name = "AI Crop Disease Detection",
            description = "A mobile application that uses machine learning to identify crop diseases from photos taken by farmers.",
            technologies = listOf("Python", "TensorFlow Lite", "Kotlin"),
            requiredSkills = listOf("Machine Learning", "Mobile Development", "Computer Vision", "UI/UX"),
            status = ProjectStatus.ABANDONED,
            revivalScore = 87,
            lastActivity = "2 years ago",
            teamSize = 2,
            problem = "The original developers graduated and lost access to the GPU cluster required for further training.",
            githubUrl = "https://github.com/sample/crop-ai"
        ),
        Project(
            id = "2",
            name = "Campus Lost & Found",
            description = "A localized platform for university students to report and find lost items within the campus premises.",
            technologies = listOf("React Native", "Firebase", "Node.js"),
            requiredSkills = listOf("Frontend", "Backend", "UI/UX Design"),
            status = ProjectStatus.INACTIVE,
            revivalScore = 65,
            lastActivity = "6 months ago",
            teamSize = 1,
            problem = "Lack of user engagement and marketing within the campus.",
            githubUrl = "https://github.com/sample/campus-found"
        ),
        Project(
            id = "3",
            name = "Open Source Developer Tool",
            description = "A CLI tool that helps developers automate the setup of local development environments for complex microservices.",
            technologies = listOf("Go", "Docker", "Bash"),
            requiredSkills = listOf("System Programming", "DevOps", "CLI Design"),
            status = ProjectStatus.REVIVING,
            revivalScore = 92,
            lastActivity = "2 days ago",
            teamSize = 4,
            problem = "Current maintainers need help refactoring the core configuration engine.",
            githubUrl = "https://github.com/sample/dev-tool"
        )
    )

    val developers = listOf(
        Developer(
            id = "dev1",
            name = "Alex Chen",
            username = "alexchen",
            bio = "Open source enthusiast and ML engineer.",
            skills = listOf("Python", "Machine Learning", "MongoDB", "TensorFlow"),
            interests = listOf("AI", "Open Source", "HealthTech"),
            experienceLevel = ExperienceLevel.ADVANCED,
            githubUsername = "alexchen88",
            charms = 1250,
            matchScore = 94,
            matchReason = "Strong ML skills match the project's core requirements."
        ),
        Developer(
            id = "dev2",
            name = "Sarah Miller",
            username = "sarahm",
            bio = "Full-stack developer with a focus on React and Node.js.",
            skills = listOf("React", "Node.js", "TypeScript", "AWS"),
            interests = listOf("Web Development", "UI/UX", "Cloud Computing"),
            experienceLevel = ExperienceLevel.INTERMEDIATE,
            githubUsername = "sarahcodes",
            charms = 850,
            matchScore = 88,
            matchReason = "Perfect fit for the frontend and backend integration needs."
        ),
        Developer(
            id = "dev3",
            name = "Michael Scott",
            username = "mscott",
            bio = "Product Manager turned developer. Loves building useful tools.",
            skills = listOf("Kotlin", "Android", "Firebase", "Product Management"),
            interests = listOf("Mobile Apps", "Productivity", "Startups"),
            experienceLevel = ExperienceLevel.INTERMEDIATE,
            githubUsername = "mscott-dev",
            charms = 600,
            matchScore = 81,
            matchReason = "Previous experience in productivity apps is a great asset."
        )
    )

    val tasks = listOf(
        Task(
            id = "task1",
            title = "Refactor ML Inference Logic",
            assignee = "Alex Chen",
            priority = TaskPriority.HIGH,
            skill = "Machine Learning",
            status = TaskStatus.IN_PROGRESS,
            dueDate = "2023-12-20"
        ),
        Task(
            id = "task2",
            title = "Implement User Authentication",
            assignee = "Sarah Miller",
            priority = TaskPriority.MEDIUM,
            skill = "Backend",
            status = TaskStatus.TODO,
            dueDate = "2023-12-25"
        ),
        Task(
            id = "task3",
            title = "Design New Dashboard UI",
            assignee = null,
            priority = TaskPriority.MEDIUM,
            skill = "UI/UX",
            status = TaskStatus.TODO,
            dueDate = "2023-12-30"
        ),
        Task(
            id = "task4",
            title = "Bug Fix: Null Pointer in Data Loader",
            assignee = "Alex Chen",
            priority = TaskPriority.URGENT,
            skill = "Python",
            status = TaskStatus.REVIEW,
            dueDate = "2023-12-18"
        )
    )

    val roadmap = listOf(
        RoadmapPhase("Phase 1", "Project Setup", "Completed", 1.0f),
        RoadmapPhase("Phase 2", "Backend Development", "In Progress", 0.6f),
        RoadmapPhase("Phase 3", "Android App", "In Progress", 0.3f),
        RoadmapPhase("Phase 4", "AI Integration", "Pending", 0.0f),
        RoadmapPhase("Phase 5", "Testing", "Pending", 0.0f),
        RoadmapPhase("Phase 6", "MVP Release", "Pending", 0.0f)
    )

    val githubActivity = listOf(
        GithubEvent("Commit", "Alex Chen", "Refactored ML model", "2 hours ago"),
        GithubEvent("Pull Request", "Sarah Miller", "Added Auth API", "5 hours ago"),
        GithubEvent("Issue", "johndev", "Broken build on Linux", "1 day ago"),
        GithubEvent("Release", "System", "v0.1-alpha", "1 week ago")
    )

    val notifications = listOf(
        Notification("1", "Alex accepted your collaboration request.", "2 hours ago", false),
        Notification("2", "New task assigned to you in 'Dev Tool'.", "5 hours ago", false),
        Notification("3", "ScrapAI analysis is ready for AI Crop Disease Detection.", "1 day ago", true),
        Notification("4", "Your project 'Campus Lost & Found' reached 70% completion.", "2 days ago", true),
        Notification("5", "GitHub contribution detected in 'Dev Tool CLI'.", "3 days ago", true)
    )
}

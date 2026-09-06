package com.scrap2stack.app.data.local

import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.model.ProjectStatus

object SampleData {
    val projects = listOf(
        Project(
            id = "1",
            name = "AI Crop Disease Detection",
            description = "A mobile application that uses machine learning to identify crop diseases from photos taken by farmers.",
            technologies = listOf("Python", "TensorFlow Lite", "Kotlin"),
            requiredSkills = listOf("Machine Learning", "Mobile Development", "Computer Vision"),
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
        ),
        Project(
            id = "4",
            name = "Student Productivity Platform",
            description = "An all-in-one workspace for students to manage assignments, notes, and study groups.",
            technologies = listOf("Flutter", "Supabase"),
            requiredSkills = listOf("Mobile Development", "Product Management"),
            status = ProjectStatus.ABANDONED,
            revivalScore = 45,
            lastActivity = "1 year ago",
            teamSize = 0,
            problem = "Initial MVP was too bloated and the project lost direction.",
            githubUrl = "https://github.com/sample/student-stack"
        ),
        Project(
            id = "5",
            name = "Community Learning Platform",
            description = "A peer-to-peer learning platform where community members can share skills through short workshops.",
            technologies = listOf("Ruby on Rails", "PostgreSQL"),
            requiredSkills = listOf("Fullstack Development", "Community Management"),
            status = ProjectStatus.INACTIVE,
            revivalScore = 78,
            lastActivity = "4 months ago",
            teamSize = 3,
            problem = "Funding ran out for server costs, and the original team disbanded.",
            githubUrl = "https://github.com/sample/learn-peer"
        )
    )
}

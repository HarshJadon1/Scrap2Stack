package com.scrap2stack.app.domain.service

import com.scrap2stack.app.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchingEngineTest {

    private val matchingEngine = MatchingEngine()

    private val sampleProject = Project(
        id = "proj1",
        name = "Test Project",
        description = "A project using Kotlin and Android for HealthTech",
        technologies = listOf("Kotlin", "Android", "PostgreSQL"),
        requiredSkills = listOf("Kotlin", "Android", "UI/UX"),
        status = ProjectStatus.ABANDONED,
        problem = "Abandoned due to lack of time"
    )

    @Test
    fun `exact skill match produces strong score`() {
        val developer = Developer(
            id = "dev1",
            name = "Perfect Dev",
            username = "perfect",
            bio = "I know Kotlin, Android and UI/UX",
            skills = listOf("Kotlin", "Android", "UI/UX"),
            interests = listOf("HealthTech"),
            experienceLevel = ExperienceLevel.ADVANCED
        )

        val match = matchingEngine.calculateMatch(sampleProject, developer)

        assertTrue("Score should be very high for perfect match", match.score >= 90)
        assertEquals(3, match.matchedSkills.size)
    }

    @Test
    fun `partial skill overlap produces lower score`() {
        val developer = Developer(
            id = "dev2",
            name = "Partial Dev",
            username = "partial",
            bio = "I know Kotlin",
            skills = listOf("Kotlin"),
            interests = listOf("Gaming"),
            experienceLevel = ExperienceLevel.BEGINNER
        )

        val match = matchingEngine.calculateMatch(sampleProject, developer)

        assertTrue("Score should be moderate", match.score in 30..60)
        assertEquals(1, match.matchedSkills.size)
    }

    @Test
    fun `no skill overlap produces low score`() {
        val developer = Developer(
            id = "dev3",
            name = "Wrong Dev",
            username = "wrong",
            bio = "I only know Python and React",
            skills = listOf("Python", "React"),
            interests = listOf("FinTech"),
            experienceLevel = ExperienceLevel.BEGINNER
        )

        val match = matchingEngine.calculateMatch(sampleProject, developer)

        assertTrue("Score should be low", match.score < 30)
        assertTrue(match.matchedSkills.isEmpty())
    }

    @Test
    fun `normalized score is always between 0 and 100`() {
        val developers = listOf(
            Developer(id = "1", name = "A", username = "a", bio = "", skills = emptyList(), interests = emptyList()),
            Developer(id = "2", name = "B", username = "b", bio = "", skills = listOf("Kotlin", "Android", "UI/UX", "PostgreSQL", "Firebase"), interests = listOf("HealthTech", "AI", "ML"), experienceLevel = ExperienceLevel.ADVANCED)
        )

        developers.forEach { dev ->
            val match = matchingEngine.calculateMatch(sampleProject, dev)
            assertTrue("Score ${match.score} should be within 0-100", match.score in 0..100)
        }
    }
}

package com.scrap2stack.app.feature.profile

import com.scrap2stack.app.domain.model.CharmContribution
import com.scrap2stack.app.domain.model.ContributionType
import com.scrap2stack.app.domain.model.Developer
import com.scrap2stack.app.domain.model.ExperienceLevel
import com.scrap2stack.app.domain.service.ContributionVerificationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ProfileHeatmapAndConfidenceTest {

    private val utcFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private fun contributionOn(date: String, id: String): CharmContribution {
        return CharmContribution(
            id = id,
            userId = "u1",
            projectId = "p1",
            contributionType = ContributionType.TASK_COMPLETED,
            charms = 10,
            description = "Task",
            referenceId = null,
            createdAt = "${date}T12:00:00Z"
        )
    }

    @Test
    fun `buildHeatmapLevels returns 21 days bucketed from 0 to 4`() {
        val anchorMillis = utcFormat.parse("2026-11-25")!!.time
        val contributions = listOf(
            contributionOn("2026-11-25", "1"), // today -> 1
            contributionOn("2026-11-24", "2"),
            contributionOn("2026-11-24", "3"), // yesterday -> 2
            contributionOn("2026-11-23", "4"),
            contributionOn("2026-11-23", "5"),
            contributionOn("2026-11-23", "6"), // 3 -> level 3
            contributionOn("2026-11-20", "7"),
            contributionOn("2026-11-20", "8"),
            contributionOn("2026-11-20", "9"),
            contributionOn("2026-11-20", "10"),
            contributionOn("2026-11-20", "11"), // 5+ -> level 4
            contributionOn("invalid-date", "bad")
        )

        val levels = buildHeatmapLevels(
            contributions = contributions,
            days = 21,
            todayMillis = anchorMillis
        )

        assertEquals(21, levels.size)
        assertEquals(1, levels[20]) // 2026-11-25
        assertEquals(2, levels[19]) // 2026-11-24
        assertEquals(3, levels[18]) // 2026-11-23
        assertEquals(0, levels[17]) // 2026-11-22
        assertEquals(4, levels[15]) // 2026-11-20
    }

    @Test
    fun `buildHeatmapLevels handles non positive day count safely`() {
        assertEquals(emptyList<Int>(), buildHeatmapLevels(emptyList(), days = 0))
    }

    @Test
    fun `skill confidence engine scores verified developer skills inside 0 to 1`() {
        val dev = Developer(
            id = "u1",
            name = "Harsh Jadon",
            skills = listOf("Kotlin", "Compose"),
            experienceLevel = ExperienceLevel.ADVANCED,
            charms = 240,
            githubUrl = "https://github.com/HarshJadon1"
        )
        val metric = ContributionVerificationEngine.getSkillConfidence(dev, "Kotlin")
        assertTrue(metric.confidence in 0.0..1.0)
        assertEquals("Kotlin", metric.skill)
    }
}

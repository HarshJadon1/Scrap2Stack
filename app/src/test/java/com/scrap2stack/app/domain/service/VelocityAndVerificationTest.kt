package com.scrap2stack.app.domain.service

import com.scrap2stack.app.domain.model.Developer
import com.scrap2stack.app.domain.model.ExperienceLevel
import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.model.Task
import com.scrap2stack.app.domain.model.TaskPriority
import com.scrap2stack.app.domain.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VelocityAndVerificationTest {

    @Test
    fun `story point mapping adheres to priority weighting model`() {
        assertEquals(4, RevivalVelocityEngine.getStoryPoints(TaskPriority.URGENT))
        assertEquals(3, RevivalVelocityEngine.getStoryPoints(TaskPriority.HIGH))
        assertEquals(2, RevivalVelocityEngine.getStoryPoints(TaskPriority.MEDIUM))
        assertEquals(1, RevivalVelocityEngine.getStoryPoints(TaskPriority.LOW))
    }

    @Test
    fun `velocity engine returns sensible defaults for empty task roadmap`() {
        val metrics = RevivalVelocityEngine.calculateMetrics(tasks = emptyList())

        assertEquals(0, metrics.totalStoryPoints)
        assertEquals(0, metrics.completedStoryPoints)
        assertEquals(0, metrics.progressPercent)
        assertEquals(ProjectHealthStatus.ON_TRACK, metrics.healthStatus)
        assertEquals("Planning Phase", metrics.healthBadgeLabel)
        assertNotNull(metrics.bottleneckInsight)
        assertTrue(metrics.estimatedWeeksToShip > 0.0)
    }

    @Test
    fun `velocity engine calculates points and estimated burnup time accurately`() {
        val tasks = listOf(
            Task(id = "1", title = "Setup CI", priority = TaskPriority.URGENT, status = TaskStatus.COMPLETED),
            Task(id = "2", title = "Write Data Models", priority = TaskPriority.HIGH, status = TaskStatus.COMPLETED),
            Task(id = "3", title = "Build UI", priority = TaskPriority.MEDIUM, status = TaskStatus.IN_PROGRESS),
            Task(id = "4", title = "Write Docs", priority = TaskPriority.LOW, status = TaskStatus.TODO)
        )
        // Total = 4 + 3 + 2 + 1 = 10
        // Completed = 4 + 3 = 7
        // Progress = 70%
        val project = Project(id = "p1", name = "Test Stack", description = "A test project", teamSize = 2)
        val metrics = RevivalVelocityEngine.calculateMetrics(tasks = tasks, project = project)

        assertEquals(10, metrics.totalStoryPoints)
        assertEquals(7, metrics.completedStoryPoints)
        assertEquals(2, metrics.inProgressStoryPoints)
        assertEquals(70, metrics.progressPercent)
        assertTrue("Weekly velocity should be greater than zero", metrics.weeklyVelocityStoryPoints > 0.0)
        assertTrue("Estimated weeks should be realistic", metrics.estimatedWeeksToShip in 0.5..8.0)
        assertTrue("CI lower should be less than CI upper", metrics.confidenceIntervalWeeks.first <= metrics.confidenceIntervalWeeks.second)
    }

    @Test
    fun `velocity engine detects bottleneck when multiple urgent tasks are blocked`() {
        val tasks = listOf(
            Task(id = "1", title = "Fix Crash", priority = TaskPriority.URGENT, status = TaskStatus.TODO),
            Task(id = "2", title = "Fix Security Vulnerability", priority = TaskPriority.URGENT, status = TaskStatus.IN_PROGRESS),
            Task(id = "3", title = "Add Feature", priority = TaskPriority.LOW, status = TaskStatus.TODO)
        )

        val metrics = RevivalVelocityEngine.calculateMetrics(tasks = tasks)
        assertEquals(ProjectHealthStatus.AT_RISK_BOTTLENECK, metrics.healthStatus)
        assertEquals("Bottleneck Detected", metrics.healthBadgeLabel)
        assertTrue(metrics.bottleneckInsight!!.contains("urgent tasks"))
    }

    @Test
    fun `velocity engine detects stalled status when parallel WIP is high with zero completions`() {
        val tasks = listOf(
            Task(id = "1", title = "Task 1", priority = TaskPriority.MEDIUM, status = TaskStatus.IN_PROGRESS),
            Task(id = "2", title = "Task 2", priority = TaskPriority.MEDIUM, status = TaskStatus.IN_PROGRESS),
            Task(id = "3", title = "Task 3", priority = TaskPriority.MEDIUM, status = TaskStatus.REVIEW)
        )

        val metrics = RevivalVelocityEngine.calculateMetrics(tasks = tasks)
        assertEquals(ProjectHealthStatus.STALLED, metrics.healthStatus)
        assertEquals("Attention Required", metrics.healthBadgeLabel)
        assertTrue(metrics.bottleneckInsight!!.contains("parallel WIP"))
    }

    @Test
    fun `velocity engine shows zero remaining effort and 100 percent when shipped`() {
        val tasks = listOf(
            Task(id = "1", title = "Task 1", priority = TaskPriority.HIGH, status = TaskStatus.COMPLETED)
        )

        val metrics = RevivalVelocityEngine.calculateMetrics(tasks = tasks, isShipped = true)
        assertEquals(0.0, metrics.estimatedWeeksToShip, 0.001)
        assertEquals(100, metrics.progressPercent)
        assertEquals(ProjectHealthStatus.ACCELERATING, metrics.healthStatus)
        assertEquals("Shipped & Completed", metrics.healthBadgeLabel)
    }

    @Test
    fun `diff weighted contribution scoring scales logarithmically and enforces bounds`() {
        // Small commit
        val smallScore = ContributionVerificationEngine.calculateDiffWeightedCharms(linesAdded = 10, linesDeleted = 5, type = "COMMIT")
        assertTrue("Small commit charms should be >= 10", smallScore >= 10)

        // Pull request with substantial code changes
        val prScore = ContributionVerificationEngine.calculateDiffWeightedCharms(linesAdded = 300, linesDeleted = 100, type = "PULL_REQUEST", filesChanged = 5)
        assertTrue("Substantial PR charms should be higher than small commit", prScore > smallScore)

        // Huge refactor (e.g. 50,000 lines generated/vendored) should not blow up beyond upper cap
        val massiveScore = ContributionVerificationEngine.calculateDiffWeightedCharms(linesAdded = 50000, linesDeleted = 20000, type = "COMMIT", filesChanged = 50)
        assertTrue("Massive diffs should be capped at 55", massiveScore <= 55)
    }

    @Test
    fun `anomaly detector flags suspicious rapid bursts and empty spam`() {
        // Empty commit spam
        val emptyStatus = ContributionVerificationEngine.detectAnomaly(
            recentContributionsCount = 3,
            timeWindowSeconds = 120,
            totalLinesChanged = 0
        )
        assertEquals(AnomalyStatus.EMPTY_COMMIT_SPAM, emptyStatus)

        // Suspicious burst: 12 commits in 100 seconds
        val burstStatus = ContributionVerificationEngine.detectAnomaly(
            recentContributionsCount = 12,
            timeWindowSeconds = 100,
            totalLinesChanged = 150
        )
        assertEquals(AnomalyStatus.SUSPICIOUS_RAPID_BURST, burstStatus)

        // Collusion risk: 30 commits in 5 minutes
        val collusionStatus = ContributionVerificationEngine.detectAnomaly(
            recentContributionsCount = 30,
            timeWindowSeconds = 300,
            totalLinesChanged = 1200
        )
        assertEquals(AnomalyStatus.COLLUSION_RISK, collusionStatus)

        // Clean normal development
        val cleanStatus = ContributionVerificationEngine.detectAnomaly(
            recentContributionsCount = 4,
            timeWindowSeconds = 3600,
            totalLinesChanged = 450
        )
        assertEquals(AnomalyStatus.CLEAN, cleanStatus)
    }

    @Test
    fun `bayesian skill confidence scales with verified code activity`() {
        // Unverified developer who lacks the skill entirely
        val noviceDev = Developer(
            id = "d1",
            name = "Novice",
            skills = listOf("HTML", "CSS"),
            charms = 0
        )
        val unverifiedMetric = ContributionVerificationEngine.getSkillConfidence(noviceDev, "Kotlin")
        assertEquals(0.10, unverifiedMetric.confidence, 0.01)
        assertFalse(unverifiedMetric.isVerified)
        assertEquals("Unverified", unverifiedMetric.confidenceLevelLabel)

        // Self-reported developer without verified GitHub or charms
        val selfReportedDev = Developer(
            id = "d2",
            name = "Self Reported",
            skills = listOf("Kotlin", "Android"),
            githubUrl = "",
            githubUsername = "",
            charms = 0
        )
        val selfReportedMetric = ContributionVerificationEngine.getSkillConfidence(selfReportedDev, "Kotlin")
        assertEquals(0.25, selfReportedMetric.confidence, 0.01)
        assertFalse(selfReportedMetric.isVerified)
        assertEquals("Self-Reported", selfReportedMetric.confidenceLevelLabel)

        // Active verified contributor with charms and GitHub
        val activeDev = Developer(
            id = "d3",
            name = "Active Pro",
            skills = listOf("Kotlin", "Jetpack Compose"),
            githubUsername = "activepro",
            charms = 600,
            experienceLevel = ExperienceLevel.ADVANCED
        )
        val verifiedMetric = ContributionVerificationEngine.getSkillConfidence(activeDev, "Kotlin")
        assertTrue("Active dev confidence should exceed 0.85", verifiedMetric.confidence >= 0.85)
        assertTrue("Active dev should be marked as verified", verifiedMetric.isVerified)
        assertEquals("Verified Master", verifiedMetric.confidenceLevelLabel)
    }
}

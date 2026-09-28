package com.scrap2stack.app.domain.service

import com.scrap2stack.app.domain.model.Developer
import com.scrap2stack.app.domain.model.ExperienceLevel
import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.model.ProjectStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DataScienceEngineTest {

    private val matchingEngine = MatchingEngine()

    @Test
    fun `skill taxonomy correctly normalizes canonical terms and synonyms`() {
        assertEquals("react", SkillTaxonomyEngine.normalize("React.js"))
        assertEquals("react", SkillTaxonomyEngine.normalize("reactjs"))
        assertEquals("jetpack compose", SkillTaxonomyEngine.normalize("compose"))
        assertEquals("postgresql", SkillTaxonomyEngine.normalize("postgres"))
        assertEquals("postgresql", SkillTaxonomyEngine.normalize("psql"))
        assertEquals("go", SkillTaxonomyEngine.normalize("golang"))
        assertEquals("python", SkillTaxonomyEngine.normalize("py"))
        assertEquals("kotlin", SkillTaxonomyEngine.normalize("Kotlin Developer"))
        assertEquals("ui design", SkillTaxonomyEngine.normalize("UI/UX Designer"))
    }

    @Test
    fun `skill taxonomy produces high similarity for intra-cluster and token overlaps`() {
        val composeAndroidSim = SkillTaxonomyEngine.calculateSkillSimilarity("Jetpack Compose", "Android")
        assertTrue("Compose and Android should have strong cluster affinity (>= 0.40)", composeAndroidSim >= 0.40)

        val kotlinDevSim = SkillTaxonomyEngine.calculateSkillSimilarity("Kotlin Developer", "Kotlin")
        assertTrue("Kotlin Developer and Kotlin should have very high similarity (>= 0.85)", kotlinDevSim >= 0.85)

        val unrelatedSim = SkillTaxonomyEngine.calculateSkillSimilarity("Kubernetes", "React")
        assertTrue("Unrelated technologies should have low similarity (<= 0.20)", unrelatedSim <= 0.20)
    }

    @Test
    fun `matching engine rewards semantic skill and technology alignment`() {
        val project = Project(
            id = "proj-ds-1",
            name = "Smart Scrap Analyzer",
            description = "Android mobile app leveraging Kotlin, Jetpack Compose, and Supabase",
            technologies = listOf("Kotlin", "Jetpack Compose", "Supabase"),
            requiredSkills = listOf("Kotlin Developer", "Android UI", "Database Architecture"),
            status = ProjectStatus.REVIVING,
            problem = "Project abandoned due to lack of Android Compose maintainers",
            category = "Mobile"
        )

        // Developer with slightly differently phrased skills
        val dev = Developer(
            id = "dev-ds-1",
            name = "Semantic Dev",
            username = "semantic_pro",
            skills = listOf("Kotlin", "Android", "Supabase", "PostgreSQL"),
            interests = listOf("Mobile", "Android UI"),
            experienceLevel = ExperienceLevel.ADVANCED,
            charms = 250
        )

        val match = matchingEngine.calculateMatch(project, dev)

        assertTrue("Fuzzy semantic matching should give a strong score >= 78", match.score >= 78)
        assertTrue("Matched skills list should not be empty", match.matchedSkills.isNotEmpty())
        assertTrue("Breakdown scores should all be within [0, 100]", match.scoreBreakdown.skillScore in 0.0..100.0)
    }

    @Test
    fun `scientifically grounded revival model produces valid dimensional breakdown`() {
        val wellDocumentedProject = Project(
            id = "proj-good",
            name = "HealthTech Sync",
            description = "Production grade health tracker application built with modern architecture and modular components",
            technologies = listOf("Kotlin", "Jetpack Compose", "Supabase", "Ktor"),
            requiredSkills = listOf("Kotlin", "Android", "Backend"),
            status = ProjectStatus.REVIVING,
            problem = "Core maintainer relocated and was unable to dedicate time to the release pipeline and integration tests.",
            category = "Mobile",
            githubUrl = "https://github.com/health/sync",
            githubConnected = true
        )

        val breakdown = ScrapAIEngine.calculateRevivalBreakdown(wellDocumentedProject)

        assertTrue("Overall score should be high for well documented modern project", breakdown.overallScore >= 75)
        assertTrue("Architecture score should be >= 70", breakdown.architectureScore >= 70)
        assertTrue("Documentation score should be >= 60", breakdown.documentationScore >= 60)
        assertTrue("Market relevance score should be >= 75", breakdown.marketRelevanceScore >= 75)
        assertTrue("Skill availability score should be >= 60", breakdown.skillAvailabilityScore >= 60)
        assertTrue("Percentile rank should be in 50..99", breakdown.percentileRank in 50..99)
        assertTrue("Effort weeks should be positive", breakdown.estimatedEffortWeeks > 0.0)
        assertTrue("Confidence interval bounds must be valid", breakdown.effortConfidenceInterval.first < breakdown.effortConfidenceInterval.second)
    }

    @Test
    fun `sparse project receives lower documentation and architecture scores`() {
        val sparseProject = Project(
            id = "proj-sparse",
            name = "Old Script",
            description = "Some code",
            technologies = listOf("Legacy Tool"),
            requiredSkills = emptyList(),
            status = ProjectStatus.ABANDONED,
            problem = "",
            category = ""
        )

        val breakdown = ScrapAIEngine.calculateRevivalBreakdown(sparseProject)

        assertTrue("Sparse project should have modest overall score", breakdown.overallScore in 40..75)
        assertTrue("Documentation score should reflect lack of problem statement", breakdown.documentationScore <= 60)
    }
}

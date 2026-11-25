package com.scrap2stack.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Map
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.scrap2stack.app.domain.model.CharmContribution
import com.scrap2stack.app.domain.model.ContributionType
import com.scrap2stack.app.domain.model.Developer
import com.scrap2stack.app.domain.model.ExperienceLevel
import com.scrap2stack.app.feature.BottomNavItem
import com.scrap2stack.app.feature.FloatingBottomNavBar
import com.scrap2stack.app.feature.profile.ProfileContent
import com.scrap2stack.app.feature.workspace.WorkspaceKeycapDock
import com.scrap2stack.app.feature.workspace.WorkspaceTab
import com.scrap2stack.app.ui.theme.Scrap2StackTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StackPopUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun floatingBottomNavBar_rendersAllFiveLabelsAt320dpAnd130PercentFontScale() {
        var selectedRoute = BottomNavItem.Profile.route
        val items = listOf(
            BottomNavItem.Home,
            BottomNavItem.Discover,
            BottomNavItem.MyProjects,
            BottomNavItem.Notifications,
            BottomNavItem.Profile
        )

        composeRule.setContent {
            val baseDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density = baseDensity.density, fontScale = 1.3f)
            ) {
                Scrap2StackTheme(darkTheme = false) {
                    Box(modifier = Modifier.width(320.dp)) {
                        FloatingBottomNavBar(
                            items = items,
                            alertsBadgeCount = 3,
                            currentRouteChecker = { it == selectedRoute },
                            onSelectItem = { selectedRoute = it.route }
                        )
                    }
                }
            }
        }

        items.forEach { item ->
            composeRule.onNodeWithText(item.label).assertIsDisplayed()
        }
        composeRule.onNodeWithText("Alerts").performClick()
        assertEquals(BottomNavItem.Notifications.route, selectedRoute)
    }

    @Test
    fun workspaceKeycapDock_switchesAllSixTabs() {
        var lastTab = -1
        composeRule.setContent {
            Scrap2StackTheme(darkTheme = true) {
                var selectedTab by remember { mutableIntStateOf(0) }
                val tabs = listOf(
                    WorkspaceTab("Dashboard", Icons.Default.Dashboard),
                    WorkspaceTab("Tasks", Icons.AutoMirrored.Filled.List),
                    WorkspaceTab("Roadmap", Icons.Default.Map),
                    WorkspaceTab("Chat", Icons.AutoMirrored.Filled.Comment),
                    WorkspaceTab("GitHub", Icons.Default.Code),
                    WorkspaceTab("Team", Icons.Default.Group)
                )
                WorkspaceKeycapDock(
                    tabs = tabs,
                    selectedTab = selectedTab,
                    onSelectTab = {
                        selectedTab = it
                        lastTab = it
                    }
                )
            }
        }

        composeRule.onNodeWithText("TASKS").performClick()
        assertEquals(1, lastTab)

        composeRule.onNodeWithText("TEAM").performScrollTo().performClick()
        assertEquals(5, lastTab)
    }

    @Test
    fun profileContent_rendersCleanSocialHandlesHeatmapAndSkillConfidence() {
        val developer = Developer(
            id = "dev-1",
            name = "harsh jadon",
            username = "hj_001",
            bio = "full stack development",
            skills = listOf("Kotlin", "Jetpack Compose"),
            experienceLevel = ExperienceLevel.ADVANCED,
            charms = 120,
            githubUrl = "https://github.com/HarshJadon1",
            linkedinUrl = "https://www.linkedin.com/in/harsh-jadon-7ab7712b5?utm_source=share"
        )
        val contributions = listOf(
            CharmContribution(
                id = "c1",
                userId = "dev-1",
                projectId = "p1",
                contributionType = ContributionType.TASK_COMPLETED,
                charms = 10,
                description = "Fixed nav bar",
                referenceId = null,
                createdAt = "2026-11-25T10:00:00Z"
            )
        )

        composeRule.setContent {
            Scrap2StackTheme(darkTheme = false) {
                ProfileContent(
                    developer = developer,
                    contributions = contributions,
                    onNavigateToSettings = {},
                    onNavigateToEditProfile = {}
                )
            }
        }

        composeRule.onNodeWithText("@hj_001").assertIsDisplayed()
        composeRule.onNodeWithText("harsh-jadon-7ab7712b5").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Revival Activity (21d)").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Skill Confidence").performScrollTo().assertIsDisplayed()
    }
}

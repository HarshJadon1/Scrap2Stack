package com.scrap2stack.app.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrap2stack.app.core.ui.components.Scrap2StackButton
import com.scrap2stack.app.core.ui.components.Scrap2StackOutlinedButton
import com.scrap2stack.app.feature.auth.AuthViewModel
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String
)

val pages = listOf(
    OnboardingPage(
        "Revive Ideas",
        "Discover abandoned and unfinished projects that still have the potential to become something useful."
    ),
    OnboardingPage(
        "Find the Right Team",
        "AI matches projects with developers based on skills, interests and experience."
    ),
    OnboardingPage(
        "Build & Ship",
        "Collaborate, complete tasks, earn Charms and bring projects back to life."
    )
)

@Composable
fun OnboardingScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { pageIndex ->
            val page = pages[pageIndex]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Branding/Illustration placeholder
                Text(
                    text = "SCRAP2STACK",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp
                    )
                )

                Spacer(modifier = Modifier.height(64.dp))

                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Page Indicator
        Row(
            modifier = Modifier.padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pages.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(if (pagerState.currentPage == iteration) 12.dp else 8.dp)
                        .background(color, MaterialTheme.shapes.extraLarge)
                )
            }
        }

        if (pagerState.currentPage < pages.size - 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Scrap2StackOutlinedButton(
                    text = "Skip",
                    onClick = {
                        scope.launch {
                            viewModel.setOnboardingCompleted(true)
                            onNavigateToLogin()
                        }
                    },
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                Scrap2StackButton(
                    text = "Next",
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
            }
        } else {
            Scrap2StackButton(
                text = "Get Started",
                onClick = {
                    scope.launch {
                        viewModel.setOnboardingCompleted(true)
                        onNavigateToRegister()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

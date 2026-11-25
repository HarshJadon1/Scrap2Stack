package com.scrap2stack.app.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrap2stack.app.core.ui.components.BrutalHeaderTag
import com.scrap2stack.app.core.ui.components.Scrap2StackButton
import com.scrap2stack.app.core.ui.components.Scrap2StackOutlinedButton
import com.scrap2stack.app.core.ui.components.brutalCard
import com.scrap2stack.app.core.ui.components.dotGridBackground
import com.scrap2stack.app.feature.auth.AuthViewModel
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.brutalColors
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
    val brutal = MaterialTheme.brutalColors
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brutal.canvas)
            .dotGridBackground()
            .systemBarsPadding()
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
                Box(
                    modifier = Modifier
                        .brutalCard(
                            cornerRadius = 14.dp,
                            containerColor = ElectricMint,
                            borderColor = brutal.border,
                            borderWidth = 2.dp,
                            shadowColor = BrutalShadowBlack,
                            shadowOffsetX = 4.dp,
                            shadowOffsetY = 4.dp
                        )
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "SCRAP2STACK",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = BrutalInkBlack,
                            letterSpacing = 2.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .brutalCard(
                            cornerRadius = 18.dp,
                            containerColor = brutal.surface,
                            borderColor = brutal.border,
                            borderWidth = 2.dp,
                            shadowColor = PrimaryIndigo,
                            shadowOffsetX = 5.dp,
                            shadowOffsetY = 5.dp
                        )
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        BrutalHeaderTag(text = "STEP 0${pageIndex + 1} / 0${pages.size}")
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = page.title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = brutal.textPrimary
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = page.description,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = brutal.textMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Stack Pop Page Indicator
        Row(
            modifier = Modifier.padding(bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(pages.size) { iteration ->
                val isSelected = pagerState.currentPage == iteration
                Box(
                    modifier = Modifier
                        .width(if (isSelected) 28.dp else 10.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) ElectricMint else brutal.surfaceRaised)
                        .border(1.5.dp, brutal.border, RoundedCornerShape(4.dp))
                )
            }
        }

        if (pagerState.currentPage < pages.size - 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Scrap2StackOutlinedButton(
                    text = "SKIP",
                    onClick = {
                        scope.launch {
                            viewModel.setOnboardingCompleted(true)
                            onNavigateToLogin()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                Scrap2StackButton(
                    text = "NEXT",
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Scrap2StackButton(
                text = "GET STARTED",
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

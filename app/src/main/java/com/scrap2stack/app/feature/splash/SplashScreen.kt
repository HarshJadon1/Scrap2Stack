package com.scrap2stack.app.feature.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrap2stack.app.core.ui.components.brutalCard
import com.scrap2stack.app.core.ui.components.dotGridBackground
import com.scrap2stack.app.feature.auth.AuthViewModel
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.brutalColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    authViewModel: AuthViewModel,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    LaunchedEffect(key1 = true) {
        val isOnboardingCompleted = authViewModel.isOnboardingCompleted().first()
        val isLoggedIn = authViewModel.isUserLoggedIn()

        delay(2000) // Branding delay

        when {
            isLoggedIn -> onNavigateToMain()
            !isOnboardingCompleted -> onNavigateToOnboarding()
            else -> onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brutal.canvas)
            .dotGridBackground(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .brutalCard(
                        cornerRadius = 18.dp,
                        containerColor = ElectricMint,
                        borderColor = brutal.border,
                        borderWidth = 2.5.dp,
                        shadowColor = BrutalShadowBlack,
                        shadowOffsetX = 6.dp,
                        shadowOffsetY = 6.dp
                    )
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "SCRAP2STACK",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp,
                        color = BrutalInkBlack
                    )
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Give abandoned projects a second chance.",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = brutal.textMuted
            )
        }
    }
}

package com.scrap2stack.app.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.BrutalTopBar
import com.scrap2stack.app.core.ui.components.LoadingView
import com.scrap2stack.app.core.ui.components.Scrap2StackButton
import com.scrap2stack.app.core.ui.components.brutalCard
import com.scrap2stack.app.core.ui.components.brutalTextFieldColors
import com.scrap2stack.app.core.ui.components.dotGridBackground
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.StatusAbandoned
import com.scrap2stack.app.ui.theme.brutalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    val fieldColors = brutalTextFieldColors()
    var email by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()
    var isSubmitted by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success && !isSubmitted) {
            isSubmitted = true
        }
    }

    Scaffold(
        topBar = {
            BrutalTopBar(
                tag = "AUTH // RECOVERY",
                title = "Reset Password",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = brutal.canvas
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .dotGridBackground()
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .brutalCard(
                        cornerRadius = 18.dp,
                        containerColor = brutal.surface,
                        borderColor = brutal.border,
                        borderWidth = 2.dp,
                        shadowColor = brutal.shadow,
                        shadowOffsetX = 5.dp,
                        shadowOffsetY = 5.dp
                    )
                    .padding(22.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    if (!isSubmitted) {
                        Text(
                            text = "Forgot Password?",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = brutal.textPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Enter the email address associated with your account and we'll send you a link to reset your password.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = brutal.textMuted,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (authState is AuthState.Error) {
                            Text(
                                text = (authState as AuthState.Error).message,
                                color = StatusAbandoned,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(bottom = 14.dp)
                            )
                        }

                        Scrap2StackButton(
                            text = "SEND RESET LINK",
                            onClick = { viewModel.forgotPassword(email) },
                            enabled = email.isNotBlank() && authState !is AuthState.Loading
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .brutalCard(
                                    cornerRadius = 16.dp,
                                    containerColor = ElectricMint,
                                    borderColor = brutal.border,
                                    borderWidth = 2.dp,
                                    shadowColor = BrutalShadowBlack,
                                    shadowOffsetX = 3.dp,
                                    shadowOffsetY = 3.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(38.dp),
                                tint = brutal.textPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Check your email",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                            color = brutal.textPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "We have sent a password recovery link to:\n$email",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = brutal.textMuted
                        )
                        Spacer(modifier = Modifier.height(28.dp))
                        Scrap2StackButton(
                            text = "RETURN TO LOGIN",
                            onClick = {
                                viewModel.resetState()
                                onNavigateBack()
                            }
                        )
                    }
                }
            }

            if (authState is AuthState.Loading) {
                LoadingView(modifier = Modifier.fillMaxSize().background(brutal.canvas.copy(alpha = 0.7f)))
            }
        }
    }
}

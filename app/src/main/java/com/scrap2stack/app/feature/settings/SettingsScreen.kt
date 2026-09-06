package com.scrap2stack.app.feature.settings

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * SettingsScreen provides configuration options for the application.
 *
 * Performance Analysis & Optimizations:
 * 1. Lazy Loading: Replaced 'Column' with 'verticalScroll' with 'LazyColumn'. For lists of items,
 *    LazyColumn is more efficient as it only composes and lays out items currently visible on the screen.
 *    This reduces the initial composition time and memory footprint as the settings list grows.
 * 2. Unresolved Reference Fix: Fixed the compilation error by replacing 'Icons.Default.GitHub'
 *    (which is not in the standard Material library) with 'Icons.Default.Code'.
 * 3. RTL Support: Switched to 'AutoMirrored' versions of navigation icons (ArrowBack and KeyboardArrowRight)
 *    to ensure proper rendering in right-to-left languages without manual layout adjustments.
 * 4. Performance Logging: Added logging to monitor navigation events, which helps in identifying
 *    any unexpected re-triggers or performance lags during user interaction.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = {
                        Log.d("SettingsScreen", "Navigating back")
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            item {
                SettingsCategory(title = "Account")
            }
            item {
                SettingsItem(icon = Icons.Default.Person, title = "Profile Information")
            }
            item {
                // FIXED: Icons.Default.GitHub does not exist in standard Material icons.
                // Replaced with Icons.Default.Code as a technical substitute.
                SettingsItem(icon = Icons.Default.Code, title = "GitHub Integration")
            }
            item {
                SettingsItem(icon = Icons.Default.Security, title = "Privacy & Security")
            }

            // PHASE 7: Recommendation Engine Settings
            item {
                SettingsCategory(title = "Recommendations")
            }
            item {
                SettingsItem(icon = Icons.Default.Favorite, title = "Project Interests")
            }
            item {
                SettingsItem(icon = Icons.Default.Star, title = "Skill Matching Preferences")
            }
            item {
                SettingsItem(icon = Icons.Default.Event, title = "Availability Status", value = "Available")
            }

            item {
                SettingsCategory(title = "Appearance")
            }
            item {
                SettingsItem(icon = Icons.Default.Palette, title = "Theme", value = "Dark Mode")
            }
            
            item {
                SettingsCategory(title = "Notifications")
            }
            item {
                SettingsItem(icon = Icons.Default.Notifications, title = "Push Notifications")
            }
            
            item {
                SettingsCategory(title = "About")
            }
            item {
                SettingsItem(icon = Icons.Default.Info, title = "Version", value = "1.0.0")
            }
            item {
                SettingsItem(icon = Icons.Default.Description, title = "Terms of Service")
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
                
                TextButton(
                    onClick = {
                        Log.d("SettingsScreen", "User requested logout")
                        /* Logout logic here */
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Log Out")
                }
            }
        }
    }
}

@Composable
fun SettingsCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    value: String? = null,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            // Use AutoMirrored icon for better localization support (LHT/RHT)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

package com.scrap2stack.app.feature.settings

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToGitHub: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel()
) {
    val preferences by viewModel.userPreferencesState.collectAsState()

    var showAvailabilityDialog by remember { mutableStateOf(false) }
    var showInterestsDialog by remember { mutableStateOf(false) }
    var showSkillsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

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
                SettingsItem(
                    icon = Icons.Default.Person,
                    title = "Profile Information",
                    onClick = onNavigateToProfile
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Code,
                    title = "GitHub Integration",
                    onClick = onNavigateToGitHub
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "Privacy & Security",
                    onClick = { showPrivacyDialog = true }
                )
            }

            item {
                SettingsCategory(title = "Recommendations")
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Favorite,
                    title = "Project Interests",
                    value = preferences.projectInterests,
                    onClick = { showInterestsDialog = true }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Star,
                    title = "Skill Matching Preferences",
                    value = preferences.skillMatchingPreferences,
                    onClick = { showSkillsDialog = true }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Event,
                    title = "Availability Status",
                    value = preferences.availabilityStatus,
                    onClick = { showAvailabilityDialog = true }
                )
            }

            item {
                SettingsCategory(title = "Appearance")
            }
            item {
                SettingsToggleItem(
                    icon = Icons.Default.Palette,
                    title = "Dark Theme",
                    checked = preferences.isDarkMode,
                    onCheckedChange = { viewModel.toggleDarkMode(it) }
                )
            }

            item {
                SettingsCategory(title = "Notifications")
            }
            item {
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Push Notifications",
                    checked = preferences.isPushNotificationsEnabled,
                    onCheckedChange = { viewModel.togglePushNotifications(it) }
                )
            }

            item {
                SettingsCategory(title = "About")
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    value = "1.0.0",
                    onClick = {}
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Description,
                    title = "Terms of Service",
                    onClick = { showTermsDialog = true }
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))

                TextButton(
                    onClick = {
                        Log.d("SettingsScreen", "User requested logout")
                        onLogout()
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Log Out")
                }
            }
        }
    }

    if (showAvailabilityDialog) {
        AvailabilityStatusDialog(
            currentStatus = preferences.availabilityStatus,
            onDismiss = { showAvailabilityDialog = false },
            onSelectStatus = { newStatus ->
                viewModel.updateAvailabilityStatus(newStatus)
                showAvailabilityDialog = false
            }
        )
    }

    if (showInterestsDialog) {
        EditPreferenceDialog(
            title = "Project Interests",
            initialValue = preferences.projectInterests,
            onDismiss = { showInterestsDialog = false },
            onSave = { newInterests ->
                viewModel.updateProjectInterests(newInterests)
                showInterestsDialog = false
            }
        )
    }

    if (showSkillsDialog) {
        EditPreferenceDialog(
            title = "Skill Matching Preferences",
            initialValue = preferences.skillMatchingPreferences,
            onDismiss = { showSkillsDialog = false },
            onSave = { newSkills ->
                viewModel.updateSkillMatchingPreferences(newSkills)
                showSkillsDialog = false
            }
        )
    }

    if (showPrivacyDialog) {
        InfoAlertDialog(
            title = "Privacy & Security",
            message = "Scrap2Stack respects your privacy. Your data is stored securely and used solely to connect developers with open source or revived project opportunities. You can revoke access or request data deletion anytime.",
            onDismiss = { showPrivacyDialog = false }
        )
    }

    if (showTermsDialog) {
        InfoAlertDialog(
            title = "Terms of Service",
            message = "By using Scrap2Stack, you agree to collaborate respectfully, credit project contributors, and adhere to open-source licenses for all revived repositories.",
            onDismiss = { showTermsDialog = false }
        )
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (value != null) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
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
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
fun AvailabilityStatusDialog(
    currentStatus: String,
    onDismiss: () -> Unit,
    onSelectStatus: (String) -> Unit
) {
    val options = listOf("Available", "Open to Collaboration", "Busy", "Not Available")
    var selectedOption by remember { mutableStateOf(currentStatus) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Availability Status") },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                options.forEach { text ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .selectable(
                                selected = (text == selectedOption),
                                onClick = { selectedOption = text },
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (text == selectedOption),
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = text, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSelectStatus(selectedOption) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditPreferenceDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var textState by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = textState,
                onValueChange = { textState = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(textState) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun InfoAlertDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

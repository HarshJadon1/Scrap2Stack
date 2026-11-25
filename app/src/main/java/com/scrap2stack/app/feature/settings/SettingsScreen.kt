package com.scrap2stack.app.feature.settings

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scrap2stack.app.core.ui.components.BrutalHeaderTag
import com.scrap2stack.app.core.ui.components.BrutalTopBar
import com.scrap2stack.app.core.ui.components.brutalCard
import com.scrap2stack.app.core.ui.components.brutalTextFieldColors
import com.scrap2stack.app.core.ui.components.dotGridBackground
import com.scrap2stack.app.core.ui.components.tactileBrutalCard
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.StatusAbandoned
import com.scrap2stack.app.ui.theme.brutalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToGitHub: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel()
) {
    val brutal = MaterialTheme.brutalColors
    val preferences by viewModel.userPreferencesState.collectAsState()

    var showAvailabilityDialog by remember { mutableStateOf(false) }
    var showInterestsDialog by remember { mutableStateOf(false) }
    var showSkillsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = brutal.canvas,
        topBar = {
            BrutalTopBar(
                tag = "SYSTEM // CONFIG",
                title = "Settings",
                onNavigateBack = {
                    Log.d("SettingsScreen", "Navigating back")
                    onNavigateBack()
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .dotGridBackground()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            item { SettingsCategory(title = "Account") }
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

            item { SettingsCategory(title = "Recommendations") }
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

            item { SettingsCategory(title = "Appearance") }
            item {
                SettingsToggleItem(
                    icon = Icons.Default.Palette,
                    title = "Dark Theme",
                    checked = preferences.isDarkMode,
                    onCheckedChange = { viewModel.toggleDarkMode(it) }
                )
            }

            item { SettingsCategory(title = "Notifications") }
            item {
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Push Notifications",
                    checked = preferences.isPushNotificationsEnabled,
                    onCheckedChange = { viewModel.togglePushNotifications(it) }
                )
            }

            item { SettingsCategory(title = "About") }
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
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .tactileBrutalCard(
                            cornerRadius = 14.dp,
                            containerColor = brutal.surface,
                            borderColor = StatusAbandoned,
                            borderWidth = 2.dp,
                            shadowColor = BrutalShadowBlack,
                            shadowOffsetX = 4.dp,
                            shadowOffsetY = 4.dp,
                            onClick = {
                                Log.d("SettingsScreen", "User requested logout")
                                onLogout()
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "LOG OUT",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        ),
                        color = StatusAbandoned
                    )
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
    BrutalHeaderTag(
        text = title,
        modifier = Modifier.padding(top = 14.dp, bottom = 2.dp, start = 2.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    value: String? = null,
    onClick: () -> Unit = {}
) {
    val brutal = MaterialTheme.brutalColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .tactileBrutalCard(
                cornerRadius = 14.dp,
                containerColor = brutal.surface,
                borderColor = brutal.border,
                borderWidth = 2.dp,
                shadowColor = brutal.shadow,
                shadowOffsetX = 3.dp,
                shadowOffsetY = 3.dp,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = brutal.accentOnSurface
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = brutal.textPrimary
                )
                if (value != null) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        color = brutal.textMuted
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = brutal.textMuted
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
    val brutal = MaterialTheme.brutalColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .brutalCard(
                cornerRadius = 14.dp,
                containerColor = brutal.surface,
                borderColor = brutal.border,
                borderWidth = 2.dp,
                shadowColor = brutal.shadow,
                shadowOffsetX = 3.dp,
                shadowOffsetY = 3.dp
            )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = brutal.accentOnSurface
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = brutal.textPrimary,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BrutalInkBlack,
                    checkedTrackColor = ElectricMint,
                    checkedBorderColor = brutal.border,
                    uncheckedThumbColor = brutal.textMuted,
                    uncheckedTrackColor = brutal.surfaceRaised,
                    uncheckedBorderColor = brutal.border
                )
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
    val brutal = MaterialTheme.brutalColors
    val options = listOf("Available", "Open to Collaboration", "Busy", "Not Available")
    var selectedOption by remember { mutableStateOf(currentStatus) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = brutal.surface,
        titleContentColor = brutal.textPrimary,
        title = { Text("Select Availability Status", fontWeight = FontWeight.Black) },
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
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = brutal.accentOnSurface)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = text, style = MaterialTheme.typography.bodyLarge, color = brutal.textPrimary)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSelectStatus(selectedOption) }) {
                Text("Save", fontWeight = FontWeight.Black, color = brutal.accentOnSurface)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = brutal.textMuted)
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
    val brutal = MaterialTheme.brutalColors
    var textState by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = brutal.surface,
        titleContentColor = brutal.textPrimary,
        title = { Text(title, fontWeight = FontWeight.Black) },
        text = {
            OutlinedTextField(
                value = textState,
                onValueChange = { textState = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = brutalTextFieldColors()
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(textState) }) {
                Text("Save", fontWeight = FontWeight.Black, color = brutal.accentOnSurface)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = brutal.textMuted)
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
    val brutal = MaterialTheme.brutalColors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = brutal.surface,
        titleContentColor = brutal.textPrimary,
        textContentColor = brutal.textMuted,
        title = { Text(title, fontWeight = FontWeight.Black) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Black, color = brutal.accentOnSurface)
            }
        }
    )
}

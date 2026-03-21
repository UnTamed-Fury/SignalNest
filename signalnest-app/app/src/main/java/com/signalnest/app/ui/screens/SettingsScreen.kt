package com.signalnest.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.signalnest.app.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel) {
    val ctx        = LocalContext.current
    val cs         = MaterialTheme.colorScheme
    val serverUrl  by vm.serverUrl.collectAsStateWithLifecycle()
    val theme      by vm.theme.collectAsStateWithLifecycle()
    val amoled     by vm.amoled.collectAsStateWithLifecycle()
    val sound      by vm.notifSound.collectAsStateWithLifecycle()
    val vib        by vm.notifVib.collectAsStateWithLifecycle()
    val maxEvents  by vm.maxEvents.collectAsStateWithLifecycle()
    var sliderVal  by remember(maxEvents) { mutableFloatStateOf(maxEvents.toFloat()) }
    var showReset  by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.background)) },
        containerColor = cs.background,
    ) { pad ->
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(pad).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)) {

            SHeader("Server")
            SCard {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Cloud, null, Modifier.size(18.dp), tint = cs.primary)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Connected server", style = MaterialTheme.typography.titleMedium)
                        Text(serverUrl.ifBlank { "Not configured" }, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    }
                }
            }

            SHeader("Appearance")
            SCard {
                Text("Theme", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("SYSTEM", "LIGHT", "DARK").forEach { t ->
                        FilterChip(theme == t, { vm.setTheme(t) }, label = { Text(t.lowercase().replaceFirstChar { it.uppercase() }) })
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 10.dp))
                SwitchRow("AMOLED black", "True black in dark mode", Icons.Default.DarkMode, amoled) { vm.setAmoled(it) }
            }

            SHeader("Notifications")
            SCard {
                SwitchRow("Sound", "Play sound on new event", Icons.Default.VolumeUp, sound) { vm.setNotifSound(it) }
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                SwitchRow("Vibration", "Vibrate on new event", Icons.Default.Vibration, vib) { vm.setNotifVib(it) }
            }

            SHeader("Storage")
            SCard {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Max stored events", style = MaterialTheme.typography.titleMedium)
                        Text("$maxEvents events", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                    }
                    Slider(sliderVal, { sliderVal = it }, valueRange = 100f..2000f, steps = 18,
                        onValueChangeFinished = { vm.setMaxEvents(sliderVal.toInt()) }, modifier = Modifier.width(120.dp))
                }
            }

            SHeader("About")
            SCard {
                InfoRow("Version", "1.0.0")
                InfoRow("Author", "UnTamedFury")
                InfoRow("License", "SignalNest Source License v1")
            }

            SHeader("Danger zone")
            OutlinedButton(onClick = { showReset = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.error)) {
                Icon(Icons.Default.RestartAlt, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp)); Text("Reset & re-run setup")
            }
            Spacer(Modifier.height(80.dp))
        }
    }

    if (showReset) AlertDialog(
        onDismissRequest = { showReset = false },
        title = { Text("Reset setup?") },
        text  = { Text("This disconnects from the server and shows onboarding again. Your local data is kept.") },
        confirmButton = { TextButton({ vm.resetOnboarding(ctx); showReset = false }) { Text("Reset", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton({ showReset = false }) { Text("Cancel") } },
    )
}

@Composable fun SHeader(text: String) =
    Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 2.dp, start = 4.dp))

@Composable fun SCard(content: @Composable ColumnScope.() -> Unit) =
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)) { Column(Modifier.padding(16.dp), content = content) }

@Composable fun SwitchRow(label: String, sub: String, icon: ImageVector, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) { Text(label, style = MaterialTheme.typography.titleMedium); Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Switch(checked, onToggle)
    }
}

@Composable fun InfoRow(label: String, value: String) =
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }

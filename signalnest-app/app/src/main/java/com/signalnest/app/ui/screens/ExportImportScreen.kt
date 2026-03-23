package com.signalnest.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.signalnest.app.ui.viewmodels.ExportState
import com.signalnest.app.ui.viewmodels.ExportViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportImportScreen(vm: ExportViewModel, onBack: () -> Unit) {
    val cs    = MaterialTheme.colorScheme
    val ctx   = LocalContext.current
    val state by vm.state.collectAsStateWithLifecycle()

    // SAF launcher — create file for export
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) vm.export(ctx, uri)
    }

    // SAF launcher — open file for import
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) vm.import(ctx, uri)
    }

    // Auto-dismiss status after 4 s
    LaunchedEffect(state) {
        if (state is ExportState.Done || state is ExportState.Error) {
            kotlinx.coroutines.delay(4_000)
            vm.reset()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                title = { Text("Backup & Restore") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.background),
            )
        },
        containerColor = cs.background,
    ) { pad ->
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(pad).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Status banner
            AnimatedVisibility(state != ExportState.Idle && state != ExportState.Working) {
                val isError = state is ExportState.Error
                Surface(
                    color  = if (isError) cs.errorContainer else cs.primaryContainer,
                    shape  = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(
                            if (isError) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                            null, Modifier.size(20.dp),
                            tint = if (isError) cs.onErrorContainer else cs.onPrimaryContainer,
                        )
                        Text(
                            when (state) {
                                is ExportState.Done  -> (state as ExportState.Done).result
                                is ExportState.Error -> (state as ExportState.Error).msg
                                else -> ""
                            },
                            color = if (isError) cs.onErrorContainer else cs.onPrimaryContainer,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            if (state is ExportState.Working) LinearProgressIndicator(Modifier.fillMaxWidth())

            // Export section
            SCard {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Upload, null, Modifier.size(24.dp), tint = cs.primary)
                    Column(Modifier.weight(1f)) {
                        Text("Export backup", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                        Text("Saves all events, notes, todos, and rules to a JSON file.",
                            style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = {
                        val ts  = SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault()).format(Date())
                        exportLauncher.launch("signalnest-backup-$ts.json")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    enabled  = state != ExportState.Working,
                ) {
                    Icon(Icons.Default.SaveAlt, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Choose save location…")
                }
            }

            // Import section
            SCard {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Download, null, Modifier.size(24.dp), tint = cs.primary)
                    Column(Modifier.weight(1f)) {
                        Text("Restore backup", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                        Text("Merges a JSON backup into your current data. Existing items are not deleted.",
                            style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick  = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    enabled  = state != ExportState.Working,
                ) {
                    Icon(Icons.Default.FolderOpen, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Choose backup file…")
                }
            }

            // Info card
            SCard {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Info, null, Modifier.size(18.dp), tint = cs.onSurfaceVariant)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Backup contains", style = MaterialTheme.typography.labelSmall,
                            color = cs.primary, fontWeight = FontWeight.SemiBold)
                        listOf("Events (feed items)", "Markdown notes", "Todo tasks", "SNRL rules").forEach {
                            Text("• $it", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("Format: JSON  •  Compatible with SignalNest v2+",
                            style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(60.dp))
        }
    }
}

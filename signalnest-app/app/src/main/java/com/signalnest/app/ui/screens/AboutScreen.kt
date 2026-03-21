package com.signalnest.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    val cs = MaterialTheme.colorScheme

    Scaffold(
        topBar = { TopAppBar(title = { Text("About") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.background)) },
        containerColor = cs.background,
    ) { pad ->
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(pad).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(96.dp).clip(RoundedCornerShape(28.dp)).background(cs.primaryContainer),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Webhook, null, Modifier.size(56.dp), tint = cs.primary)
            }
            Spacer(Modifier.height(20.dp))
            Text("SignalNest", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("v1.0.0", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text("Webhook notifications — no cloud, no Firebase, no bullshit.",
                style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant, textAlign = TextAlign.Center)

            Spacer(Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(Modifier.height(24.dp))

            Text("Developer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(12.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cs.surfaceVariant)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, Modifier.size(20.dp), tint = cs.primary)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("UnTamedFury", style = MaterialTheme.typography.titleMedium)
                        Text("Creator & lead developer", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Tech Stack", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(12.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cs.surfaceVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TechRow("Android", "Kotlin + Jetpack Compose + Room")
                    TechRow("Server",  "Node.js + Express + WebSocket")
                    TechRow("Deploy",  "Render.com (free tier)")
                    TechRow("Push",    "WebSocket — no Firebase")
                    TechRow("LAN",     "NanoHTTPD embedded server")
                    TechRow("Storage", "DataStore + Room SQLite")
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("License", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(12.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cs.surfaceVariant)) {
                Column(Modifier.padding(16.dp)) {
                    Text("SignalNest Source License v1", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text("Free for personal use. Commercial use and forks require attribution to UnTamedFury and a royalty agreement. See LICENSE file.",
                        style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(40.dp))
            Text("Made with ❤️ and way too much caffeine",
                style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun TechRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

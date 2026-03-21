package com.signalnest.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.signalnest.app.ui.viewmodels.OnboardState
import com.signalnest.app.ui.viewmodels.OnboardingViewModel

@Composable
fun OnboardingScreen(vm: OnboardingViewModel = viewModel()) {
    val ctx   = LocalContext.current
    val state by vm.state.collectAsStateWithLifecycle()
    val cs    = MaterialTheme.colorScheme

    var url       by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var showPass  by remember { mutableStateOf(false) }
    var step      by remember { mutableIntStateOf(0) }   // 0=welcome 1=connect

    Box(Modifier.fillMaxSize().background(cs.background), contentAlignment = Alignment.Center) {
        Column(
            Modifier.fillMaxWidth().padding(32.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // Logo
            Box(
                Modifier.size(80.dp).clip(RoundedCornerShape(24.dp)).background(cs.primaryContainer),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Default.Webhook, null, Modifier.size(48.dp), tint = cs.primary) }

            Spacer(Modifier.height(24.dp))
            Text("SignalNest", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Webhook notifications — no cloud, no Firebase",
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(40.dp))

            AnimatedContent(targetState = step, label = "step") { s ->
                when (s) {
                    0 -> WelcomeStep(features = listOf(
                        Icons.Default.Webhook       to "Receive webhooks from anywhere",
                        Icons.Default.Wifi          to "Works on local network too",
                        Icons.Default.RssFeed       to "Built-in RSS feed reader",
                        Icons.Default.CheckCircle   to "Todo list with reminders",
                        Icons.Default.StickyNote2   to "Markdown notes",
                        Icons.Default.Lock          to "No account required",
                    ), onContinue = { step = 1 })
                    else -> ConnectStep(
                        url = url, password = password, showPass = showPass,
                        onUrl = { url = it }, onPassword = { password = it },
                        onTogglePass = { showPass = !showPass },
                        loading = state is OnboardState.Loading,
                        error   = (state as? OnboardState.Error)?.msg,
                        onConnect = { vm.connect(url.trim(), password, ctx) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep(features: List<Pair<androidx.compose.ui.graphics.vector.ImageVector, String>>, onContinue: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        features.forEach { (icon, label) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, Modifier.size(20.dp), tint = cs.primary)
                Spacer(Modifier.width(14.dp))
                Text(label, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(32.dp))
        Button(onClick = onContinue, Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
            Text("Get Started", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, null, Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ConnectStep(
    url: String, password: String, showPass: Boolean,
    onUrl: (String) -> Unit, onPassword: (String) -> Unit, onTogglePass: () -> Unit,
    loading: Boolean, error: String?, onConnect: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Column {
        Text("Connect to server", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text("Deploy signalnest-server on Render and paste the URL below.", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(url, onUrl, Modifier.fillMaxWidth(), label = { Text("Server URL") },
            placeholder = { Text("https://your-app.onrender.com") },
            leadingIcon = { Icon(Icons.Default.Cloud, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            singleLine = true, shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(password, onPassword, Modifier.fillMaxWidth(), label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            trailingIcon = { IconButton(onTogglePass) { Icon(if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } },
            visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true, shape = RoundedCornerShape(12.dp),
            supportingText = { Text("Set via PASSWORD env var on your server") })
        Spacer(Modifier.height(8.dp))

        AnimatedVisibility(visible = error != null) {
            Surface(Modifier.fillMaxWidth(), color = cs.errorContainer, shape = RoundedCornerShape(10.dp)) {
                Text(error ?: "", Modifier.padding(12.dp), color = cs.onErrorContainer, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(onClick = onConnect, Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp),
            enabled = !loading && url.isNotBlank() && password.isNotBlank()) {
            if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = cs.onPrimary)
            else { Text("Connect", fontWeight = FontWeight.SemiBold, fontSize = 16.sp); Spacer(Modifier.width(8.dp)); Icon(Icons.Default.Link, null, Modifier.size(18.dp)) }
        }
    }
}

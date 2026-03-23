package com.signalnest.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.signalnest.app.data.models.SnrlRule
import com.signalnest.app.ui.viewmodels.RuleOp
import com.signalnest.app.ui.viewmodels.RulesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersScreen(vm: RulesViewModel, onBack: () -> Unit) {
    val cs       = MaterialTheme.colorScheme
    val rules    by vm.rules.collectAsStateWithLifecycle()
    val op       by vm.op.collectAsStateWithLifecycle()
    val validate by vm.validateResult.collectAsStateWithLifecycle()

    var showCreate  by remember { mutableStateOf(false) }
    var editing     by remember { mutableStateOf<SnrlRule?>(null) }
    var toDelete    by remember { mutableStateOf<SnrlRule?>(null) }

    // Sync on first load
    LaunchedEffect(Unit) { vm.syncFromServer() }

    // Dismiss errors/done after a beat
    LaunchedEffect(op) {
        if (op is RuleOp.Done || op is RuleOp.Error) {
            kotlinx.coroutines.delay(2_000)
            vm.clearOp()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                title = { Text("SNRL Rules") },
                actions = {
                    IconButton({ vm.syncFromServer() }) { Icon(Icons.Default.Sync, "Sync") }
                    IconButton({ showCreate = true }) { Icon(Icons.Default.Add, "Add rule") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.background),
            )
        },
        containerColor = cs.background,
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            // Status bar
            AnimatedVisibility(op != RuleOp.Idle && op != RuleOp.Loading) {
                val (color, msg) = when (op) {
                    is RuleOp.Error -> cs.errorContainer to (op as RuleOp.Error).msg
                    else            -> cs.primaryContainer to "Saved"
                }
                Surface(color = color, modifier = Modifier.fillMaxWidth()) {
                    Text(msg, Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (op is RuleOp.Error) cs.onErrorContainer else cs.onPrimaryContainer)
                }
            }
            if (op is RuleOp.Loading) LinearProgressIndicator(Modifier.fillMaxWidth())

            if (rules.isEmpty() && op !is RuleOp.Loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)) {
                        Icon(Icons.Default.Rule, null, Modifier.size(64.dp),
                            tint = cs.onSurfaceVariant.copy(alpha = 0.2f))
                        Spacer(Modifier.height(12.dp))
                        Text("No rules yet", style = MaterialTheme.typography.titleMedium,
                            color = cs.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Text("Rules transform events before they arrive.\nTap + to create one.",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Spacer(Modifier.height(20.dp))
                        // Show syntax cheat-sheet
                        SnrlCheatSheet()
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    items(rules, key = { it.id }) { rule ->
                        RuleCard(rule,
                            onToggle = { vm.toggleEnabled(rule) },
                            onEdit   = { editing = rule },
                            onDelete = { toDelete = rule })
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showCreate || editing != null) {
        RuleSheet(
            existing  = editing,
            validate  = validate,
            onValidate = { vm.validateRule(it) },
            onClearValidate = { vm.clearValidateResult() },
            onDismiss = { showCreate = false; editing = null; vm.clearValidateResult() },
            onSave    = { name, text ->
                if (editing != null) vm.updateRule(editing!!, name, text)
                else vm.createRule(name, text)
                showCreate = false; editing = null
            },
        )
    }

    toDelete?.let { rule ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title   = { Text("Delete rule?") },
            text    = { Text("\"${rule.name}\" will be permanently deleted from the server.") },
            confirmButton = { TextButton({ vm.deleteRule(rule); toDelete = null }) { Text("Delete", color = cs.error) } },
            dismissButton = { TextButton({ toDelete = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun RuleCard(rule: SnrlRule, onToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Card(
        Modifier.fillMaxWidth(),
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(rule.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(2.dp))
                    Text(rule.text,
                        style    = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color    = cs.onSurfaceVariant,
                        maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.width(8.dp))
                Switch(checked = rule.enabled, onCheckedChange = { onToggle() })
            }
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onEdit) {
                    Icon(Icons.Default.Edit, null, Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp)); Text("Edit")
                }
                TextButton(onDelete) {
                    Icon(Icons.Default.Delete, null, Modifier.size(14.dp), tint = cs.error)
                    Spacer(Modifier.width(4.dp)); Text("Delete", color = cs.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RuleSheet(
    existing: SnrlRule?,
    validate: com.signalnest.app.network.ValidateResponse?,
    onValidate: (String) -> Unit,
    onClearValidate: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var text by remember { mutableStateOf(existing?.text ?: "") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                .navigationBarsPadding().imePadding()
        ) {
            Text(if (existing == null) "New rule" else "Edit rule",
                style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(),
                label = { Text("Rule name") }, singleLine = true,
                shape = RoundedCornerShape(12.dp))
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value   = text,
                onValueChange = { text = it; onClearValidate() },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                label   = { Text("SNRL rule") },
                maxLines = 8,
                shape   = RoundedCornerShape(12.dp),
                placeholder = { Text("WHEN source = \"github\" AND title CONTAINS \"push\"\nTHEN group = \"ci\", category = \"normal\"",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
            )

            // Validate result
            AnimatedVisibility(validate != null) {
                val v = validate ?: return@AnimatedVisibility
                val color = if (v.ok) cs.primaryContainer else cs.errorContainer
                val tint  = if (v.ok) cs.onPrimaryContainer else cs.onErrorContainer
                Surface(color = color, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (v.ok) Icons.Default.CheckCircle else Icons.Default.Error,
                                null, Modifier.size(16.dp), tint = tint)
                            Spacer(Modifier.width(8.dp))
                            Text(if (v.ok) "Valid rule" else v.error ?: "Invalid",
                                color = tint, style = MaterialTheme.typography.bodySmall)
                        }
                        v.warnings.forEach { w ->
                            Text("⚠ $w", color = tint.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton({ onValidate(text) }, Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    enabled = text.isNotBlank()) { Text("Validate") }
                Button({
                    if (name.isBlank()) return@Button
                    onSave(name, text)
                }, Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    enabled = name.isNotBlank() && text.isNotBlank()) {
                    Text(if (existing == null) "Create" else "Update")
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SnrlCheatSheet() {
    val cs = MaterialTheme.colorScheme
    Card(
        Modifier.fillMaxWidth(),
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surfaceVariant.copy(alpha = 0.5f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("SNRL syntax", style = MaterialTheme.typography.labelSmall,
                color = cs.primary, fontWeight = FontWeight.Bold)
            CheatRow("Operators", "=  !=  CONTAINS  STARTSWITH  ENDSWITH  MATCHES")
            CheatRow("Fields", "title  body  source  group  category  channel")
            CheatRow("Combine", "AND  OR")
            CheatRow("Template", "{{title}}  {{source}}  etc. in THEN values")
            Spacer(Modifier.height(4.dp))
            Text("Example:", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
            Surface(color = cs.background, shape = RoundedCornerShape(8.dp)) {
                Text(
                    "WHEN source = \"github\" AND title CONTAINS \"push\"\nTHEN group = \"ci\", title = \"🔀 {{title}}\"",
                    Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = cs.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CheatRow(label: String, value: String) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth()) {
        Text("$label:", style = MaterialTheme.typography.labelSmall,
            color = cs.onSurfaceVariant, modifier = Modifier.width(72.dp))
        Text(value, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = cs.onSurface)
    }
}

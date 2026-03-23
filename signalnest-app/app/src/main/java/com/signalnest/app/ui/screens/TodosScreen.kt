package fury.signalnest.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fury.signalnest.app.data.models.Todo
import fury.signalnest.app.ui.viewmodels.TodosViewModel
import fury.signalnest.app.utils.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodosScreen(vm: TodosViewModel) {
    val todos  by vm.todos.collectAsStateWithLifecycle()
    val cs     = MaterialTheme.colorScheme
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Todo?>(null) }

    val pending   = todos.filter { !it.isDone }
    val completed = todos.filter {  it.isDone }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Todos") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.background)) },
        floatingActionButton = {
            FloatingActionButton({ showAdd = true }, containerColor = cs.primary) {
                Icon(Icons.Default.Add, null, tint = cs.onPrimary)
            }
        },
        containerColor = cs.background,
    ) { pad ->
        if (todos.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircleOutline, null, Modifier.size(72.dp), tint = cs.onSurfaceVariant.copy(alpha = 0.25f))
                    Spacer(Modifier.height(12.dp))
                    Text("All done!", color = cs.onSurfaceVariant, style = MaterialTheme.typography.titleMedium)
                    Text("Tap + to add a task", color = cs.onSurfaceVariant.copy(alpha = 0.5f))
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp,
                    top = pad.calculateTopPadding() + 8.dp, bottom = pad.calculateBottomPadding() + 80.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (pending.isNotEmpty()) {
                    item { Text("Pending", style = MaterialTheme.typography.labelSmall, color = cs.primary, modifier = Modifier.padding(start = 4.dp, top = 4.dp)) }
                    items(pending, key = { it.id }) { t ->
                        TodoCard(t, onToggle = { vm.setDone(t.id, !t.isDone) }, onEdit = { editing = t }, onDelete = { vm.delete(t) })
                    }
                }
                if (completed.isNotEmpty()) {
                    item { Spacer(Modifier.height(8.dp)); Text("Completed", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp)) }
                    items(completed, key = { it.id }) { t ->
                        TodoCard(t, onToggle = { vm.setDone(t.id, !t.isDone) }, onEdit = { editing = t }, onDelete = { vm.delete(t) })
                    }
                }
            }
        }
    }

    if (showAdd || editing != null) {
        TodoSheet(editing, onDismiss = { showAdd = false; editing = null }) { title, desc, dueAt, cron, priority ->
            editing?.let { vm.update(it.copy(title = title, description = desc, dueAt = dueAt, cronExpr = cron, priority = priority)) }
                ?: vm.save(title, desc, dueAt, cron, priority)
            showAdd = false; editing = null
        }
    }
}

@Composable
private fun TodoCard(todo: Todo, onToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val priorityColor = when (todo.priority) { 2 -> cs.error; 1 -> cs.tertiary; else -> cs.onSurfaceVariant }

    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surfaceVariant), elevation = CardDefaults.cardElevation(0.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = todo.isDone, onCheckedChange = { onToggle() })
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(todo.title,
                    style = if (todo.isDone) MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough)
                            else MaterialTheme.typography.bodyMedium,
                    color = if (todo.isDone) cs.onSurfaceVariant else cs.onSurface)
                if (todo.description.isNotBlank())
                    Text(todo.description, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, maxLines = 2)
                if (todo.dueAt != null)
                    Text("Due: ${TimeUtils.format(todo.dueAt)}", style = MaterialTheme.typography.labelSmall, color = priorityColor)
                if (todo.cronExpr.isNotBlank())
                    Text("Cron: ${todo.cronExpr}", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
            }
            IconButton(onEdit, Modifier.size(32.dp)) { Icon(Icons.Default.Edit, null, Modifier.size(16.dp)) }
            IconButton(onDelete, Modifier.size(32.dp)) { Icon(Icons.Default.Delete, null, Modifier.size(16.dp), tint = cs.error) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoSheet(existing: Todo?, onDismiss: () -> Unit, onSave: (String, String, Long?, String, Int) -> Unit) {
    var title    by remember { mutableStateOf(existing?.title ?: "") }
    var desc     by remember { mutableStateOf(existing?.description ?: "") }
    var cron     by remember { mutableStateOf(existing?.cronExpr ?: "") }
    var priority by remember { mutableIntStateOf(existing?.priority ?: 0) }
    var dueInput by remember { mutableStateOf(existing?.dueAt?.let { TimeUtils.format(it) } ?: "") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).navigationBarsPadding().imePadding()) {
            Text(if (existing == null) "New task" else "Edit task", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Title") }, singleLine = true, shape = RoundedCornerShape(12.dp))
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(desc, { desc = it }, Modifier.fillMaxWidth(), label = { Text("Description") }, maxLines = 4, shape = RoundedCornerShape(12.dp))
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(dueInput, { dueInput = it }, Modifier.fillMaxWidth(), label = { Text("Due date (optional)") },
                placeholder = { Text("e.g. 2025-12-31 18:00") }, singleLine = true, shape = RoundedCornerShape(12.dp))
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(cron, { cron = it }, Modifier.fillMaxWidth(), label = { Text("Cron reminder (optional)") },
                placeholder = { Text("e.g. 0 9 * * 1  (every Mon 9am)") }, singleLine = true, shape = RoundedCornerShape(12.dp))
            Spacer(Modifier.height(12.dp))
            Text("Priority", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0 to "Normal", 1 to "High", 2 to "Urgent").forEach { (v, label) ->
                    FilterChip(priority == v, { priority = v }, label = { Text(label) })
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onDismiss) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                Button({
                    // Parse due date loosely
                    val dueMs = runCatching {
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                        sdf.parse(dueInput)?.time
                    }.getOrNull()
                    onSave(title, desc, dueMs, cron, priority)
                }, enabled = title.isNotBlank(), shape = RoundedCornerShape(12.dp)) {
                    Text(if (existing == null) "Save" else "Update")
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

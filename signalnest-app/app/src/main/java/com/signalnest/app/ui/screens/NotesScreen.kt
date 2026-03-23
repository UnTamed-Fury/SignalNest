package fury.signalnest.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fury.signalnest.app.data.models.Note
import fury.signalnest.app.ui.viewmodels.NotesViewModel
import fury.signalnest.app.utils.TimeUtils

private val noteColors = listOf(
    Color(0xFF1E2A3A), Color(0xFF1A2E2A), Color(0xFF2E1A2E),
    Color(0xFF2E2A1A), Color(0xFF1A1E2E), Color(0xFF2A1A1A),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(vm: NotesViewModel) {
    val notes   by vm.notes.collectAsStateWithLifecycle()
    val cs      = MaterialTheme.colorScheme
    var showAdd  by remember { mutableStateOf(false) }
    var editing  by remember { mutableStateOf<Note?>(null) }
    var toDelete by remember { mutableStateOf<Note?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Notes") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.background)) },
        floatingActionButton = {
            FloatingActionButton({ showAdd = true }, containerColor = cs.primary) {
                Icon(Icons.Default.Add, null, tint = cs.onPrimary)
            }
        },
        containerColor = cs.background,
    ) { pad ->
        if (notes.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NoteAdd, null, Modifier.size(72.dp), tint = cs.onSurfaceVariant.copy(alpha = 0.25f))
                    Spacer(Modifier.height(12.dp))
                    Text("No notes yet", color = cs.onSurfaceVariant, style = MaterialTheme.typography.titleMedium)
                    Text("Tap + to add one", color = cs.onSurfaceVariant.copy(alpha = 0.5f), style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp,
                    top = pad.calculateTopPadding() + 8.dp, bottom = pad.calculateBottomPadding() + 80.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp,
                modifier = Modifier.fillMaxSize(),
            ) {
                items(notes, key = { it.id }) { note ->
                    NoteCard(note, onClick = { editing = note }, onDelete = { toDelete = note }, onPin = { vm.togglePin(note) })
                }
            }
        }
    }

    if (showAdd || editing != null) {
        NoteSheet(editing, onDismiss = { showAdd = false; editing = null }) { t, c, ci ->
            editing?.let { vm.update(it.copy(title = t, content = c, colorIndex = ci)) } ?: vm.save(t, c, ci)
            showAdd = false; editing = null
        }
    }

    toDelete?.let { n ->
        AlertDialog(onDismissRequest = { toDelete = null },
            title = { Text("Delete note?") },
            confirmButton = { TextButton({ vm.delete(n); toDelete = null }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton({ toDelete = null }) { Text("Cancel") } })
    }
}

@Composable
private fun NoteCard(note: Note, onClick: () -> Unit, onDelete: () -> Unit, onPin: () -> Unit) {
    val bg = noteColors.getOrElse(note.colorIndex) { noteColors[0] }
    Card(onClick, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = bg), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.padding(12.dp)) {
            if (note.isPinned) Icon(Icons.Default.PushPin, null, Modifier.size(14.dp).align(Alignment.End), tint = MaterialTheme.colorScheme.primary)
            if (note.title.isNotBlank()) { Text(note.title, style = MaterialTheme.typography.titleMedium, color = Color(0xFFE4E4EF)); Spacer(Modifier.height(4.dp)) }
            if (note.content.isNotBlank()) Text(note.content, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFB0B0C8), maxLines = 8)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(TimeUtils.relative(note.updatedAt), style = MaterialTheme.typography.labelSmall, color = Color(0xFF606080))
                Row {
                    IconButton(onPin, Modifier.size(28.dp)) { Icon(Icons.Default.PushPin, null, Modifier.size(14.dp), tint = Color(0xFF606080)) }
                    IconButton(onDelete, Modifier.size(28.dp)) { Icon(Icons.Default.Delete, null, Modifier.size(14.dp), tint = Color(0xFF606080)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteSheet(existing: Note?, onDismiss: () -> Unit, onSave: (String, String, Int) -> Unit) {
    var title   by remember { mutableStateOf(existing?.title   ?: "") }
    var content by remember { mutableStateOf(existing?.content ?: "") }
    var colorIdx by remember { mutableIntStateOf(existing?.colorIndex ?: 0) }
    val fr = remember { FocusRequester() }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).navigationBarsPadding().imePadding()) {
            Text(if (existing == null) "New note" else "Edit note", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Title") }, singleLine = true, shape = RoundedCornerShape(12.dp))
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(content, { content = it }, Modifier.fillMaxWidth().heightIn(min = 120.dp).focusRequester(fr),
                label = { Text("Content (Markdown supported)") }, maxLines = 14, shape = RoundedCornerShape(12.dp))
            Spacer(Modifier.height(14.dp))
            Text("Colour", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                noteColors.forEachIndexed { i, c ->
                    Surface(onClick = { colorIdx = i }, shape = CircleShape, color = c,
                        border = if (i == colorIdx) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier.size(28.dp)) {}
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onDismiss) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                Button({ onSave(title, content, colorIdx) }, enabled = title.isNotBlank() || content.isNotBlank(), shape = RoundedCornerShape(12.dp)) {
                    Text(if (existing == null) "Save" else "Update")
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
    LaunchedEffect(Unit) { if (existing == null) fr.requestFocus() }
}

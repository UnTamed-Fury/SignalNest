package fury.signalnest.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fury.signalnest.app.data.models.RssFeed
import fury.signalnest.app.ui.viewmodels.RssViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RssScreen(vm: RssViewModel) {
    val feeds  by vm.feeds.collectAsStateWithLifecycle()
    val cs     = MaterialTheme.colorScheme
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<RssFeed?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("RSS Feeds") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.background)) },
        floatingActionButton = {
            FloatingActionButton({ showAdd = true }, containerColor = cs.primary) { Icon(Icons.Default.Add, null, tint = cs.onPrimary) }
        },
        containerColor = cs.background,
    ) { pad ->
        if (feeds.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.RssFeed, null, Modifier.size(72.dp), tint = cs.onSurfaceVariant.copy(alpha = 0.25f))
                    Spacer(Modifier.height(12.dp))
                    Text("No RSS feeds", color = cs.onSurfaceVariant, style = MaterialTheme.typography.titleMedium)
                    Text("Tap + to add a feed URL", color = cs.onSurfaceVariant.copy(alpha = 0.5f), textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp,
                    top = pad.calculateTopPadding() + 8.dp, bottom = pad.calculateBottomPadding() + 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(feeds, key = { it.id }) { feed ->
                    FeedCard(feed,
                        onToggle = { vm.toggleEnabled(feed) },
                        onEdit   = { editing = feed },
                        onDelete = { vm.delete(feed) })
                }
            }
        }
    }

    if (showAdd || editing != null) {
        RssSheet(editing, onDismiss = { showAdd = false; editing = null }) { title, url, interval, silent ->
            editing?.let { vm.update(it.copy(title = title, url = url, intervalMinutes = interval, notifySilent = silent)) }
                ?: vm.add(title, url, interval, silent)
            showAdd = false; editing = null
        }
    }
}

@Composable
private fun FeedCard(feed: RssFeed, onToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surfaceVariant), elevation = CardDefaults.cardElevation(0.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.RssFeed, null, Modifier.size(24.dp),
                tint = if (feed.isEnabled) cs.primary else cs.onSurfaceVariant)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(feed.title, style = MaterialTheme.typography.bodyMedium)
                Text(feed.url, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, maxLines = 1)
                Text("Every ${feed.intervalMinutes}m · ${if (feed.notifySilent) "silent" else "normal"}",
                    style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
            }
            Switch(checked = feed.isEnabled, onCheckedChange = { onToggle() })
            IconButton(onEdit, Modifier.size(32.dp)) { Icon(Icons.Default.Edit, null, Modifier.size(16.dp)) }
            IconButton(onDelete, Modifier.size(32.dp)) { Icon(Icons.Default.Delete, null, Modifier.size(16.dp), tint = cs.error) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RssSheet(existing: RssFeed?, onDismiss: () -> Unit, onSave: (String, String, Int, Boolean) -> Unit) {
    var title    by remember { mutableStateOf(existing?.title ?: "") }
    var url      by remember { mutableStateOf(existing?.url ?: "") }
    var interval by remember { mutableStateOf((existing?.intervalMinutes ?: 30).toString()) }
    var silent   by remember { mutableStateOf(existing?.notifySilent ?: false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).navigationBarsPadding().imePadding()) {
            Text(if (existing == null) "Add RSS feed" else "Edit RSS feed", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Name") }, singleLine = true, shape = RoundedCornerShape(12.dp))
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(url, { url = it }, Modifier.fillMaxWidth(), label = { Text("Feed URL") },
                placeholder = { Text("https://example.com/feed.xml") }, singleLine = true, shape = RoundedCornerShape(12.dp))
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(interval, { interval = it }, Modifier.fillMaxWidth(), label = { Text("Poll interval (minutes)") },
                singleLine = true, shape = RoundedCornerShape(12.dp))
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("Silent notifications", style = MaterialTheme.typography.bodyMedium)
                    Text("No sound or vibration", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Switch(silent, { silent = it })
            }
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onDismiss) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                Button({ onSave(title, url, interval.toIntOrNull() ?: 30, silent) },
                    enabled = title.isNotBlank() && url.isNotBlank(), shape = RoundedCornerShape(12.dp)) {
                    Text(if (existing == null) "Add" else "Save")
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

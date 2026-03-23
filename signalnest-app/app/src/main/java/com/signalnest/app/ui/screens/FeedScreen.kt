package fury.signalnest.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fury.signalnest.app.data.models.Event
import fury.signalnest.app.ui.viewmodels.FeedViewModel
import fury.signalnest.app.utils.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(vm: FeedViewModel, onSearch: () -> Unit) {
    val events  by vm.events.collectAsStateWithLifecycle()
    val unread  by vm.unreadCount.collectAsStateWithLifecycle()
    val groups  by vm.groups.collectAsStateWithLifecycle()
    val cs      = MaterialTheme.colorScheme

    var selectedGroup by remember { mutableStateOf<String?>(null) }
    var showClear     by remember { mutableStateOf(false) }
    var expandedId    by remember { mutableStateOf<String?>(null) }

    val filtered = if (selectedGroup == null) events
                   else events.filter { it.group == selectedGroup }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Feed")
                        if (unread > 0) Badge { Text("$unread") }
                    }
                },
                actions = {
                    // Phase 2: search button
                    IconButton(onSearch) { Icon(Icons.Default.Search, "Search") }
                    if (unread > 0) IconButton({ vm.markAllRead() }) { Icon(Icons.Default.DoneAll, "Mark all read") }
                    if (events.isNotEmpty()) IconButton({ showClear = true }) { Icon(Icons.Default.DeleteSweep, "Clear") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.background),
            )
        },
        containerColor = cs.background,
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            if (groups.size > 1) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item { FilterChip(selectedGroup == null, { selectedGroup = null }, label = { Text("All") }) }
                    items(groups) { g ->
                        FilterChip(selectedGroup == g, { selectedGroup = g }, label = { Text(g) })
                    }
                }
            }

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.NotificationsNone, null, Modifier.size(72.dp),
                            tint = cs.onSurfaceVariant.copy(alpha = 0.25f))
                        Spacer(Modifier.height(16.dp))
                        Text("No events yet", style = MaterialTheme.typography.titleMedium, color = cs.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Text("POST JSON to  /webhook  on your server",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(
                    Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filtered, key = { it.id }) { event ->
                        EventCard(
                            event    = event,
                            expanded = expandedId == event.id,
                            onTap    = {
                                expandedId = if (expandedId == event.id) null else event.id
                                if (!event.isRead) vm.markRead(event.id)
                            },
                            onPin    = { vm.pin(event.id, !event.isPinned) },
                            onDelete = { vm.delete(event) },
                        )
                    }
                }
            }
        }
    }

    if (showClear) AlertDialog(
        onDismissRequest = { showClear = false },
        title = { Text("Clear all events?") },
        text  = { Text("Permanently deletes all ${events.size} events.") },
        confirmButton = { TextButton({ vm.clearAll(); showClear = false }) { Text("Clear", color = cs.error) } },
        dismissButton = { TextButton({ showClear = false }) { Text("Cancel") } },
    )
}

@Composable
fun EventCard(event: Event, expanded: Boolean, onTap: () -> Unit, onPin: () -> Unit, onDelete: () -> Unit) {
    val cs   = MaterialTheme.colorScheme
    val clip = LocalClipboardManager.current

    val channelColor = when (event.channel) {
        "remote" -> Color(0xFF6366F1)
        "lan"    -> Color(0xFF10B981)
        "rss"    -> Color(0xFFF59E0B)
        else     -> cs.primary
    }

    Card(
        onClick   = onTap,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (!event.isRead) cs.primaryContainer.copy(alpha = 0.15f) else cs.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(channelColor))
                if (!event.isRead) Box(Modifier.size(6.dp).clip(CircleShape).background(cs.primary))
                if (event.category == "silent") Icon(Icons.Default.VolumeOff, null, Modifier.size(12.dp), tint = cs.onSurfaceVariant)
                if (event.isPinned) Icon(Icons.Default.PushPin, null, Modifier.size(12.dp), tint = cs.primary)
                Text(event.title, style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(TimeUtils.relative(event.timestamp),
                    style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
            }
            Spacer(Modifier.height(4.dp))
            Text(event.body, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant,
                maxLines = if (expanded) Int.MAX_VALUE else 2, overflow = TextOverflow.Ellipsis)

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = cs.outline.copy(alpha = 0.3f))
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AssistChip({}, label = { Text(event.source, style = MaterialTheme.typography.labelSmall) })
                        AssistChip({}, label = { Text(event.channel.uppercase(), style = MaterialTheme.typography.labelSmall) })
                        AssistChip({}, label = { Text(event.group, style = MaterialTheme.typography.labelSmall) })
                    }
                    if (event.rawPayload.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Surface(color = cs.background, shape = RoundedCornerShape(8.dp)) {
                            Text(event.rawPayload, Modifier.padding(10.dp).fillMaxWidth(),
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = cs.onSurfaceVariant)
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton({ clip.setText(AnnotatedString(event.rawPayload.ifBlank { event.body })) }) {
                            Icon(Icons.Default.ContentCopy, null, Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp)); Text("Copy")
                        }
                        TextButton(onPin) {
                            Icon(Icons.Default.PushPin, null, Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp)); Text(if (event.isPinned) "Unpin" else "Pin")
                        }
                        TextButton(onDelete) {
                            Icon(Icons.Default.Delete, null, Modifier.size(14.dp), tint = cs.error)
                            Spacer(Modifier.width(4.dp)); Text("Delete", color = cs.error)
                        }
                    }
                }
            }
        }
    }
}

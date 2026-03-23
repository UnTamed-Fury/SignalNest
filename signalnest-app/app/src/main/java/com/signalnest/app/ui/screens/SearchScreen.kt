package fury.signalnest.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fury.signalnest.app.ui.viewmodels.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(vm: SearchViewModel, onBack: () -> Unit) {
    val cs      = MaterialTheme.colorScheme
    val query   by vm.query.collectAsStateWithLifecycle()
    val results by vm.results.collectAsStateWithLifecycle()
    val fr      = remember { FocusRequester() }
    val fm      = LocalFocusManager.current
    var expandedId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                title = {
                    OutlinedTextField(
                        value         = query,
                        onValueChange = vm::setQuery,
                        placeholder   = { Text("Search events…") },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth().focusRequester(fr),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { fm.clearFocus() }),
                        trailingIcon  = {
                            AnimatedVisibility(query.isNotEmpty()) {
                                IconButton({ vm.clear(); fr.requestFocus() }) {
                                    Icon(Icons.Default.Close, "Clear")
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = cs.primary,
                            unfocusedBorderColor = cs.outline,
                        ),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.background),
            )
        },
        containerColor = cs.background,
    ) { pad ->
        when {
            query.isBlank() -> {
                Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Search, null, Modifier.size(64.dp),
                            tint = cs.onSurfaceVariant.copy(alpha = 0.2f))
                        Spacer(Modifier.height(12.dp))
                        Text("Type to search events", color = cs.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            results.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                    Text("No results for \"$query\"", color = cs.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                }
            }
            else -> {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start  = 12.dp, end = 12.dp,
                        top    = pad.calculateTopPadding() + 8.dp,
                        bottom = 80.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        Text("${results.size} result${if (results.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp))
                    }
                    items(results, key = { it.id }) { event ->
                        EventCard(
                            event    = event,
                            expanded = expandedId == event.id,
                            onTap    = {
                                expandedId = if (expandedId == event.id) null else event.id
                                if (!event.isRead) vm.markRead(event.id)
                            },
                            onPin    = {},
                            onDelete = { vm.delete(event) },
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) { fr.requestFocus() }
}

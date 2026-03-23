package fury.signalnest.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import fury.signalnest.app.server.ConnectionService
import fury.signalnest.app.ui.navigation.*
import fury.signalnest.app.ui.viewmodels.*

@Composable
fun MainScaffold(settingsVm: SettingsViewModel) {
    val ctx     = LocalContext.current
    val nav     = rememberNavController()

    val feedVm:   FeedViewModel   = viewModel()
    val notesVm:  NotesViewModel  = viewModel()
    val todosVm:  TodosViewModel  = viewModel()
    val rssVm:    RssViewModel    = viewModel()
    val searchVm: SearchViewModel = viewModel()
    val rulesVm:  RulesViewModel  = viewModel()
    val exportVm: ExportViewModel = viewModel()

    val unread       by feedVm.unreadCount.collectAsStateWithLifecycle()
    val currentRoute = nav.currentBackStackEntryAsState().value?.destination?.route

    // Notification permission
    var notifOk by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= 33)
                ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED
            else true
        )
    }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notifOk = it
        if (it) ConnectionService.start(ctx)
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 && !notifOk)
            permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        else
            ConnectionService.start(ctx)
    }

    // Routes that should show the bottom bar
    val bottomRoutes = bottomNavItems.map { it.screen.route }.toSet()
    val showBottomBar = currentRoute in bottomRoutes

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter   = slideInVertically { it },
                exit    = slideOutVertically { it },
            ) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick  = {
                                nav.navigate(item.screen.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true; restoreState = true
                                }
                            },
                            icon  = {
                                BadgedBox(badge = {
                                    if (item.screen == Screen.Feed && unread > 0)
                                        Badge { Text(if (unread > 99) "99+" else "$unread") }
                                }) { Icon(if (selected) item.filled else item.outline, item.screen.label) }
                            },
                            label = { Text(item.screen.label) },
                        )
                    }
                }
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad)) {
            // Notification permission banner
            if (!notifOk) {
                Surface(color = MaterialTheme.colorScheme.errorContainer) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Notification permission needed", Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer)
                        TextButton({
                            if (Build.VERSION.SDK_INT >= 33)
                                permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }) { Text("Grant", color = MaterialTheme.colorScheme.onErrorContainer) }
                    }
                }
            }

            NavHost(nav, startDestination = Screen.Feed.route, Modifier.weight(1f)) {
                // ── Bottom nav screens ─────────────────────────────────────────
                composable(Screen.Feed.route) {
                    FeedScreen(feedVm, onSearch = { nav.navigate(Screen.Search.route) })
                }
                composable(Screen.Notes.route)    { NotesScreen(notesVm) }
                composable(Screen.Todos.route)    { TodosScreen(todosVm) }
                composable(Screen.Rss.route)      { RssScreen(rssVm) }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        vm          = settingsVm,
                        onFilters   = { nav.navigate(Screen.Filters.route) },
                        onBackup    = { nav.navigate(Screen.ExportImport.route) },
                    )
                }
                composable(Screen.About.route)    { AboutScreen() }

                // ── Phase 2 screens (no bottom bar) ────────────────────────────
                composable(Screen.Search.route) {
                    SearchScreen(searchVm, onBack = { nav.popBackStack() })
                }
                composable(Screen.Filters.route) {
                    FiltersScreen(rulesVm, onBack = { nav.popBackStack() })
                }
                composable(Screen.ExportImport.route) {
                    ExportImportScreen(exportVm, onBack = { nav.popBackStack() })
                }
            }
        }
    }
}

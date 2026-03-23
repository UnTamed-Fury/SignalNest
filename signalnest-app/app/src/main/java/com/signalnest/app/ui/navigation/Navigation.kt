package com.signalnest.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String) {
    // main nav
    object Onboarding    : Screen("onboarding",    "Setup")
    object Feed          : Screen("feed",           "Feed")
    object Notes         : Screen("notes",          "Notes")
    object Todos         : Screen("todos",          "Todos")
    object Rss           : Screen("rss",            "RSS")
    object Settings      : Screen("settings",       "Settings")
    object About         : Screen("about",          "About")
    // Phase 2 — accessed from top bar / settings, not bottom nav
    object Search        : Screen("search",         "Search")
    object Filters       : Screen("filters",        "Rules")
    object ExportImport  : Screen("export_import",  "Backup")
}

data class NavItem(val screen: Screen, val filled: ImageVector, val outline: ImageVector)

val bottomNavItems = listOf(
    NavItem(Screen.Feed,     Icons.Filled.DynamicFeed,  Icons.Outlined.DynamicFeed),
    NavItem(Screen.Notes,    Icons.Filled.StickyNote2,  Icons.Outlined.StickyNote2),
    NavItem(Screen.Todos,    Icons.Filled.CheckCircle,  Icons.Outlined.CheckCircle),
    NavItem(Screen.Rss,      Icons.Filled.RssFeed,      Icons.Outlined.RssFeed),
    NavItem(Screen.Settings, Icons.Filled.Settings,     Icons.Outlined.Settings),
)

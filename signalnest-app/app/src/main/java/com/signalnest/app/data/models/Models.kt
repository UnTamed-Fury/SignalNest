package com.signalnest.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

// ── Webhook event (feed) ──────────────────────────────────────────────────────
@Entity(tableName = "events")
data class Event(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val source: String,          // "github", "uptime-kuma", "custom", "rss", etc.
    val category: String = "normal",  // "normal" | "silent"
    val group: String   = "default",
    val rawPayload: String = "",
    val channel: String = "remote",   // "remote" | "lan"
    val isRead: Boolean = false,
    val isPinned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
)

// ── Note ──────────────────────────────────────────────────────────────────────
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val content: String = "",        // Markdown supported
    val colorIndex: Int = 0,
    val isPinned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

// ── Todo ──────────────────────────────────────────────────────────────────────
@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val isDone: Boolean = false,
    val dueAt: Long? = null,          // epoch ms — null means no due date
    val cronExpr: String = "",        // cron expression for recurring reminders
    val priority: Int = 0,           // 0=normal 1=high 2=urgent
    val createdAt: Long = System.currentTimeMillis(),
)

// ── RSS feed ──────────────────────────────────────────────────────────────────
@Entity(tableName = "rss_feeds")
data class RssFeed(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val isEnabled: Boolean = true,
    val intervalMinutes: Int = 30,
    val lastFetchAt: Long = 0,
    val category: String = "rss",
    val notifySilent: Boolean = false,
)

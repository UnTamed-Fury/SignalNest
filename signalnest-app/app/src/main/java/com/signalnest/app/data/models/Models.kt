package fury.signalnest.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

// @Serializable is required for kotlinx.serialization (ExportManager / SignalNestBackup).
// @Entity is required for Room.  Both annotations can coexist on the same class.

@Serializable
@Entity(tableName = "events")
data class Event(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val source: String,
    val category: String  = "normal",
    val group: String     = "default",
    val rawPayload: String = "",
    val channel: String   = "remote",
    val isRead: Boolean   = false,
    val isPinned: Boolean = false,
    val timestamp: Long   = System.currentTimeMillis(),
)

@Serializable
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String     = "",
    val content: String   = "",
    val colorIndex: Int   = 0,
    val isPinned: Boolean = false,
    val timestamp: Long   = System.currentTimeMillis(),
    val updatedAt: Long   = System.currentTimeMillis(),
)

@Serializable
@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val isDone: Boolean     = false,
    val dueAt: Long?        = null,
    val cronExpr: String    = "",
    val priority: Int       = 0,
    val createdAt: Long     = System.currentTimeMillis(),
)

@Entity(tableName = "rss_feeds")          // not serialized in backup, no @Serializable needed
data class RssFeed(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val isEnabled: Boolean   = true,
    val intervalMinutes: Int = 30,
    val lastFetchAt: Long    = 0,
    val category: String     = "rss",
    val notifySilent: Boolean = false,
)

@Serializable
@Entity(tableName = "snrl_rules")
data class SnrlRule(
    @PrimaryKey val id: String,
    val name: String,
    val text: String,
    val enabled: Boolean = true,
    val order: Int       = 0,
    val createdAt: Long  = System.currentTimeMillis(),
    val updatedAt: Long  = System.currentTimeMillis(),
)

package fury.signalnest.app.data

import android.content.Context
import android.net.Uri
import fury.signalnest.app.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class SignalNestBackup(
    val version: Int = 2,
    val exportedAt: Long = System.currentTimeMillis(),
    val events: List<Event> = emptyList(),
    val notes: List<Note>   = emptyList(),
    val todos: List<Todo>   = emptyList(),
    val rules: List<SnrlRule> = emptyList(),
)

class ExportManager(private val db: AppDatabase) {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }

    /** Export everything to a JSON string. */
    suspend fun exportJson(): String = withContext(Dispatchers.IO) {
        // Collect each table — using a one-shot snapshot via first()
        val events = mutableListOf<Event>()
        val notes  = mutableListOf<Note>()
        val todos  = mutableListOf<Todo>()
        val rules  = mutableListOf<SnrlRule>()

        db.events().getRecent(10_000).collect { events.addAll(it); return@collect }
        db.notes().getAll().collect { notes.addAll(it); return@collect }
        db.todos().getAll().collect { todos.addAll(it); return@collect }
        db.rules().getAll().collect { rules.addAll(it); return@collect }

        val backup = SignalNestBackup(events = events, notes = notes, todos = todos, rules = rules)
        json.encodeToString(backup)
    }

    /** Write JSON export to a Uri (from SAF file picker). */
    suspend fun exportToUri(ctx: Context, uri: Uri) = withContext(Dispatchers.IO) {
        val data = exportJson()
        ctx.contentResolver.openOutputStream(uri)?.use { it.write(data.toByteArray()) }
    }

    /**
     * Import from JSON string.
     * Strategy: REPLACE existing rows (same id overwrites, new ids insert).
     * Returns a summary of what was imported.
     */
    suspend fun importJson(jsonStr: String): ImportResult = withContext(Dispatchers.IO) {
        val backup = runCatching { json.decodeFromString<SignalNestBackup>(jsonStr) }
            .getOrElse { throw IllegalArgumentException("Invalid backup file: ${it.message}") }

        if (backup.events.isNotEmpty()) db.events().insertAll(backup.events)
        backup.notes.forEach  { db.notes().insert(it) }
        backup.todos.forEach  { db.todos().insert(it) }
        if (backup.rules.isNotEmpty()) db.rules().insertAll(backup.rules)

        ImportResult(
            events = backup.events.size,
            notes  = backup.notes.size,
            todos  = backup.todos.size,
            rules  = backup.rules.size,
        )
    }

    /** Read JSON from a Uri and import. */
    suspend fun importFromUri(ctx: Context, uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        val data = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
            ?: throw IllegalStateException("Could not read file")
        importJson(data)
    }
}

data class ImportResult(val events: Int, val notes: Int, val todos: Int, val rules: Int) {
    override fun toString() = "Imported: $events events, $notes notes, $todos todos, $rules rules"
}

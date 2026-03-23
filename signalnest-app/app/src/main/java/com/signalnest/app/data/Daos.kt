package fury.signalnest.app.data

import androidx.room.*
import fury.signalnest.app.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao interface EventDao {
    @Query("SELECT * FROM events ORDER BY isPinned DESC, timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int = 500): Flow<List<Event>>

    // Phase 2: full-text search across title + body + source + group
    @Query("""
        SELECT * FROM events
        WHERE lower(title) LIKE '%' || lower(:q) || '%'
           OR lower(body)  LIKE '%' || lower(:q) || '%'
           OR lower(source)LIKE '%' || lower(:q) || '%'
           OR lower(`group`)LIKE '%' || lower(:q) || '%'
        ORDER BY isPinned DESC, timestamp DESC
        LIMIT 200
    """)
    fun search(q: String): Flow<List<Event>>

    @Query("SELECT COUNT(*) FROM events WHERE isRead = 0")
    fun unreadCount(): Flow<Int>

    @Query("SELECT * FROM events WHERE `group` = :group ORDER BY timestamp DESC")
    fun getByGroup(group: String): Flow<List<Event>>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(e: Event): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(list: List<Event>)
    @Delete suspend fun delete(e: Event)

    @Query("UPDATE events SET isRead = 1 WHERE id = :id")    suspend fun markRead(id: String)
    @Query("UPDATE events SET isRead = 1")                   suspend fun markAllRead()
    @Query("UPDATE events SET isPinned = :v WHERE id = :id") suspend fun setPin(id: String, v: Boolean)
    @Query("DELETE FROM events")                             suspend fun deleteAll()
    @Query("DELETE FROM events WHERE id = :id")             suspend fun deleteById(id: String)

    @Query("SELECT DISTINCT `group` FROM events ORDER BY `group`")
    fun allGroups(): Flow<List<String>>
}

@Dao interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAll(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(n: Note): Long
    @Update suspend fun update(n: Note)
    @Delete suspend fun delete(n: Note)
    @Query("DELETE FROM notes") suspend fun deleteAll()
}

@Dao interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY isDone ASC, priority DESC, createdAt DESC")
    fun getAll(): Flow<List<Todo>>

    @Query("SELECT * FROM todos WHERE isDone = 0 AND dueAt IS NOT NULL AND dueAt <= :nowMs")
    suspend fun getOverdue(nowMs: Long = System.currentTimeMillis()): List<Todo>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(t: Todo): Long
    @Update suspend fun update(t: Todo)
    @Delete suspend fun delete(t: Todo)
    @Query("UPDATE todos SET isDone = :done WHERE id = :id") suspend fun setDone(id: Long, done: Boolean)
}

@Dao interface RssFeedDao {
    @Query("SELECT * FROM rss_feeds ORDER BY title ASC")
    fun getAll(): Flow<List<RssFeed>>

    @Query("SELECT * FROM rss_feeds WHERE isEnabled = 1")
    suspend fun getEnabled(): List<RssFeed>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(f: RssFeed): Long
    @Update suspend fun update(f: RssFeed)
    @Delete suspend fun delete(f: RssFeed)
}

@Dao interface SnrlRuleDao {
    @Query("SELECT * FROM snrl_rules ORDER BY `order` ASC")
    fun getAll(): Flow<List<SnrlRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(r: SnrlRule): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(rules: List<SnrlRule>)
    @Update suspend fun update(r: SnrlRule)
    @Delete suspend fun delete(r: SnrlRule)
    @Query("DELETE FROM snrl_rules") suspend fun deleteAll()
}

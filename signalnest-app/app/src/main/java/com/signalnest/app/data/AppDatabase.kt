package fury.signalnest.app.data

import android.content.Context
import androidx.room.*
import fury.signalnest.app.data.models.*

@Database(
    entities     = [Event::class, Note::class, Todo::class, RssFeed::class, SnrlRule::class],
    version      = 2,            // bumped from 1 → 2 for SnrlRule table
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun events(): EventDao
    abstract fun notes(): NoteDao
    abstract fun todos(): TodoDao
    abstract fun feeds(): RssFeedDao
    abstract fun rules(): SnrlRuleDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(ctx: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(ctx.applicationContext, AppDatabase::class.java, "signalnest.db")
                .fallbackToDestructiveMigration()   // dev-friendly; swap for migrations in production
                .build().also { INSTANCE = it }
        }
    }
}

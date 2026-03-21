package com.signalnest.app.data

import android.content.Context
import androidx.room.*
import com.signalnest.app.data.models.Event
import com.signalnest.app.data.models.Note
import com.signalnest.app.data.models.RssFeed
import com.signalnest.app.data.models.Todo

@Database(
    entities     = [Event::class, Note::class, Todo::class, RssFeed::class],
    version      = 1,
    exportSchema = false,   // false avoids needing schemaLocation KSP arg on Termux
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun events(): EventDao
    abstract fun notes(): NoteDao
    abstract fun todos(): TodoDao
    abstract fun feeds(): RssFeedDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(ctx: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(ctx.applicationContext, AppDatabase::class.java, "signalnest.db")
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
        }
    }
}

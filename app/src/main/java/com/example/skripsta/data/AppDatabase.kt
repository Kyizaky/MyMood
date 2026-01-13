package com.example.skripsta.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.skripsta.data.dao.ActivityDao
import com.example.skripsta.data.dao.FeelingDao
import com.example.skripsta.data.dao.IconDao
import com.example.skripsta.data.dao.MoodEntryDao
import com.example.skripsta.data.dao.UserDao
import com.example.skripsta.data.entity.Activity
import com.example.skripsta.data.entity.Feeling
import com.example.skripsta.data.entity.Icon
import com.example.skripsta.data.entity.MoodEntry
import com.example.skripsta.data.entity.User

@Database(entities = [Feeling::class, User::class, Activity::class, MoodEntry::class, Icon::class], version = 21, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun feelingDao(): FeelingDao
    abstract fun userDao(): UserDao
    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun activityDao(): ActivityDao
    abstract fun iconDao(): IconDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}

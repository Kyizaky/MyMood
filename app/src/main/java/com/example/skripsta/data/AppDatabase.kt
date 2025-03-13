package com.example.skripsta.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class, ActivityEntity::class], version = 1, exportSchema = false) // Tambahkan ActivityEntity
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun activityDao(): ActivityDao

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
                    "app_database" // Ubah nama database jika perlu
                )
                    .fallbackToDestructiveMigration() // Hindari crash jika ada perubahan versi
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}

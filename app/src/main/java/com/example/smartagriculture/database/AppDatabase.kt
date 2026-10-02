package com.example.smartagriculture.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.smartagriculture.model.ScanHistoryItem
import com.example.smartagriculture.model.User

@Database(
    entities = [
        User::class,
        ScanHistoryItem::class,
        PrototypeEntity::class,
        CropActivityEntity::class,
        SchemeEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun scanHistoryDao(): ScanHistoryDao
    abstract fun prototypeDao(): PrototypeDao
    abstract fun cropActivityDao(): CropActivityDao
    abstract fun schemeDao(): SchemeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_agriculture_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
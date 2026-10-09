package com.example.smartagriculture.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 8,
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

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE scan_history ADD COLUMN chatHistoryJson TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE crop_activities ADD COLUMN isDaily INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN passwordSalt TEXT DEFAULT NULL")
                database.execSQL("ALTER TABLE users ADD COLUMN passwordHintQuestion TEXT DEFAULT NULL")
                database.execSQL("ALTER TABLE users ADD COLUMN passwordHintAnswerHash TEXT DEFAULT NULL")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_agriculture_database"
                )
                .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
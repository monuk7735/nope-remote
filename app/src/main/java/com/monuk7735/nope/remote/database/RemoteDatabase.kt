package com.monuk7735.nope.remote.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.monuk7735.nope.remote.models.database.MacroDataDBModel
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel

@Database(
    entities = [
        RemoteDataDBModel::class,
        MacroDataDBModel::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RemoteDatabase : RoomDatabase() {

    abstract fun remoteDao(): RemoteDao

    abstract fun macroDao(): MacroDao

    companion object {

        @Volatile
        private var INSTANCE: RemoteDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE remotes ADD COLUMN preferCustomUi INTEGER NOT NULL DEFAULT 1")
            }
        }

        fun getDatabase(context: Context): RemoteDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null)
                return tempInstance

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RemoteDatabase::class.java,
                    "remotes"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration(false)
                    .build()

                INSTANCE = instance
                return instance
            }
        }
    }

}
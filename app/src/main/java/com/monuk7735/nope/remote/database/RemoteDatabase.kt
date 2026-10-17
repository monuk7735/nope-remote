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
        MacroDataDBModel::class,
    ],
    version = 5,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class RemoteDatabase : RoomDatabase() {

    abstract fun remoteDao(): RemoteDao

    abstract fun macroDao(): MacroDao

    companion object {

        @Volatile
        private var instance: RemoteDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE remotes ADD COLUMN preferCustomUi INTEGER NOT NULL DEFAULT 1",
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE remotes ADD COLUMN model TEXT DEFAULT NULL")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE remotes ADD COLUMN isCustom INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): RemoteDatabase {
            val tempInstance = instance
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    RemoteDatabase::class.java,
                    "remotes",
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration(false)
                    .build()

                instance = newInstance
                return newInstance
            }
        }
    }
}

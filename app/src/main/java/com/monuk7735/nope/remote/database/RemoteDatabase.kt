package com.monuk7735.nope.remote.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.monuk7735.nope.remote.models.database.MacroDataDBModel
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel

@Database(
    entities = [
        RemoteDataDBModel::class,
        MacroDataDBModel::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RemoteDatabase : RoomDatabase() {

    abstract fun remoteDao(): RemoteDao

    abstract fun macroDao(): MacroDao

    companion object {

        @Volatile
        private var INSTANCE: RemoteDatabase? = null

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
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                return instance
            }
        }
    }

}
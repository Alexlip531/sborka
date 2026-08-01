package com.zai.sugar.data.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zai.sugar.data.dao.PressureDao
import com.zai.sugar.data.dao.SugarDao
import com.zai.sugar.data.entity.PressureMeasurement
import com.zai.sugar.data.entity.SugarMeasurement

@Database(
    entities = [SugarMeasurement::class, PressureMeasurement::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sugarDao(): SugarDao
    abstract fun pressureDao(): PressureDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sugar.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}

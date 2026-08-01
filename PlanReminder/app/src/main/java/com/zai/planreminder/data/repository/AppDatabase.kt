package com.zai.planreminder.data.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zai.planreminder.data.dao.PlanDao
import com.zai.planreminder.data.entity.Plan

@Database(entities = [Plan::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun planDao(): PlanDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "plan_reminder.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}

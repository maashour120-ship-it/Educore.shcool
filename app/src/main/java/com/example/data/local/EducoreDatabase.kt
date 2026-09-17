package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        GroupEntity::class,
        StudentEntity::class,
        LessonEntity::class,
        ExamEntity::class,
        GradeEntity::class,
        AttendanceEntity::class,
        WarningEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EducoreDatabase : RoomDatabase() {
    abstract fun educoreDao(): EducoreDao

    companion object {
        @Volatile
        private var INSTANCE: EducoreDatabase? = null

        fun getDatabase(context: Context): EducoreDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EducoreDatabase::class.java,
                    "educore_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

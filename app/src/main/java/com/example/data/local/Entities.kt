package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val gradeLevel: String,
    val subject: String,
    val scheduleDays: String,
    val maxCapacity: Int = 35,
    val enrolledCount: Int = 0,
    val teacherCode: String = "mahmoud123"
)

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val parentPhone: String,
    val groupId: String,
    val groupName: String,
    val teacherCode: String,
    val attendanceRate: Int = 90,
    val averageGrade: Int = 85,
    val warningsCount: Int = 0
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val groupId: String,
    val groupName: String,
    val startTime: String,
    val endTime: String,
    val date: String,
    val teacherCode: String,
    val isUpcoming: Boolean = true,
    val qrCodeToken: String = ""
)

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val groupId: String,
    val groupName: String,
    val date: String,
    val maxScore: Int = 100,
    val teacherCode: String
)

@Entity(tableName = "grades")
data class GradeEntity(
    @PrimaryKey val id: String,
    val examId: String,
    val examTitle: String,
    val studentId: String,
    val studentName: String,
    val subject: String,
    val score: Int,
    val maxScore: Int = 100,
    val date: String,
    val teacherFeedback: String = ""
)

@Entity(tableName = "attendance_records")
data class AttendanceEntity(
    @PrimaryKey val id: String,
    val lessonId: String,
    val lessonTitle: String,
    val studentId: String,
    val studentName: String,
    val groupId: String,
    val date: String,
    val status: String, // "PRESENT", "ABSENT", "LATE", "EXCUSED"
    val checkInTime: String
)

@Entity(tableName = "warnings")
data class WarningEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val studentName: String,
    val teacherCode: String,
    val teacherName: String,
    val reason: String,
    val severity: String, // "LOW", "MEDIUM", "HIGH"
    val date: String
)

package com.example.model

enum class UserRole(val code: String, val titleAr: String, val titleEn: String) {
    STUDENT("student", "طالب", "Student"),
    TEACHER("teacher", "أستاذ / معلم", "Teacher"),
    SUPERVISOR("supervisor", "مشرف", "Supervisor")
}

data class UserSession(
    val id: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val role: UserRole,
    val isEmailVerified: Boolean = true,
    val avatarUrl: String? = null,
    val parentPhoneNumber: String? = null,
    val customCode: String? = null,
    val teacherCodes: List<String> = emptyList(),
    val groupName: String? = null
)

data class StatMetric(
    val title: String,
    val value: String,
    val subtitle: String,
    val iconType: String,
    val isWarning: Boolean = false,
    val isSuccess: Boolean = false
)

data class LessonItem(
    val id: String,
    val title: String,
    val subject: String,
    val groupName: String,
    val startTime: String,
    val endTime: String,
    val isUpcoming: Boolean = true,
    val studentsCount: Int = 30
)

data class GradeItem(
    val id: String,
    val subject: String,
    val score: Int,
    val maxScore: Int = 100,
    val examName: String,
    val date: String,
    val teacherName: String
)

data class AttendanceSummary(
    val totalPresent: Int,
    val totalAbsent: Int,
    val averageScore: Int,
    val todayLessonsCount: Int
)

data class GroupItem(
    val id: String,
    val name: String,
    val subject: String,
    val code: String,
    val teacherName: String,
    val studentsCount: Int,
    val description: String
)

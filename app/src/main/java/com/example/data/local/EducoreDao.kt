package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EducoreDao {

    // Groups
    @Query("SELECT * FROM groups ORDER BY name ASC")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups")
    suspend fun getGroupsSync(): List<GroupEntity>

    @Query("SELECT * FROM groups WHERE id = :groupId LIMIT 1")
    suspend fun getGroupById(groupId: String): GroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<GroupEntity>)

    @Query("DELETE FROM groups WHERE id = :groupId")
    suspend fun deleteGroup(groupId: String)

    // Students
    @Query("SELECT * FROM students ORDER BY fullName ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students")
    suspend fun getStudentsSync(): List<StudentEntity>

    @Query("SELECT * FROM students WHERE groupId = :groupId ORDER BY fullName ASC")
    fun getStudentsByGroup(groupId: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :studentId LIMIT 1")
    suspend fun getStudentById(studentId: String): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE id = :studentId")
    suspend fun deleteStudent(studentId: String)

    // Lessons
    @Query("SELECT * FROM lessons ORDER BY date ASC, startTime ASC")
    fun getAllLessons(): Flow<List<LessonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    // Exams
    @Query("SELECT * FROM exams ORDER BY date DESC")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams")
    suspend fun getExamsSync(): List<ExamEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExams(exams: List<ExamEntity>)

    // Grades
    @Query("SELECT * FROM grades WHERE studentId = :studentId ORDER BY date DESC")
    fun getGradesByStudent(studentId: String): Flow<List<GradeEntity>>

    @Query("SELECT * FROM grades WHERE examId = :examId")
    fun getGradesByExam(examId: String): Flow<List<GradeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: GradeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrades(grades: List<GradeEntity>)

    // Attendance
    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceByStudent(studentId: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records")
    suspend fun getAttendanceSync(): List<AttendanceEntity>

    @Query("SELECT * FROM attendance_records WHERE lessonId = :lessonId")
    fun getAttendanceByLesson(lessonId: String): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(record: AttendanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(records: List<AttendanceEntity>)

    // Warnings
    @Query("SELECT * FROM warnings WHERE studentId = :studentId ORDER BY date DESC")
    fun getWarningsByStudent(studentId: String): Flow<List<WarningEntity>>

    @Query("SELECT * FROM warnings ORDER BY date DESC")
    fun getAllWarnings(): Flow<List<WarningEntity>>

    @Query("SELECT * FROM warnings")
    suspend fun getWarningsSync(): List<WarningEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarning(warning: WarningEntity)
}

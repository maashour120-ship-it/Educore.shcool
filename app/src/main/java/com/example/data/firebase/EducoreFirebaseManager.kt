package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.core.security.EducoreSecurityManager
import com.example.data.local.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class CloudSyncState {
    data object Idle : CloudSyncState()
    data class Syncing(val message: String = "جاري مزامنة البيانات مع سحابة Firebase...") : CloudSyncState()
    data class Success(val message: String, val syncedCount: Int) : CloudSyncState()
    data class Error(val message: String) : CloudSyncState()
    data class NotConfigured(val message: String = "يرجى إضافة ملف google-services.json لتفعيل الاتصال بالسيرفر السحابي") : CloudSyncState()
}

/**
 * Enterprise Firebase Integration Service for Educore Platform.
 * Connects Local Room Database to Google Cloud Firestore & Firebase Authentication.
 */
object EducoreFirebaseManager {
    private const val TAG = "EducoreFirebase"

    private val _syncState = MutableStateFlow<CloudSyncState>(CloudSyncState.Idle)
    val syncState: StateFlow<CloudSyncState> = _syncState.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<Long?>(null)
    val lastSyncTimestamp: StateFlow<Long?> = _lastSyncTimestamp.asStateFlow()

    fun isConfigured(context: Context): Boolean {
        return try {
            ensureInitialized(context)
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    fun ensureInitialized(context: Context): Boolean {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = com.google.firebase.FirebaseOptions.Builder()
                    .setApplicationId("1:855006195292:android:47920deda80e8636234099")
                    .setApiKey("AIzaSyDlu-o2BgI8cwGeJdmvqJ39rZBVQMk_u7c")
                    .setProjectId("educore-app-a0155")
                    .setStorageBucket("educore-app-a0155.firebasestorage.app")
                    .setGcmSenderId("855006195292")
                    .build()
                FirebaseApp.initializeApp(context.applicationContext, options)
            }
            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to initialize FirebaseApp: ${e.message}")
            false
        }
    }

    private fun getFirestore(context: Context): FirebaseFirestore? {
        return try {
            ensureInitialized(context)
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore not initialized: ${e.message}")
            null
        }
    }

    private fun getAuth(context: Context): FirebaseAuth? {
        return try {
            ensureInitialized(context)
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth not initialized: ${e.message}")
            null
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 1. Bidirectional Cloud Firestore Sync
    // ─────────────────────────────────────────────────────────────

    /**
     * Pushes all local Room DB data (groups, students, attendance, exams, warnings) to Firebase Firestore
     */
    suspend fun pushLocalDataToCloud(context: Context, dao: EducoreDao): Result<Int> = withContext(Dispatchers.IO) {
        val firestore = getFirestore(context)
        if (firestore == null) {
            _syncState.value = CloudSyncState.NotConfigured()
            return@withContext Result.failure(Exception("Firebase غير مهيأ بعد على هذا الجهاز"))
        }

        _syncState.value = CloudSyncState.Syncing("جاري رفع البيانات إلى Cloud Firestore...")
        try {
            var count = 0

            // 1. Sync Groups
            val groups = dao.getGroupsSync()
            for (group in groups) {
                firestore.collection("groups").document(group.id).set(
                    mapOf(
                        "id" to group.id,
                        "name" to group.name,
                        "gradeLevel" to group.gradeLevel,
                        "subject" to group.subject,
                        "scheduleDays" to group.scheduleDays,
                        "maxCapacity" to group.maxCapacity,
                        "enrolledCount" to group.enrolledCount,
                        "teacherCode" to group.teacherCode,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                ).await()
                count++
            }

            // 2. Sync Students
            val students = dao.getStudentsSync()
            for (student in students) {
                firestore.collection("students").document(student.id).set(
                    mapOf(
                        "id" to student.id,
                        "fullName" to student.fullName,
                        "email" to student.email,
                        "phone" to student.phone,
                        "parentPhone" to student.parentPhone,
                        "groupId" to student.groupId,
                        "groupName" to student.groupName,
                        "teacherCode" to student.teacherCode,
                        "attendanceRate" to student.attendanceRate,
                        "averageGrade" to student.averageGrade,
                        "warningsCount" to student.warningsCount,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                ).await()
                count++
            }

            // 3. Sync Exams
            val exams = dao.getExamsSync()
            for (exam in exams) {
                firestore.collection("exams").document(exam.id).set(
                    mapOf(
                        "id" to exam.id,
                        "title" to exam.title,
                        "subject" to exam.subject,
                        "groupId" to exam.groupId,
                        "groupName" to exam.groupName,
                        "date" to exam.date,
                        "maxScore" to exam.maxScore,
                        "teacherCode" to exam.teacherCode,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                ).await()
                count++
            }

            // 4. Sync Attendance Records
            val attendance = dao.getAttendanceSync()
            for (record in attendance) {
                firestore.collection("attendance_records").document(record.id).set(
                    mapOf(
                        "id" to record.id,
                        "lessonId" to record.lessonId,
                        "lessonTitle" to record.lessonTitle,
                        "studentId" to record.studentId,
                        "studentName" to record.studentName,
                        "groupId" to record.groupId,
                        "date" to record.date,
                        "status" to record.status,
                        "checkInTime" to record.checkInTime,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                ).await()
                count++
            }

            // 5. Sync Warnings
            val warnings = dao.getWarningsSync()
            for (warning in warnings) {
                firestore.collection("warnings").document(warning.id).set(
                    mapOf(
                        "id" to warning.id,
                        "studentId" to warning.studentId,
                        "studentName" to warning.studentName,
                        "teacherCode" to warning.teacherCode,
                        "teacherName" to warning.teacherName,
                        "reason" to warning.reason,
                        "severity" to warning.severity,
                        "date" to warning.date,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                ).await()
                count++
            }

            _lastSyncTimestamp.value = System.currentTimeMillis()
            _syncState.value = CloudSyncState.Success("تمت مزامنة ورفع $count عنصراً بنجاح إلى السحابة", count)
            EducoreSecurityManager.addAuditLog("☁️ تمت مزامنة $count عنصراً بنجاح مع سيرفر Firebase Firestore")
            Result.success(count)
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "حدث خطأ أثناء الاتصال بالسيرفر السحابي"
            _syncState.value = CloudSyncState.Error(errorMsg)
            EducoreSecurityManager.addAuditLog("⚠️ فشلت المزامنة السحابية: $errorMsg")
            Result.failure(e)
        }
    }

    /**
     * Pulls data from Firebase Firestore down to local Room Database
     */
    suspend fun pullCloudDataToLocal(context: Context, dao: EducoreDao): Result<Int> = withContext(Dispatchers.IO) {
        val firestore = getFirestore(context)
        if (firestore == null) {
            _syncState.value = CloudSyncState.NotConfigured()
            return@withContext Result.failure(Exception("Firebase غير مهيأ بعد على هذا الجهاز"))
        }

        _syncState.value = CloudSyncState.Syncing("جاري سحب التحديثات من Firebase Firestore...")
        try {
            var count = 0

            // 1. Pull Groups
            val groupsSnapshot = firestore.collection("groups").get().await()
            val cloudGroups = groupsSnapshot.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val name = doc.getString("name") ?: return@mapNotNull null
                GroupEntity(
                    id = id,
                    name = name,
                    gradeLevel = doc.getString("gradeLevel") ?: "الصف الثالث الثانوي",
                    subject = doc.getString("subject") ?: "فيزياء",
                    scheduleDays = doc.getString("scheduleDays") ?: "",
                    maxCapacity = (doc.getLong("maxCapacity") ?: 35L).toInt(),
                    enrolledCount = (doc.getLong("enrolledCount") ?: 0L).toInt(),
                    teacherCode = doc.getString("teacherCode") ?: "mahmoud123"
                )
            }
            if (cloudGroups.isNotEmpty()) {
                dao.insertGroups(cloudGroups)
                count += cloudGroups.size
            }

            // 2. Pull Students
            val studentsSnapshot = firestore.collection("students").get().await()
            val cloudStudents = studentsSnapshot.documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: doc.id
                val fullName = doc.getString("fullName") ?: return@mapNotNull null
                StudentEntity(
                    id = id,
                    fullName = fullName,
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("phone") ?: "",
                    parentPhone = doc.getString("parentPhone") ?: "",
                    groupId = doc.getString("groupId") ?: "group-1",
                    groupName = doc.getString("groupName") ?: "المجموعة العامة",
                    teacherCode = doc.getString("teacherCode") ?: "mahmoud123",
                    attendanceRate = (doc.getLong("attendanceRate") ?: 90L).toInt(),
                    averageGrade = (doc.getLong("averageGrade") ?: 85L).toInt(),
                    warningsCount = (doc.getLong("warningsCount") ?: 0L).toInt()
                )
            }
            if (cloudStudents.isNotEmpty()) {
                dao.insertStudents(cloudStudents)
                count += cloudStudents.size
            }

            _lastSyncTimestamp.value = System.currentTimeMillis()
            _syncState.value = CloudSyncState.Success("تم سحب وتحديث $count عنصراً من السحابة", count)
            EducoreSecurityManager.addAuditLog("☁️ تم جلب وتحديث $count عنصراً من سيرفر Firebase Firestore إلى قاعدة البيانات المحلية")
            Result.success(count)
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "حدث خطأ أثناء جلب البيانات من السيرفر"
            _syncState.value = CloudSyncState.Error(errorMsg)
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 2. Firebase Authentication Integration
    // ─────────────────────────────────────────────────────────────

    suspend fun firebaseSignUp(context: Context, email: String, pass: String): Result<String> = withContext(Dispatchers.IO) {
        val auth = getAuth(context) ?: return@withContext Result.failure(Exception("Firebase Auth غير متاح"))
        try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val uid = authResult.user?.uid ?: ""
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun firebaseSignIn(context: Context, email: String, pass: String): Result<String> = withContext(Dispatchers.IO) {
        val auth = getAuth(context) ?: return@withContext Result.failure(Exception("Firebase Auth غير متاح"))
        try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), pass).await()
            val uid = authResult.user?.uid ?: ""
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package com.example.data

import android.content.Context
import com.example.core.security.EducoreSecurityManager
import com.example.data.local.AttendanceEntity
import com.example.data.local.EducoreDao
import com.example.data.local.EducoreDatabase
import com.example.data.local.ExamEntity
import com.example.data.local.GradeEntity
import com.example.data.local.GroupEntity
import com.example.data.local.LessonEntity
import com.example.data.local.StudentEntity
import com.example.data.local.WarningEntity
import com.example.model.AttendanceSummary
import com.example.model.GradeItem
import com.example.model.GroupItem
import com.example.model.LessonItem
import com.example.model.UserRole
import com.example.model.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

sealed class AuthResult {
    data class Success(val session: UserSession) : AuthResult()
    data class NeedsEmailVerification(val email: String) : AuthResult()
    data class Error(val messageAr: String, val messageEn: String) : AuthResult()
}

class AuthRepository {

    // Pre-registered mock users and teacher codes
    private val validTeacherCodes = mutableSetOf(
        "mahmoud123", "AHMED-BIO-2025", "EDU-7K92", "MATH-SEC3", "CHEM-PRO", "PHYS-2025"
    )

    private val userCredentials = mutableMapOf(
        "ahmed.student@educore.edu" to EducoreSecurityManager.hashPassword("123456"),
        "ahmed.teacher@educore.edu" to EducoreSecurityManager.hashPassword("123456"),
        "mostafa.super@educore.edu" to EducoreSecurityManager.hashPassword("123456")
    )

    private val userDatabase = mutableListOf(
        UserSession(
            id = "student-001",
            fullName = "أحمد محمد",
            email = "ahmed.student@educore.edu",
            phoneNumber = "01012345678",
            role = UserRole.STUDENT,
            isEmailVerified = true,
            parentPhoneNumber = "01198765432",
            teacherCodes = listOf("mahmoud123", "EDU-7K92"),
            groupName = "مجموعة ثالثة ثانوي (أ)"
        ),
        UserSession(
            id = "teacher-001",
            fullName = "أحمد محمد",
            email = "ahmed.teacher@educore.edu",
            phoneNumber = "01234567890",
            role = UserRole.TEACHER,
            isEmailVerified = true,
            customCode = "mahmoud123"
        ),
        UserSession(
            id = "supervisor-001",
            fullName = "د. مصطفى كامل",
            email = "mostafa.super@educore.edu",
            phoneNumber = "01511223344",
            role = UserRole.SUPERVISOR,
            isEmailVerified = true,
            customCode = "SUPER-ADMIN"
        )
    )

    private val _currentSession = MutableStateFlow<UserSession?>(null)
    val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    suspend fun login(email: String, pass: String, expectedRole: UserRole? = null): AuthResult {
        delay(500)
        val cleanEmail = email.trim().lowercase()

        // 1. Anti-Brute-Force Rate Limiting Check
        val (allowed, remainingSec) = EducoreSecurityManager.checkLoginAllowed(cleanEmail)
        if (!allowed) {
            return AuthResult.Error(
                "تم تجميد محاولات الدخول مؤقتاً لحماية حسابك بسبب محاولات متكررة خاطئة. يرجى الانتظار $remainingSec ثانية.",
                "Login temporarily locked for security due to multiple failed attempts. Please wait $remainingSec seconds."
            )
        }

        val user = userDatabase.find { it.email.lowercase() == cleanEmail }
        val storedHash = userCredentials[cleanEmail]

        if (user == null || (storedHash != null && !EducoreSecurityManager.verifyPassword(pass, storedHash))) {
            val failedCount = EducoreSecurityManager.recordFailedLogin(cleanEmail)
            val remainingAttempts = 5 - failedCount
            val attemptMsg = if (remainingAttempts > 0) " (المحاولات المتبقية: $remainingAttempts)" else " (تم قفل الحساب مؤقتاً)"
            return AuthResult.Error(
                "البريد الإلكتروني أو كلمة المرور غير صحيحة$attemptMsg",
                "Invalid email or password"
            )
        }

        if (expectedRole != null && user.role != expectedRole && !(expectedRole == UserRole.TEACHER && user.role == UserRole.SUPERVISOR)) {
            EducoreSecurityManager.recordFailedLogin(cleanEmail)
            return AuthResult.Error(
                "الحساب لا يملك صلاحية الدخول كـ ${expectedRole.titleAr}",
                "Account does not have ${expectedRole.titleEn} permissions"
            )
        }

        if (!user.isEmailVerified) {
            return AuthResult.NeedsEmailVerification(user.email)
        }

        // Record successful login (clears lockout counter)
        EducoreSecurityManager.recordSuccessfulLogin(cleanEmail)
        _currentSession.value = user
        return AuthResult.Success(user)
    }

    suspend fun registerStudent(
        fullName: String,
        email: String,
        password: String,
        phone: String,
        parentPhone: String,
        teacherCodes: List<String>
    ): AuthResult {
        delay(600)
        val cleanEmail = email.trim().lowercase()
        val sanitizedName = EducoreSecurityManager.sanitizeInput(fullName)
        val sanitizedPhone = EducoreSecurityManager.sanitizePhoneNumber(phone)
        val sanitizedParentPhone = EducoreSecurityManager.sanitizePhoneNumber(parentPhone)

        if (!EducoreSecurityManager.isValidEmail(cleanEmail)) {
            return AuthResult.Error("صيغة البريد الإلكتروني غير صالحة", "Invalid email format")
        }

        if (userDatabase.any { it.email.lowercase() == cleanEmail }) {
            return AuthResult.Error(
                "البريد الإلكتروني مسجل بالفعل",
                "Email is already registered"
            )
        }

        // Validate teacher codes
        val invalidCodes = teacherCodes.filter { it.isNotBlank() && !validTeacherCodes.contains(it.trim()) }
        if (invalidCodes.isNotEmpty()) {
            return AuthResult.Error(
                "أكواد المعلمين التالية غير صحيحة: ${invalidCodes.joinToString()}",
                "The following teacher codes are invalid: ${invalidCodes.joinToString()}"
            )
        }

        val newStudent = UserSession(
            id = "student-${System.currentTimeMillis()}",
            fullName = sanitizedName,
            email = cleanEmail,
            phoneNumber = sanitizedPhone,
            role = UserRole.STUDENT,
            isEmailVerified = true,
            parentPhoneNumber = sanitizedParentPhone,
            teacherCodes = teacherCodes.filter { it.isNotBlank() }.map { it.trim() },
            groupName = "مجموعة ثالثة ثانوي (أ)"
        )

        userCredentials[cleanEmail] = EducoreSecurityManager.hashPassword(password)
        userDatabase.add(newStudent)
        EducoreSecurityManager.recordSuccessfulLogin(cleanEmail)
        EducoreSecurityManager.addAuditLog("👤 تسجيل طالب جديد بنجاح: $sanitizedName ($cleanEmail)")
        _currentSession.value = newStudent
        return AuthResult.Success(newStudent)
    }

    suspend fun registerStaff(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        customCode: String,
        role: UserRole
    ): AuthResult {
        delay(600)
        val cleanEmail = email.trim().lowercase()
        val sanitizedName = EducoreSecurityManager.sanitizeInput(fullName)
        val sanitizedPhone = EducoreSecurityManager.sanitizePhoneNumber(phone)
        val cleanCode = EducoreSecurityManager.sanitizeInput(customCode).uppercase()

        if (!EducoreSecurityManager.isValidEmail(cleanEmail)) {
            return AuthResult.Error("صيغة البريد الإلكتروني غير صالحة", "Invalid email format")
        }

        if (userDatabase.any { it.email.lowercase() == cleanEmail }) {
            return AuthResult.Error(
                "البريد الإلكتروني مسجل بالفعل",
                "Email is already registered"
            )
        }

        if (validTeacherCodes.contains(cleanCode)) {
            return AuthResult.Error(
                "كود المعلم مستخدم بالفعل، يرجى اختيار كود فريد آخر",
                "Teacher code is already taken. Please choose another unique code."
            )
        }

        validTeacherCodes.add(cleanCode)

        val newStaff = UserSession(
            id = "staff-${System.currentTimeMillis()}",
            fullName = sanitizedName,
            email = cleanEmail,
            phoneNumber = sanitizedPhone,
            role = role,
            isEmailVerified = true,
            customCode = cleanCode
        )

        userCredentials[cleanEmail] = EducoreSecurityManager.hashPassword(password)
        userDatabase.add(newStaff)
        EducoreSecurityManager.recordSuccessfulLogin(cleanEmail)
        EducoreSecurityManager.addAuditLog("👨‍🏫 تسجيل معلم/مشرف جديد: $sanitizedName ($cleanCode)")
        _currentSession.value = newStaff
        return AuthResult.Success(newStaff)
    }


    suspend fun resendVerificationEmail(email: String): Boolean {
        delay(500)
        return true
    }

    suspend fun sendPasswordReset(email: String): Boolean {
        delay(500)
        return true
    }

    fun logout() {
        _currentSession.value = null
    }

    fun getValidTeacherCodes(): Set<String> = validTeacherCodes.toSet()
}

class EducoreDataRepository(context: Context? = null) {

    private val appContext: Context? = context?.applicationContext
    private val dao: EducoreDao? = context?.let { EducoreDatabase.getDatabase(it).educoreDao() }
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        if (dao != null) {
            repositoryScope.launch {
                seedInitialDataIfEmpty()
                // Auto-sync in background without user interaction
                triggerBackgroundCloudSync()
            }
        }
    }

    /**
     * Seamlessly syncs local changes to Cloud Firebase in background
     */
    private fun triggerBackgroundCloudSync() {
        val ctx = appContext ?: return
        val currentDao = dao ?: return
        repositoryScope.launch {
            try {
                com.example.data.firebase.EducoreFirebaseManager.pushLocalDataToCloud(ctx, currentDao)
            } catch (e: Exception) {
                android.util.Log.w("EducoreRepository", "Silent cloud sync: ${e.message}")
            }
        }
    }

    private suspend fun seedInitialDataIfEmpty() {
        if (dao == null) return
        
        // Seed Groups
        val initialGroups = listOf(
            GroupEntity(
                id = "group-1",
                name = "مجموعة ثالثة ثانوي (أ)",
                gradeLevel = "الصف الثالث الثانوي",
                subject = "فيزياء",
                scheduleDays = "السبت والثلاثاء 08:00 ص",
                maxCapacity = 35,
                enrolledCount = 30
            ),
            GroupEntity(
                id = "group-2",
                name = "مجموعة ثانية ثانوي (ب)",
                gradeLevel = "الصف الثاني الثانوي",
                subject = "فيزياء",
                scheduleDays = "الأحد والأربعاء 10:00 ص",
                maxCapacity = 30,
                enrolledCount = 28
            ),
            GroupEntity(
                id = "group-3",
                name = "مجموعة أولى ثانوي (ج)",
                gradeLevel = "الصف الأول الثانوي",
                subject = "علوم متكاملة",
                scheduleDays = "الإثنين والخميس 12:00 م",
                maxCapacity = 35,
                enrolledCount = 25
            )
        )
        dao.insertGroups(initialGroups)

        // Seed Students
        val initialStudents = listOf(
            StudentEntity(
                id = "student-001",
                fullName = "أحمد محمد محمود",
                email = "ahmed.student@educore.edu",
                phone = "01012345678",
                parentPhone = "01198765432",
                groupId = "group-1",
                groupName = "مجموعة ثالثة ثانوي (أ)",
                teacherCode = "mahmoud123",
                attendanceRate = 92,
                averageGrade = 90,
                warningsCount = 0
            ),
            StudentEntity(
                id = "student-002",
                fullName = "عمر خالد سليم",
                email = "omar.khaled@educore.edu",
                phone = "01055566677",
                parentPhone = "01233344455",
                groupId = "group-1",
                groupName = "مجموعة ثالثة ثانوي (أ)",
                teacherCode = "mahmoud123",
                attendanceRate = 85,
                averageGrade = 88,
                warningsCount = 1
            ),
            StudentEntity(
                id = "student-003",
                fullName = "مريم عبد الرحمن",
                email = "maryam.a@educore.edu",
                phone = "01122334455",
                parentPhone = "01066778899",
                groupId = "group-1",
                groupName = "مجموعة ثالثة ثانوي (أ)",
                teacherCode = "mahmoud123",
                attendanceRate = 98,
                averageGrade = 96,
                warningsCount = 0
            ),
            StudentEntity(
                id = "student-004",
                fullName = "يوسف إبراهيم علي",
                email = "youssef.i@educore.edu",
                phone = "01244556677",
                parentPhone = "01188990011",
                groupId = "group-2",
                groupName = "مجموعة ثانية ثانوي (ب)",
                teacherCode = "mahmoud123",
                attendanceRate = 78,
                averageGrade = 75,
                warningsCount = 2
            ),
            StudentEntity(
                id = "student-005",
                fullName = "سارة حسن فتحي",
                email = "sara.hassan@educore.edu",
                phone = "01511223344",
                parentPhone = "01099887766",
                groupId = "group-2",
                groupName = "مجموعة ثانية ثانوي (ب)",
                teacherCode = "mahmoud123",
                attendanceRate = 95,
                averageGrade = 94,
                warningsCount = 0
            )
        )
        dao.insertStudents(initialStudents)

        // Seed Lessons
        val initialLessons = listOf(
            LessonEntity(
                id = "lesson-1",
                title = "الفيزياء الحديثة وقانون أوم",
                subject = "فيزياء",
                groupId = "group-1",
                groupName = "مجموعة ثالثة ثانوي (أ)",
                startTime = "08:00 ص",
                endTime = "09:00 ص",
                date = "اليوم",
                teacherCode = "mahmoud123",
                isUpcoming = true,
                qrCodeToken = "EDU-PHYS-01"
            ),
            LessonEntity(
                id = "lesson-2",
                title = "التفاضل والتكامل والمصفوفات",
                subject = "رياضيات",
                groupId = "group-1",
                groupName = "مجموعة ثالثة ثانوي (أ)",
                startTime = "10:00 ص",
                endTime = "11:00 ص",
                date = "اليوم",
                teacherCode = "mahmoud123",
                isUpcoming = true,
                qrCodeToken = "EDU-MATH-02"
            ),
            LessonEntity(
                id = "lesson-3",
                title = "الكيمياء العضوية والروابط",
                subject = "كيمياء",
                groupId = "group-1",
                groupName = "مجموعة ثالثة ثانوي (أ)",
                startTime = "12:00 م",
                endTime = "01:00 م",
                date = "اليوم",
                teacherCode = "mahmoud123",
                isUpcoming = true,
                qrCodeToken = "EDU-CHEM-03"
            )
        )
        dao.insertLessons(initialLessons)

        // Seed Exams
        val initialExams = listOf(
            ExamEntity(
                id = "exam-1",
                title = "امتحان الفيزياء الشامل (الفصل الأول)",
                subject = "فيزياء",
                groupId = "group-1",
                groupName = "مجموعة ثالثة ثانوي (أ)",
                date = "2025/05/10",
                maxScore = 100,
                teacherCode = "mahmoud123"
            ),
            ExamEntity(
                id = "exam-2",
                title = "اختبار قصير: الدوائر الكهربية",
                subject = "فيزياء",
                groupId = "group-1",
                groupName = "مجموعة ثالثة ثانوي (أ)",
                date = "2025/05/03",
                maxScore = 50,
                teacherCode = "mahmoud123"
            )
        )
        dao.insertExams(initialExams)

        // Seed Grades
        val initialGrades = listOf(
            GradeEntity(
                id = "grade-1",
                examId = "exam-1",
                examTitle = "امتحان الفيزياء الشامل",
                studentId = "student-001",
                studentName = "أحمد محمد محمود",
                subject = "فيزياء",
                score = 90,
                maxScore = 100,
                date = "2025/05/10",
                teacherFeedback = "أداء ممتاز وإجابات دقيقة"
            ),
            GradeEntity(
                id = "grade-2",
                examId = "exam-2",
                examTitle = "اختبار الدوائر الكهربية",
                studentId = "student-001",
                studentName = "أحمد محمد محمود",
                subject = "فيزياء",
                score = 48,
                maxScore = 50,
                date = "2025/05/03",
                teacherFeedback = "رائع جداً"
            )
        )
        dao.insertGrades(initialGrades)

        // Seed Warnings
        val initialWarnings = listOf(
            WarningEntity(
                id = "warn-1",
                studentId = "student-002",
                studentName = "عمر خالد سليم",
                teacherCode = "mahmoud123",
                teacherName = "أ. محمود عبد العال",
                reason = "تأخر متكرر عن بدء الحصة لأكثر من 15 دقيقة",
                severity = "MEDIUM",
                date = "2025/05/02"
            )
        )
        dao.insertWarning(initialWarnings[0])
    }

    // Reactive Flow getters
    fun getAllGroupsFlow(): Flow<List<GroupEntity>>? = dao?.getAllGroups()
    fun getAllStudentsFlow(): Flow<List<StudentEntity>>? = dao?.getAllStudents()
    fun getStudentsByGroupFlow(groupId: String): Flow<List<StudentEntity>>? = dao?.getStudentsByGroup(groupId)
    fun getAllExamsFlow(): Flow<List<ExamEntity>>? = dao?.getAllExams()
    fun getGradesByStudentFlow(studentId: String): Flow<List<GradeEntity>>? = dao?.getGradesByStudent(studentId)
    fun getWarningsByStudentFlow(studentId: String): Flow<List<WarningEntity>>? = dao?.getWarningsByStudent(studentId)
    fun getAllWarningsFlow(): Flow<List<WarningEntity>>? = dao?.getAllWarnings()
    fun getAllLessonsFlow(): Flow<List<LessonEntity>>? = dao?.getAllLessons()

    // Suspend operations for CRUD with automatic silent background Cloud sync
    suspend fun addGroup(group: GroupEntity) {
        dao?.insertGroup(group)
        triggerBackgroundCloudSync()
    }

    suspend fun deleteGroup(groupId: String) {
        dao?.deleteGroup(groupId)
        triggerBackgroundCloudSync()
    }

    suspend fun addLesson(lesson: LessonEntity) {
        dao?.insertLesson(lesson)
        triggerBackgroundCloudSync()
    }

    suspend fun addStudent(student: StudentEntity) {
        dao?.insertStudent(student)
        triggerBackgroundCloudSync()
    }

    suspend fun deleteStudent(studentId: String) {
        dao?.deleteStudent(studentId)
        triggerBackgroundCloudSync()
    }

    suspend fun updateStudent(student: StudentEntity) {
        dao?.updateStudent(student)
        triggerBackgroundCloudSync()
    }

    suspend fun addExam(exam: ExamEntity) {
        dao?.insertExam(exam)
        triggerBackgroundCloudSync()
    }

    suspend fun addGrade(grade: GradeEntity) {
        dao?.insertGrade(grade)
        triggerBackgroundCloudSync()
    }

    suspend fun addGrades(grades: List<GradeEntity>) {
        dao?.insertGrades(grades)
        triggerBackgroundCloudSync()
    }

    suspend fun addAttendanceRecord(record: AttendanceEntity) {
        dao?.insertAttendance(record)
        triggerBackgroundCloudSync()
    }

    suspend fun addAttendanceRecords(records: List<AttendanceEntity>) {
        dao?.insertAttendanceList(records)
        triggerBackgroundCloudSync()
    }

    suspend fun addWarning(warning: WarningEntity) {
        dao?.insertWarning(warning)
        triggerBackgroundCloudSync()
    }

    // Synchronous fallback helpers
    fun getStudentAttendanceSummary(): AttendanceSummary {
        return AttendanceSummary(
            totalPresent = 28,
            totalAbsent = 4,
            averageScore = 92,
            todayLessonsCount = 6
        )
    }

    fun getStudentTodayLessons(): List<LessonItem> {
        return listOf(
            LessonItem(
                id = "lesson-1",
                title = "مادة الفيزياء",
                subject = "الفيزياء الحديثة وقانون أوم",
                groupName = "مجموعة ثالثة ثانوي (أ)",
                startTime = "08:00 ص",
                endTime = "09:00 ص",
                isUpcoming = true,
                studentsCount = 32
            ),
            LessonItem(
                id = "lesson-2",
                title = "مادة الرياضيات",
                subject = "التفاضل والتكامل والمصفوفات",
                groupName = "مجموعة ثالثة ثانوي (أ)",
                startTime = "10:00 ص",
                endTime = "11:00 ص",
                isUpcoming = true,
                studentsCount = 28
            ),
            LessonItem(
                id = "lesson-3",
                title = "مادة الكيمياء",
                subject = "الكيمياء العضوية والروابط الجزيئية",
                groupName = "مجموعة ثالثة ثانوي (أ)",
                startTime = "12:00 م",
                endTime = "01:00 م",
                isUpcoming = true,
                studentsCount = 30
            )
        )
    }

    fun getStudentRecentGrades(): List<GradeItem> {
        return listOf(
            GradeItem(
                id = "grade-1",
                subject = "الفيزياء",
                score = 90,
                maxScore = 100,
                examName = "امتحان الفيزياء الشهري",
                date = "2025/05/10",
                teacherName = "أ. محمود عبد العال"
            ),
            GradeItem(
                id = "grade-2",
                subject = "الرياضيات",
                score = 85,
                maxScore = 100,
                examName = "اختبار التفاضل والتكامل",
                date = "2025/05/08",
                teacherName = "أ. محمد عبد الرحمن"
            ),
            GradeItem(
                id = "grade-3",
                subject = "الكيمياء",
                score = 92,
                maxScore = 100,
                examName = "امتحان الكيمياء العضوية",
                date = "2025/05/05",
                teacherName = "أ. حسام الدين"
            ),
            GradeItem(
                id = "grade-4",
                subject = "اللغة العربية",
                score = 78,
                maxScore = 100,
                examName = "اختبار النحو والبلاغة",
                date = "2025/05/01",
                teacherName = "أ. علاء فاروق"
            )
        )
    }

    fun getTeacherTodayLessons(): List<LessonItem> {
        return listOf(
            LessonItem(
                id = "t-lesson-1",
                title = "مجموعة أولى ثانوي",
                subject = "مادة الفيزياء",
                groupName = "أولى ثانوي (ب)",
                startTime = "08:00 ص",
                endTime = "09:00 ص",
                isUpcoming = true,
                studentsCount = 32
            ),
            LessonItem(
                id = "t-lesson-2",
                title = "مجموعة ثانية ثانوي",
                subject = "مادة الرياضيات",
                groupName = "ثانية ثانوي (أ)",
                startTime = "10:00 ص",
                endTime = "11:00 ص",
                isUpcoming = true,
                studentsCount = 28
            ),
            LessonItem(
                id = "t-lesson-3",
                title = "مجموعة ثالثة ثانوي",
                subject = "مادة الكيمياء",
                groupName = "ثالثة ثانوي (ج)",
                startTime = "12:00 م",
                endTime = "01:00 م",
                isUpcoming = true,
                studentsCount = 30
            )
        )
    }

    fun getTeacherGroups(): List<GroupItem> {
        return listOf(
            GroupItem(
                id = "g-1",
                name = "مجموعة ثالثة ثانوي (أ)",
                subject = "فيزياء",
                code = "EDU-7K92",
                teacherName = "أحمد محمد",
                studentsCount = 30,
                description = "مجموعة الشعبة العلمية - فرع الدقي"
            ),
            GroupItem(
                id = "g-2",
                name = "مجموعة ثانية ثانوي (ب)",
                subject = "رياضيات",
                code = "MATH-2025",
                teacherName = "أحمد محمد",
                studentsCount = 28,
                description = "مجموعة المتفوقين - السنتر الرئيسي"
            )
        )
    }

    suspend fun syncWithFirebase(context: Context): Result<Int> {
        return if (dao != null) {
            com.example.data.firebase.EducoreFirebaseManager.pushLocalDataToCloud(context, dao)
        } else {
            Result.failure(Exception("قاعدة البيانات المحلية غير متاحة"))
        }
    }

    suspend fun pullFromFirebase(context: Context): Result<Int> {
        return if (dao != null) {
            com.example.data.firebase.EducoreFirebaseManager.pullCloudDataToLocal(context, dao)
        } else {
            Result.failure(Exception("قاعدة البيانات المحلية غير متاحة"))
        }
    }

    fun getDao(): EducoreDao? = dao
}


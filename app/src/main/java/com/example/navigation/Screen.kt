package com.example.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Welcome : Screen("welcome")
    
    // Auth Routes
    data object StudentLogin : Screen("student_login")
    data object StudentRegister : Screen("student_register")
    data object TeacherLogin : Screen("teacher_login")
    data object TeacherRegister : Screen("teacher_register")
    data object ForgotPassword : Screen("forgot_password")
    data object EmailVerification : Screen("email_verification/{email}") {
        fun createRoute(email: String) = "email_verification/$email"
    }

    // Role Dashboards
    data object StudentDashboard : Screen("student_dashboard")
    data object TeacherDashboard : Screen("teacher_dashboard")
    data object SupervisorDashboard : Screen("supervisor_dashboard")

    // Sub-screens
    data object StudentResults : Screen("student_results")
    data object StudentAttendance : Screen("student_attendance")
    data object StudentLessons : Screen("student_lessons")
    data object StudentGroups : Screen("student_groups")
    data object StudentExams : Screen("student_exams")
    data object StudentWarnings : Screen("student_warnings")
    data object StudentProfile : Screen("student_profile")
    
    data object TeacherGroups : Screen("teacher_groups")
    data object TeacherStudents : Screen("teacher_students")
    data object TeacherExams : Screen("teacher_exams")
    data object TeacherAttendance : Screen("teacher_attendance")
    data object TeacherGrades : Screen("teacher_grades")
    data object TeacherWarnings : Screen("teacher_warnings")
    data object TeacherReports : Screen("teacher_reports")
    data object TeacherPrintQr : Screen("teacher_print_qr")
    data object TeacherQrGenerate : Screen("teacher_qr_generate")
    data object TeacherProfile : Screen("teacher_profile")
    data object SecurityCenter : Screen("security_center")
}

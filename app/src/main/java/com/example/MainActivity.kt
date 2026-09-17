package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalizedStrings
import com.example.core.preferences.AppPreferences
import com.example.data.AuthRepository
import com.example.data.EducoreDataRepository
import com.example.model.UserRole
import com.example.navigation.Screen
import com.example.ui.screens.AttendanceManagementScreen
import com.example.ui.screens.EmailVerificationScreen
import com.example.ui.screens.ExamsManagementScreen
import com.example.ui.screens.ForgotPasswordScreen
import com.example.ui.screens.GroupsManagementScreen
import com.example.ui.screens.QrPrintScreen
import com.example.ui.screens.ReportsAnalyticsScreen
import com.example.ui.screens.SecurityCenterScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StudentAttendanceScreen
import com.example.ui.screens.StudentDashboardScreen
import com.example.ui.screens.StudentGradesScreen
import com.example.ui.screens.StudentLoginScreen
import com.example.ui.screens.StudentRegisterScreen
import com.example.ui.screens.StudentScheduleScreen
import com.example.ui.screens.StudentsManagementScreen
import com.example.ui.screens.SupervisorDashboardScreen
import com.example.ui.screens.TeacherDashboardScreen
import com.example.ui.screens.TeacherLoginScreen
import com.example.ui.screens.TeacherRegisterScreen
import com.example.ui.screens.WarningsManagementScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.EducoreTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val scope = rememberCoroutineScope()
            val appPreferences = remember { AppPreferences(applicationContext, scope) }
            val authRepository = remember { AuthRepository() }
            val dataRepository = remember { EducoreDataRepository(applicationContext) }

            val language by appPreferences.language.collectAsState()
            val themeMode by appPreferences.themeMode.collectAsState()
            val currentSession by authRepository.currentSession.collectAsState()

            val layoutDirection = if (language == AppLanguage.ARABIC) {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            }

            EducoreTheme(themeMode = themeMode) {
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        EducoreAppNavigation(
                            appPreferences = appPreferences,
                            authRepository = authRepository,
                            dataRepository = dataRepository,
                            language = language,
                            themeMode = themeMode,
                            currentSession = currentSession
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EducoreAppNavigation(
    appPreferences: AppPreferences,
    authRepository: AuthRepository,
    dataRepository: EducoreDataRepository,
    language: AppLanguage,
    themeMode: com.example.core.preferences.ThemeMode,
    currentSession: com.example.model.UserSession?
) {
    val navController = rememberNavController()
    val strings = LocalizedStrings.get(language)

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                language = language,
                currentSession = currentSession,
                onNavigateToWelcome = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToStudentDashboard = {
                    navController.navigate(Screen.StudentDashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToTeacherDashboard = {
                    navController.navigate(Screen.TeacherDashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToSupervisorDashboard = {
                    navController.navigate(Screen.SupervisorDashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Welcome.route) {
            WelcomeScreen(
                language = language,
                themeMode = themeMode,
                onToggleLanguage = { appPreferences.toggleLanguage() },
                onToggleTheme = { appPreferences.toggleTheme() },
                onNavigateToStudentLogin = { navController.navigate(Screen.StudentLogin.route) },
                onNavigateToStudentRegister = { navController.navigate(Screen.StudentRegister.route) },
                onNavigateToTeacherLogin = { navController.navigate(Screen.TeacherLogin.route) },
                onNavigateToTeacherRegister = { navController.navigate(Screen.TeacherRegister.route) }
            )
        }

        composable(Screen.StudentLogin.route) {
            StudentLoginScreen(
                language = language,
                themeMode = themeMode,
                authRepository = authRepository,
                onToggleLanguage = { appPreferences.toggleLanguage() },
                onToggleTheme = { appPreferences.toggleTheme() },
                onBack = { navController.popBackStack() },
                onNavigateToRegister = { navController.navigate(Screen.StudentRegister.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onLoginSuccess = { session ->
                    navController.navigate(Screen.StudentDashboard.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNeedsEmailVerification = { email ->
                    navController.navigate(Screen.EmailVerification.createRoute(email))
                }
            )
        }

        composable(Screen.StudentRegister.route) {
            StudentRegisterScreen(
                language = language,
                themeMode = themeMode,
                authRepository = authRepository,
                onToggleLanguage = { appPreferences.toggleLanguage() },
                onToggleTheme = { appPreferences.toggleTheme() },
                onBack = { navController.popBackStack() },
                onNavigateToLogin = { navController.navigate(Screen.StudentLogin.route) },
                onRegisterSuccess = { session ->
                    navController.navigate(Screen.StudentDashboard.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.TeacherLogin.route) {
            TeacherLoginScreen(
                language = language,
                themeMode = themeMode,
                authRepository = authRepository,
                onToggleLanguage = { appPreferences.toggleLanguage() },
                onToggleTheme = { appPreferences.toggleTheme() },
                onBack = { navController.popBackStack() },
                onNavigateToRegister = { navController.navigate(Screen.TeacherRegister.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onLoginSuccess = { session ->
                    val target = if (session.role == UserRole.SUPERVISOR) {
                        Screen.SupervisorDashboard.route
                    } else {
                        Screen.TeacherDashboard.route
                    }
                    navController.navigate(target) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.TeacherRegister.route) {
            TeacherRegisterScreen(
                language = language,
                themeMode = themeMode,
                authRepository = authRepository,
                onToggleLanguage = { appPreferences.toggleLanguage() },
                onToggleTheme = { appPreferences.toggleTheme() },
                onBack = { navController.popBackStack() },
                onNavigateToLogin = { navController.navigate(Screen.TeacherLogin.route) },
                onRegisterSuccess = { session ->
                    val target = if (session.role == UserRole.SUPERVISOR) {
                        Screen.SupervisorDashboard.route
                    } else {
                        Screen.TeacherDashboard.route
                    }
                    navController.navigate(target) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                language = language,
                themeMode = themeMode,
                authRepository = authRepository,
                onToggleLanguage = { appPreferences.toggleLanguage() },
                onToggleTheme = { appPreferences.toggleTheme() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.EmailVerification.route) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            EmailVerificationScreen(
                email = email,
                language = language,
                themeMode = themeMode,
                authRepository = authRepository,
                onToggleLanguage = { appPreferences.toggleLanguage() },
                onToggleTheme = { appPreferences.toggleTheme() },
                onVerified = {
                    navController.navigate(Screen.StudentDashboard.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.StudentDashboard.route) {
            val session = currentSession ?: com.example.model.UserSession(
                id = "student-001",
                fullName = "أحمد محمد",
                email = "ahmed.student@educore.edu",
                phoneNumber = "01012345678",
                role = UserRole.STUDENT,
                groupName = "طالب في مجموعة ثالثة ثانوي (أ)"
            )
            StudentDashboardScreen(
                userSession = session,
                language = language,
                themeMode = themeMode,
                dataRepository = dataRepository,
                onToggleLanguage = { appPreferences.toggleLanguage() },
                onToggleTheme = { appPreferences.toggleTheme() },
                onLogout = {
                    authRepository.logout()
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToResults = { navController.navigate(Screen.StudentResults.route) },
                onNavigateToAttendance = { navController.navigate(Screen.StudentAttendance.route) },
                onNavigateToLessons = { navController.navigate(Screen.StudentLessons.route) },
                onNavigateToWarnings = { navController.navigate(Screen.StudentWarnings.route) }
            )
        }

        composable(Screen.TeacherDashboard.route) {
            val session = currentSession ?: com.example.model.UserSession(
                id = "teacher-001",
                fullName = "أحمد محمد",
                email = "ahmed.teacher@educore.edu",
                phoneNumber = "01234567890",
                role = UserRole.TEACHER,
                customCode = "mahmoud123"
            )
            TeacherDashboardScreen(
                userSession = session,
                language = language,
                themeMode = themeMode,
                dataRepository = dataRepository,
                onToggleLanguage = { appPreferences.toggleLanguage() },
                onToggleTheme = { appPreferences.toggleTheme() },
                onLogout = {
                    authRepository.logout()
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToGroups = { navController.navigate(Screen.TeacherGroups.route) },
                onNavigateToStudents = { navController.navigate(Screen.TeacherStudents.route) },
                onNavigateToExams = { navController.navigate(Screen.TeacherExams.route) },
                onNavigateToAttendance = { navController.navigate(Screen.TeacherAttendance.route) },
                onNavigateToWarnings = { navController.navigate(Screen.TeacherWarnings.route) },
                onNavigateToReports = { navController.navigate(Screen.TeacherReports.route) },
                onNavigateToPrintQr = { navController.navigate(Screen.TeacherPrintQr.route) },
                onNavigateToSecurity = { navController.navigate(Screen.SecurityCenter.route) }
            )
        }

        composable(Screen.SupervisorDashboard.route) {
            val session = currentSession ?: com.example.model.UserSession(
                id = "supervisor-001",
                fullName = "د. مصطفى كامل",
                email = "mostafa.super@educore.edu",
                phoneNumber = "01511223344",
                role = UserRole.SUPERVISOR,
                customCode = "SUPER-ADMIN"
            )
            SupervisorDashboardScreen(
                userSession = session,
                language = language,
                themeMode = themeMode,
                dataRepository = dataRepository,
                onToggleLanguage = { appPreferences.toggleLanguage() },
                onToggleTheme = { appPreferences.toggleTheme() },
                onLogout = {
                    authRepository.logout()
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToGroups = { navController.navigate(Screen.TeacherGroups.route) },
                onNavigateToStudents = { navController.navigate(Screen.TeacherStudents.route) },
                onNavigateToExams = { navController.navigate(Screen.TeacherExams.route) },
                onNavigateToAttendance = { navController.navigate(Screen.TeacherAttendance.route) },
                onNavigateToWarnings = { navController.navigate(Screen.TeacherWarnings.route) },
                onNavigateToReports = { navController.navigate(Screen.TeacherReports.route) },
                onNavigateToPrintQr = { navController.navigate(Screen.TeacherPrintQr.route) },
                onNavigateToSecurity = { navController.navigate(Screen.SecurityCenter.route) }
            )
        }

        // Phase 2 Management Screens
        composable(Screen.TeacherGroups.route) {
            GroupsManagementScreen(
                strings = strings,
                dataRepository = dataRepository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TeacherStudents.route) {
            StudentsManagementScreen(
                strings = strings,
                dataRepository = dataRepository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TeacherExams.route) {
            ExamsManagementScreen(
                strings = strings,
                dataRepository = dataRepository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TeacherAttendance.route) {
            AttendanceManagementScreen(
                strings = strings,
                dataRepository = dataRepository,
                onBack = { navController.popBackStack() },
                onNavigateToPrintQr = { navController.navigate(Screen.TeacherPrintQr.route) }
            )
        }

        composable(Screen.TeacherWarnings.route) {
            WarningsManagementScreen(
                strings = strings,
                dataRepository = dataRepository,
                onBack = { navController.popBackStack() }
            )
        }

        // Phase 3 Analytics, Reports & Print QR Screens
        composable(Screen.TeacherReports.route) {
            ReportsAnalyticsScreen(
                strings = strings,
                dataRepository = dataRepository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TeacherPrintQr.route) {
            QrPrintScreen(
                strings = strings,
                dataRepository = dataRepository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.StudentWarnings.route) {
            WarningsManagementScreen(
                strings = strings,
                dataRepository = dataRepository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.StudentResults.route) {
            StudentGradesScreen(
                strings = strings,
                dataRepository = dataRepository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.StudentAttendance.route) {
            StudentAttendanceScreen(
                strings = strings,
                dataRepository = dataRepository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.StudentLessons.route) {
            StudentScheduleScreen(
                strings = strings,
                dataRepository = dataRepository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SecurityCenter.route) {
            SecurityCenterScreen(
                strings = strings,
                dataRepository = dataRepository,
                onBack = { navController.popBackStack() }
            )
        }
    }
}


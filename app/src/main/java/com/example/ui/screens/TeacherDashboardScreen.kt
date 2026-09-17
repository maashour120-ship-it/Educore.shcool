package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CloudDone
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalizedStrings
import com.example.core.preferences.ThemeMode
import com.example.data.EducoreDataRepository
import com.example.model.LessonItem
import com.example.model.UserSession
import com.example.ui.components.EducoreHeaderBar
import com.example.ui.theme.EducoreError
import com.example.ui.theme.EducorePrimary
import com.example.ui.theme.EducorePurple
import com.example.ui.theme.EducoreSuccess
import com.example.ui.theme.EducoreWarning
import kotlinx.coroutines.launch

@Composable
fun TeacherDashboardScreen(
    userSession: UserSession,
    language: AppLanguage,
    themeMode: ThemeMode,
    dataRepository: EducoreDataRepository,
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToGroups: () -> Unit = {},
    onNavigateToStudents: () -> Unit = {},
    onNavigateToExams: () -> Unit = {},
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToWarnings: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToPrintQr: () -> Unit = {},
    onNavigateToSecurity: () -> Unit = {}
) {
    val strings = LocalizedStrings.get(language)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedLessonForQr by remember { mutableStateOf<LessonItem?>(null) }

    val todayLessons = remember { dataRepository.getTeacherTodayLessons() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = userSession.fullName.take(2),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Column {
                            Text(
                                text = userSession.fullName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${strings.teacherCode}: ${userSession.customCode ?: "mahmoud123"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline))
                    Spacer(modifier = Modifier.height(16.dp))

                    TeacherDrawerRow(icon = Icons.Default.Class, title = strings.groups) {
                        scope.launch { drawerState.close() }
                        onNavigateToGroups()
                    }
                    TeacherDrawerRow(icon = Icons.Default.People, title = strings.students) {
                        scope.launch { drawerState.close() }
                        onNavigateToStudents()
                    }
                    TeacherDrawerRow(icon = Icons.Default.Assignment, title = strings.exams) {
                        scope.launch { drawerState.close() }
                        onNavigateToExams()
                    }
                    TeacherDrawerRow(icon = Icons.Default.CheckCircle, title = strings.attendance) {
                        scope.launch { drawerState.close() }
                        onNavigateToAttendance()
                    }
                    TeacherDrawerRow(icon = Icons.Default.Warning, title = strings.myWarnings) {
                        scope.launch { drawerState.close() }
                        onNavigateToWarnings()
                    }
                    TeacherDrawerRow(icon = Icons.Default.Assessment, title = strings.analyticsAndReports) {
                        scope.launch { drawerState.close() }
                        onNavigateToReports()
                    }
                    TeacherDrawerRow(icon = Icons.Default.Print, title = strings.printQrPoster) {
                        scope.launch { drawerState.close() }
                        onNavigateToPrintQr()
                    }
                    TeacherDrawerRow(icon = Icons.Default.Lock, title = "مركز الحماية والأمان") {
                        scope.launch { drawerState.close() }
                        onNavigateToSecurity()
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth().testTag("teacher_logout_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EducoreError.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = strings.logout,
                            color = EducoreError,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Top Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .testTag("teacher_drawer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    EducoreHeaderBar(
                        language = language,
                        themeMode = themeMode,
                        onToggleLanguage = onToggleLanguage,
                        onToggleTheme = onToggleTheme,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Welcome Greeting Header
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${strings.teacherWelcome} ${userSession.fullName}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = strings.teacherActivitySummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = userSession.customCode ?: "mahmoud123",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // 4 Stats Metric Cards
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TeacherStatCard(
                            title = strings.totalStudents,
                            value = "120",
                            subtitle = "+12% هذا الشهر",
                            indicatorColor = EducoreSuccess,
                            modifier = Modifier.weight(1f).clickable { onNavigateToStudents() }
                        )
                        TeacherStatCard(
                            title = strings.scheduledLessonsToday,
                            value = "4",
                            subtitle = "2 متبقية",
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TeacherStatCard(
                            title = strings.averageAttendance,
                            value = "94%",
                            subtitle = "+3% هذا الأسبوع",
                            indicatorColor = EducoreSuccess,
                            modifier = Modifier.weight(1f).clickable { onNavigateToAttendance() }
                        )
                        TeacherStatCard(
                            title = strings.upcomingExams,
                            value = "2",
                            subtitle = "خلال هذا الأسبوع",
                            indicatorColor = EducoreWarning,
                            modifier = Modifier.weight(1f).clickable { onNavigateToExams() }
                        )
                    }
                }
            }

            // "الخيارات الرئيسية" Section
            item {
                Text(
                    text = strings.mainOptions,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp)
                )
            }

            // 6 Options Grid Cards
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TeacherOptionCard(
                            title = strings.groups,
                            description = strings.groupsDesc,
                            icon = Icons.Default.Group,
                            iconTint = Color(0xFF6366F1),
                            iconBg = Color(0xFFEEF2FF),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToGroups
                        )
                        TeacherOptionCard(
                            title = strings.students,
                            description = strings.studentsDesc,
                            icon = Icons.Default.People,
                            iconTint = EducoreSuccess,
                            iconBg = Color(0xFFECFDF5),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToStudents
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TeacherOptionCard(
                            title = strings.exams,
                            description = strings.examsDesc,
                            icon = Icons.Default.Assignment,
                            iconTint = EducorePurple,
                            iconBg = Color(0xFFF5F3FF),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToExams
                        )
                        TeacherOptionCard(
                            title = strings.attendance,
                            description = strings.attendanceDesc,
                            icon = Icons.Default.CheckCircle,
                            iconTint = Color(0xFF0284C7),
                            iconBg = Color(0xFFE0F2FE),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToAttendance
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TeacherOptionCard(
                            title = strings.myWarnings,
                            description = "متابعة وإصدار الإنذارات التأديبية",
                            icon = Icons.Default.Warning,
                            iconTint = Color(0xFFEF4444),
                            iconBg = Color(0xFFFEE2E2),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToWarnings
                        )
                        TeacherOptionCard(
                            title = strings.generateQr,
                            description = strings.generateQrDesc,
                            icon = Icons.Default.QrCode,
                            iconTint = EducorePrimary,
                            iconBg = Color(0xFFEFF6FF),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedLessonForQr = todayLessons.firstOrNull()
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TeacherOptionCard(
                            title = strings.analyticsAndReports,
                            description = "مؤشرات الأداء وكشوف الدرجات",
                            icon = Icons.Default.Assessment,
                            iconTint = Color(0xFFD97706),
                            iconBg = Color(0xFFFEF3C7),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToReports
                        )
                        TeacherOptionCard(
                            title = strings.printQrCode,
                            description = "طباعة ملصق الحضور للقاعة",
                            icon = Icons.Default.Print,
                            iconTint = Color(0xFF059669),
                            iconBg = Color(0xFFD1FAE5),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToPrintQr
                        )
                    }

                    TeacherOptionCard(
                        title = "مركز الحماية والأمان وتأمين البيانات",
                        description = "سجل الأنشطة ودروع الحماية المشفرة والتخزين الآمن",
                        icon = Icons.Default.Lock,
                        iconTint = Color(0xFF2563EB),
                        iconBg = Color(0xFFEFF6FF),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onNavigateToSecurity
                    )
                }
            }

            // "حصص اليوم" Section Header
            item {
                Text(
                    text = strings.scheduledLessonsToday,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 10.dp)
                )
            }

            // Today Lessons with QR generator button
            items(todayLessons) { lesson ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = lesson.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${lesson.subject} (${lesson.studentsCount} ${strings.studentsCountUnit})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${lesson.startTime} - ${lesson.endTime}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Surface(
                            onClick = { selectedLessonForQr = lesson },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("generate_lesson_qr_${lesson.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "توليد QR",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // QR Code Live Presenter Dialog
    if (selectedLessonForQr != null) {
        val lesson = selectedLessonForQr!!
        AlertDialog(
            onDismissRequest = { selectedLessonForQr = null },
            title = {
                Text(
                    text = "رمز QR الحضور المباشر",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${lesson.subject} | ${lesson.startTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // QR Code visual box
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Session QR",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(170.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "رمز الحصة: EDU-${lesson.id.uppercase()}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "صالح لمدة 15 دقيقة فقط",
                        style = MaterialTheme.typography.bodySmall,
                        color = EducoreWarning
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedLessonForQr = null },
                    colors = ButtonDefaults.buttonColors(containerColor = EducorePrimary)
                ) {
                    Text("إغلاق")
                }
            }
        )
    }
}

@Composable
fun SupervisorDashboardScreen(
    userSession: UserSession,
    language: AppLanguage,
    themeMode: ThemeMode,
    dataRepository: EducoreDataRepository,
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToGroups: () -> Unit = {},
    onNavigateToStudents: () -> Unit = {},
    onNavigateToExams: () -> Unit = {},
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToWarnings: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToPrintQr: () -> Unit = {},
    onNavigateToSecurity: () -> Unit = {}
) {
    TeacherDashboardScreen(
        userSession = userSession,
        language = language,
        themeMode = themeMode,
        dataRepository = dataRepository,
        onToggleLanguage = onToggleLanguage,
        onToggleTheme = onToggleTheme,
        onLogout = onLogout,
        onNavigateToGroups = onNavigateToGroups,
        onNavigateToStudents = onNavigateToStudents,
        onNavigateToExams = onNavigateToExams,
        onNavigateToAttendance = onNavigateToAttendance,
        onNavigateToWarnings = onNavigateToWarnings,
        onNavigateToReports = onNavigateToReports,
        onNavigateToPrintQr = onNavigateToPrintQr,
        onNavigateToSecurity = onNavigateToSecurity
    )
}


@Composable
private fun TeacherStatCard(
    title: String,
    value: String,
    subtitle: String,
    indicatorColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TeacherOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.clickable { onClick?.invoke() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                minLines = 2,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun TeacherDrawerRow(
    icon: ImageVector,
    title: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

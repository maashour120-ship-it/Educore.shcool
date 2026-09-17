package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppStrings
import com.example.data.EducoreDataRepository
import com.example.data.local.ExamEntity
import com.example.data.local.GroupEntity
import com.example.data.local.StudentEntity
import com.example.ui.theme.EducoreError
import com.example.ui.theme.EducorePrimary
import com.example.ui.theme.EducorePurple
import com.example.ui.theme.EducoreSuccess
import com.example.ui.theme.EducoreWarning
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsAnalyticsScreen(
    strings: AppStrings,
    dataRepository: EducoreDataRepository,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf(
        strings.analyticsAndReports,
        strings.gradeSheet,
        strings.studentReportCard
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.analyticsAndReports,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("reports_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.cancel
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier.testTag("reports_tab_$index")
                    )
                }
            }

            when (selectedTab) {
                0 -> AnalyticsDashboardTab(strings = strings, dataRepository = dataRepository)
                1 -> GradeSheetExportTab(strings = strings, dataRepository = dataRepository)
                2 -> StudentReportCardTab(strings = strings, dataRepository = dataRepository)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Tab 1: Analytics & Performance Insights
// ─────────────────────────────────────────────────────────────
@Composable
fun AnalyticsDashboardTab(
    strings: AppStrings,
    dataRepository: EducoreDataRepository
) {
    val groupsFlow = dataRepository.getAllGroupsFlow()
    val studentsFlow = dataRepository.getAllStudentsFlow()
    val examsFlow = dataRepository.getAllExamsFlow()

    val groups by (groupsFlow?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })
    val students by (studentsFlow?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })
    val exams by (examsFlow?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })

    val avgAttendance = if (students.isNotEmpty()) students.map { it.attendanceRate }.average().toInt() else 92
    val avgScore = if (students.isNotEmpty()) students.map { it.averageGrade }.average().toInt() else 88
    val topStudents = remember(students) { students.sortedByDescending { it.averageGrade }.take(3) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High-level KPI summary cards
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                KpiMetricCard(
                    title = strings.averageScore,
                    value = "$avgScore%",
                    subtitle = "معدل تفوق عام مرتفع",
                    color = EducorePrimary,
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
                KpiMetricCard(
                    title = strings.attendanceRate,
                    value = "$avgAttendance%",
                    subtitle = "نسبة حضور ممتازة",
                    color = EducoreSuccess,
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                KpiMetricCard(
                    title = "إجمالي المجموعات",
                    value = "${groups.size.coerceAtLeast(3)}",
                    subtitle = "مجموعات نشطة",
                    color = EducorePurple,
                    icon = Icons.Default.School,
                    modifier = Modifier.weight(1f)
                )
                KpiMetricCard(
                    title = "الامتحانات المنجزة",
                    value = "${exams.size.coerceAtLeast(2)}",
                    subtitle = "تم تصحيحها",
                    color = Color(0xFFD97706),
                    icon = Icons.Default.Assessment,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Grade Distribution Chart Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = strings.performanceTrend,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "توزيع الدرجات",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Animated Grade Bar Chart
                    GradeDistributionBarChart()
                }
            }
        }

        // Attendance & Engagement Ring Chart Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "مؤشر الحضور والغياب العام",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AttendanceDonutChart(presentPercent = avgAttendance)

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ChartLegendItem(color = EducoreSuccess, label = "حاضر ($avgAttendance%)")
                            ChartLegendItem(color = EducoreWarning, label = "متأخر (${(100 - avgAttendance) / 2}%)")
                            ChartLegendItem(color = EducoreError, label = "غائب (${(100 - avgAttendance) / 2}%)")
                        }
                    }
                }
            }
        }

        // Top Performing Students Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B)
                )
                Text(
                    text = strings.topStudents,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Top Students List Items
        items(topStudents) { student ->
            TopStudentRankingCard(student = student)
        }
    }
}

@Composable
fun KpiMetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GradeDistributionBarChart() {
    val brackets = listOf(
        Pair("ممتاز (90-100)", 65f),
        Pair("جيد جداً (80-89)", 22f),
        Pair("جيد (65-79)", 10f),
        Pair("يحتاج دعم (<65)", 3f)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        brackets.forEach { (label, percent) ->
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "${percent.toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EducorePrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percent / 100f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                when {
                                    percent >= 50 -> EducoreSuccess
                                    percent >= 20 -> EducorePrimary
                                    percent >= 10 -> EducoreWarning
                                    else -> EducoreError
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceDonutChart(presentPercent: Int) {
    Box(
        modifier = Modifier.size(110.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(100.dp)) {
            val strokeWidth = 14.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val centerOffset = Offset(size.width / 2, size.height / 2)

            // Background Circle
            drawCircle(
                color = Color(0xFFEF4444).copy(alpha = 0.2f),
                radius = radius,
                center = centerOffset,
                style = Stroke(width = strokeWidth)
            )

            // Active Arc
            val sweep = 360f * (presentPercent / 100f)
            drawArc(
                color = Color(0xFF10B981),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                style = Stroke(width = strokeWidth)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$presentPercent%",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "حضور",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ChartLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun TopStudentRankingCard(student: StudentEntity) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFFEF3C7),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.fullName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${student.groupName} • هاتف: ${student.phone}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = EducoreSuccess.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "${student.averageGrade}%",
                    color = EducoreSuccess,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Tab 2: Grade Sheet Export & Print
// ─────────────────────────────────────────────────────────────
@Composable
fun GradeSheetExportTab(
    strings: AppStrings,
    dataRepository: EducoreDataRepository
) {
    val context = LocalContext.current
    val groupsFlow = dataRepository.getAllGroupsFlow()
    val examsFlow = dataRepository.getAllExamsFlow()
    val studentsFlow = dataRepository.getAllStudentsFlow()

    val groups by (groupsFlow?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })
    val exams by (examsFlow?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })
    val students by (studentsFlow?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })

    var selectedGroup by remember { mutableStateOf<GroupEntity?>(null) }
    var selectedExam by remember { mutableStateOf<ExamEntity?>(null) }

    if (selectedGroup == null && groups.isNotEmpty()) selectedGroup = groups.first()
    if (selectedExam == null && exams.isNotEmpty()) selectedExam = exams.first()

    val filteredStudents = remember(students, selectedGroup) {
        if (selectedGroup != null) students.filter { it.groupId == selectedGroup?.id }
        else students
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Group Filter Selector
        item {
            Text(text = "اختر المجموعة:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(groups) { group ->
                    val isSelected = selectedGroup?.id == group.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedGroup = group },
                        label = { Text(group.name, fontSize = 12.sp) }
                    )
                }
            }
        }

        // Exam Filter Selector
        item {
            Text(text = "اختر الاختبار:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(exams) { exam ->
                    val isSelected = selectedExam?.id == exam.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedExam = exam },
                        label = { Text(exam.title, fontSize = 12.sp) }
                    )
                }
            }
        }

        // Action Toolbar (Print / Share)
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        triggerGradeSheetPrint(
                            context = context,
                            groupName = selectedGroup?.name ?: "المجموعة",
                            examTitle = selectedExam?.title ?: "الاختبار",
                            students = filteredStudents,
                            maxScore = selectedExam?.maxScore ?: 100
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EducorePrimary),
                    modifier = Modifier.weight(1f).testTag("print_grade_sheet_button")
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.print, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        shareGradeSheet(
                            context = context,
                            groupName = selectedGroup?.name ?: "المجموعة",
                            examTitle = selectedExam?.title ?: "الاختبار",
                            students = filteredStudents
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("share_grade_sheet_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.shareReport, fontSize = 13.sp)
                }
            }
        }

        // Grade Sheet Printable Table Header
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "كشف درجات: ${selectedExam?.title ?: "الاختبار"}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${selectedGroup?.name ?: "المجموعة"} • إجمالي الطلاب: ${filteredStudents.size}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EducorePrimary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "الدرجة العظمى: ${selectedExam?.maxScore ?: 100}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EducorePrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Table Columns
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "#", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(30.dp))
                            Text(text = strings.fullName, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1.5f))
                            Text(text = "الدرجة", fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                            Text(text = "التقدير", fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Table Rows for Students
        items(filteredStudents) { student ->
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredStudents.indexOf(student) + 1}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(30.dp)
                    )
                    Text(
                        text = student.fullName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1.5f)
                    )
                    Text(
                        text = "${student.averageGrade} / 100",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = EducorePrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = when {
                            student.averageGrade >= 90 -> "ممتاز"
                            student.averageGrade >= 80 -> "جيد جداً"
                            student.averageGrade >= 65 -> "جيد"
                            else -> "يحتاج متابعة"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (student.averageGrade >= 80) EducoreSuccess else EducoreWarning,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Tab 3: Individual Student Report Card
// ─────────────────────────────────────────────────────────────
@Composable
fun StudentReportCardTab(
    strings: AppStrings,
    dataRepository: EducoreDataRepository
) {
    val context = LocalContext.current
    val studentsFlow = dataRepository.getAllStudentsFlow()
    val students by (studentsFlow?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })
    var selectedStudent by remember { mutableStateOf<StudentEntity?>(null) }

    if (selectedStudent == null && students.isNotEmpty()) {
        selectedStudent = students.first()
    }

    val student = selectedStudent

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Student Picker
        item {
            Text(text = "اختر الطالب لعرض الشهادة والتقرير:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(students) { s ->
                    val isSelected = selectedStudent?.id == s.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStudent = s },
                        label = { Text(s.fullName, fontSize = 12.sp) }
                    )
                }
            }
        }

        if (student != null) {
            // Action Buttons
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            triggerStudentReportPrint(
                                context = context,
                                student = student
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EducorePurple),
                        modifier = Modifier.weight(1f).testTag("print_student_report_button")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.print, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            shareStudentReport(context = context, student = student)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("share_student_report_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.shareReport, fontSize = 13.sp)
                    }
                }
            }

            // High-Polished Certificate Preview Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(2.dp, EducorePurple.copy(alpha = 0.3f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth().testTag("student_certificate_preview")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        // Logo & Platform Name
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Educore Platform",
                                fontWeight = FontWeight.Bold,
                                color = EducorePurple,
                                fontSize = 16.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFAF5FF)
                            ) {
                                Text(
                                    text = "تقرير أداء الطالب الرسمي",
                                    color = EducorePurple,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            shape = CircleShape,
                            color = EducorePurple.copy(alpha = 0.1f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = EducorePurple,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = student.fullName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "${student.groupName} • هاتف ولي الأمر: ${student.parentPhone}",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Stats Grid
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ReportMetricBox(
                                title = "المعدل العام",
                                value = "${student.averageGrade}%",
                                color = EducoreSuccess,
                                modifier = Modifier.weight(1f)
                            )
                            ReportMetricBox(
                                title = "نسبة الحضور",
                                value = "${student.attendanceRate}%",
                                color = EducorePrimary,
                                modifier = Modifier.weight(1f)
                            )
                            ReportMetricBox(
                                title = "الإنذارات",
                                value = "${student.warningsCount}",
                                color = if (student.warningsCount > 0) EducoreError else EducoreSuccess,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Teacher Remarks
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "ملاحظات وتوصية المعلم:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF374151)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (student.averageGrade >= 85)
                                        "طالب متميز وملتزم بالحضور والتفاعل الصفي. نتمنى له دوام التفوق والنجاح."
                                    else
                                        "يحتاج إلى مراجعة دورية لدروس الفيزياء وحل التمارين الإضافية بانتظام.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF6B7280),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportMetricBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
        ) {
            Text(text = title, fontSize = 11.sp, color = Color(0xFF4B5563))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Print & Share Utilities
// ─────────────────────────────────────────────────────────────
fun shareGradeSheet(context: Context, groupName: String, examTitle: String, students: List<StudentEntity>) {
    try {
        val sb = StringBuilder()
        sb.appendLine("📊 *كشف درجات منصة Educore*")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("📌 المجموعة: $groupName")
        sb.appendLine("📝 الاختبار: $examTitle")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        students.forEachIndexed { i, s ->
            sb.appendLine("${i + 1}. ${s.fullName} ➔ ${s.averageGrade}%")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "كشف درجات $groupName - $examTitle")
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة كشف الدرجات عبر:"))
    } catch (e: Exception) {
        Toast.makeText(context, "تعذر المشاركة: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

fun shareStudentReport(context: Context, student: StudentEntity) {
    try {
        val text = """
            🎓 *تقرير أداء الطالب - منصة Educore*
            ━━━━━━━━━━━━━━━━━━━━
            👤 الطالب: ${student.fullName}
            🏫 المجموعة: ${student.groupName}
            📈 المعدل العام: ${student.averageGrade}%
            📅 نسبة الحضور: ${student.attendanceRate}%
            ⚠️ الإنذارات: ${student.warningsCount}
            ━━━━━━━━━━━━━━━━━━━━
            تم الإصدار رسمياً عبر منصة Educore التعليمية.
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "تقرير أداء الطالب: ${student.fullName}")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة تقرير الطالب عبر:"))
    } catch (e: Exception) {
        Toast.makeText(context, "تعذر المشاركة: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}


fun triggerGradeSheetPrint(
    context: Context,
    groupName: String,
    examTitle: String,
    students: List<StudentEntity>,
    maxScore: Int
) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
    val jobName = "Educore_GradeSheet_${groupName.replace(" ", "_")}"

    if (printManager != null) {
        try {
            printManager.print(
                jobName,
                object : PrintDocumentAdapter() {
                    override fun onLayout(
                        oldAttributes: PrintAttributes?,
                        newAttributes: PrintAttributes?,
                        cancellationSignal: CancellationSignal?,
                        callback: LayoutResultCallback?,
                        extras: Bundle?
                    ) {
                        val info = PrintDocumentInfo.Builder("$jobName.pdf")
                            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                            .setPageCount(1)
                            .build()
                        callback?.onLayoutFinished(info, true)
                    }

                    override fun onWrite(
                        pages: Array<out PageRange>?,
                        destination: ParcelFileDescriptor?,
                        cancellationSignal: CancellationSignal?,
                        callback: WriteResultCallback?
                    ) {
                        val pdfDocument = PdfDocument()
                        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                        val page = pdfDocument.startPage(pageInfo)
                        val canvas: Canvas = page.canvas
                        val paint = Paint().apply { isAntiAlias = true }

                        // Background
                        paint.color = android.graphics.Color.WHITE
                        canvas.drawRect(0f, 0f, 595f, 842f, paint)

                        // Title Banner
                        paint.color = android.graphics.Color.rgb(37, 99, 235)
                        canvas.drawRect(40f, 40f, 555f, 95f, paint)

                        paint.color = android.graphics.Color.WHITE
                        paint.textSize = 18f
                        paint.isFakeBoldText = true
                        canvas.drawText("Educore Platform - Official Grade Sheet", 60f, 75f, paint)

                        // Details
                        paint.color = android.graphics.Color.BLACK
                        paint.textSize = 13f
                        paint.isFakeBoldText = false
                        canvas.drawText("Group: $groupName  |  Exam: $examTitle  |  Max Score: $maxScore", 40f, 120f, paint)

                        // Table Headers
                        paint.color = android.graphics.Color.LTGRAY
                        canvas.drawRect(40f, 135f, 555f, 160f, paint)

                        paint.color = android.graphics.Color.BLACK
                        paint.isFakeBoldText = true
                        paint.textSize = 11f
                        canvas.drawText("#", 50f, 152f, paint)
                        canvas.drawText("Student Name", 80f, 152f, paint)
                        canvas.drawText("Score", 400f, 152f, paint)
                        canvas.drawText("Status", 480f, 152f, paint)

                        // Table Rows
                        var y = 180f
                        students.forEachIndexed { i, s ->
                            paint.isFakeBoldText = false
                            canvas.drawText("${i + 1}", 50f, y, paint)
                            canvas.drawText(s.fullName, 80f, y, paint)
                            canvas.drawText("${s.averageGrade} / $maxScore", 400f, y, paint)
                            canvas.drawText(if (s.averageGrade >= 60) "PASS" else "FAIL", 480f, y, paint)
                            y += 24f
                        }

                        pdfDocument.finishPage(page)

                        try {
                            destination?.let {
                                val out = FileOutputStream(it.fileDescriptor)
                                pdfDocument.writeTo(out)
                                callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                            }
                        } catch (e: Exception) {
                            callback?.onWriteFailed(e.message)
                        } finally {
                            pdfDocument.close()
                        }
                    }
                },
                PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4).build()
            )
        } catch (e: Exception) {
            Toast.makeText(context, "الطباعة: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}

fun triggerStudentReportPrint(
    context: Context,
    student: StudentEntity
) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
    val jobName = "Educore_Report_${student.fullName.replace(" ", "_")}"

    if (printManager != null) {
        try {
            printManager.print(
                jobName,
                object : PrintDocumentAdapter() {
                    override fun onLayout(
                        oldAttributes: PrintAttributes?,
                        newAttributes: PrintAttributes?,
                        cancellationSignal: CancellationSignal?,
                        callback: LayoutResultCallback?,
                        extras: Bundle?
                    ) {
                        val info = PrintDocumentInfo.Builder("$jobName.pdf")
                            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                            .setPageCount(1)
                            .build()
                        callback?.onLayoutFinished(info, true)
                    }

                    override fun onWrite(
                        pages: Array<out PageRange>?,
                        destination: ParcelFileDescriptor?,
                        cancellationSignal: CancellationSignal?,
                        callback: WriteResultCallback?
                    ) {
                        val pdfDocument = PdfDocument()
                        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                        val page = pdfDocument.startPage(pageInfo)
                        val canvas: Canvas = page.canvas
                        val paint = Paint().apply { isAntiAlias = true }

                        // Background
                        paint.color = android.graphics.Color.WHITE
                        canvas.drawRect(0f, 0f, 595f, 842f, paint)

                        // Certificate Frame
                        paint.color = android.graphics.Color.rgb(88, 28, 135)
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = 4f
                        canvas.drawRect(30f, 30f, 565f, 812f, paint)

                        paint.style = Paint.Style.FILL
                        paint.textSize = 24f
                        paint.isFakeBoldText = true
                        paint.textAlign = Paint.Align.CENTER
                        canvas.drawText("Educore Educational Platform", 595f / 2, 80f, paint)

                        paint.textSize = 16f
                        paint.color = android.graphics.Color.DKGRAY
                        canvas.drawText("Official Student Performance Certificate", 595f / 2, 110f, paint)

                        paint.color = android.graphics.Color.BLACK
                        paint.textSize = 20f
                        paint.isFakeBoldText = true
                        canvas.drawText(student.fullName, 595f / 2, 180f, paint)

                        paint.textSize = 13f
                        paint.isFakeBoldText = false
                        canvas.drawText("Group: ${student.groupName}  |  Parent Phone: ${student.parentPhone}", 595f / 2, 210f, paint)

                        // Metrics
                        paint.textSize = 15f
                        paint.isFakeBoldText = true
                        canvas.drawText("Academic Average: ${student.averageGrade}%", 595f / 2, 280f, paint)
                        canvas.drawText("Attendance Rate: ${student.attendanceRate}%", 595f / 2, 310f, paint)
                        canvas.drawText("Disciplinary Warnings: ${student.warningsCount}", 595f / 2, 340f, paint)

                        paint.textSize = 12f
                        paint.isFakeBoldText = false
                        paint.color = android.graphics.Color.GRAY
                        canvas.drawText("Issued by Educore System Management", 595f / 2, 750f, paint)

                        pdfDocument.finishPage(page)

                        try {
                            destination?.let {
                                val out = FileOutputStream(it.fileDescriptor)
                                pdfDocument.writeTo(out)
                                callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                            }
                        } catch (e: Exception) {
                            callback?.onWriteFailed(e.message)
                        } finally {
                            pdfDocument.close()
                        }
                    }
                },
                PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4).build()
            )
        } catch (e: Exception) {
            Toast.makeText(context, "الطباعة: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}

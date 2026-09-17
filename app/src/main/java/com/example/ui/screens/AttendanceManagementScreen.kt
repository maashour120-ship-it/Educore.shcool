package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppStrings
import com.example.data.EducoreDataRepository
import com.example.data.local.AttendanceEntity
import com.example.data.local.GroupEntity
import com.example.data.local.StudentEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceManagementScreen(
    strings: AppStrings,
    dataRepository: EducoreDataRepository,
    onBack: () -> Unit,
    onNavigateToPrintQr: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val groupsFlow = dataRepository.getAllGroupsFlow()
    val groups by (groupsFlow?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })

    var selectedGroup by remember { mutableStateOf<GroupEntity?>(null) }

    LaunchedEffect(groups) {
        if (selectedGroup == null && groups.isNotEmpty()) {
            selectedGroup = groups.first()
        }
    }

    val studentsFlow = selectedGroup?.let { dataRepository.getStudentsByGroupFlow(it.id) }
    val students by (studentsFlow?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })

    val attendanceState = remember { mutableStateMapOf<String, String>() }

    // Initialize all to PRESENT when students change
    LaunchedEffect(students) {
        students.forEach { student ->
            if (!attendanceState.containsKey(student.id)) {
                attendanceState[student.id] = "PRESENT"
            }
        }
    }

    var showLiveQrDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.attendance,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("attendance_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.cancel
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToPrintQr,
                        modifier = Modifier.testTag("attendance_top_print_qr_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = strings.printQrPoster,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    FilledTonalButton(
                        onClick = { showLiveQrDialog = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("generate_live_qr_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.generateQr, fontSize = 12.sp)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            students.forEach { student ->
                                attendanceState[student.id] = "PRESENT"
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mark_all_present_button")
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.markAllPresent, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val records = students.map { student ->
                                    AttendanceEntity(
                                        id = "att-${System.currentTimeMillis()}-${student.id}",
                                        lessonId = "lesson-live",
                                        lessonTitle = "حصة الفيزياء الأسبوعية",
                                        studentId = student.id,
                                        studentName = student.fullName,
                                        groupId = selectedGroup?.id ?: "group-1",
                                        date = "اليوم",
                                        status = attendanceState[student.id] ?: "PRESENT",
                                        checkInTime = "08:15 ص"
                                    )
                                }
                                dataRepository.addAttendanceRecords(records)
                                snackbarHostState.showSnackbar(strings.attendanceSavedSuccess)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_attendance_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.save, fontSize = 12.sp)
                    }
                }
            }
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Group Picker Chips
            Text(
                text = "اختر المجموعة الدراسية:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                groups.forEach { group ->
                    FilterChip(
                        selected = selectedGroup?.id == group.id,
                        onClick = { selectedGroup = group },
                        label = { Text(group.name, fontSize = 12.sp) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Quick Stats Card
            val presentCount = students.count { (attendanceState[it.id] ?: "PRESENT") == "PRESENT" }
            val absentCount = students.count { (attendanceState[it.id] ?: "PRESENT") == "ABSENT" }
            val lateCount = students.count { (attendanceState[it.id] ?: "PRESENT") == "LATE" }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    AttendanceStatBadge(label = strings.statusPresent, count = presentCount, color = MaterialTheme.colorScheme.primary)
                    AttendanceStatBadge(label = strings.statusAbsent, count = absentCount, color = MaterialTheme.colorScheme.error)
                    AttendanceStatBadge(label = strings.statusLate, count = lateCount, color = MaterialTheme.colorScheme.tertiary)
                }
            }

            // Student Roster with Toggle States
            if (students.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = strings.noDataFound, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(students, key = { it.id }) { student ->
                        val currentStatus = attendanceState[student.id] ?: "PRESENT"

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("attendance_row_${student.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = student.fullName,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = student.phone,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    StatusToggleChip(
                                        label = strings.statusPresent,
                                        isSelected = currentStatus == "PRESENT",
                                        activeColor = MaterialTheme.colorScheme.primary,
                                        onClick = { attendanceState[student.id] = "PRESENT" }
                                    )
                                    StatusToggleChip(
                                        label = strings.statusAbsent,
                                        isSelected = currentStatus == "ABSENT",
                                        activeColor = MaterialTheme.colorScheme.error,
                                        onClick = { attendanceState[student.id] = "ABSENT" }
                                    )
                                    StatusToggleChip(
                                        label = strings.statusLate,
                                        isSelected = currentStatus == "LATE",
                                        activeColor = MaterialTheme.colorScheme.tertiary,
                                        onClick = { attendanceState[student.id] = "LATE" }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLiveQrDialog) {
        LiveQrModal(
            group = selectedGroup,
            strings = strings,
            onDismiss = { showLiveQrDialog = false },
            onPrintPoster = {
                showLiveQrDialog = false
                onNavigateToPrintQr()
            }
        )
    }
}

@Composable
fun StatusToggleChip(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun AttendanceStatBadge(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = "$count", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun LiveQrModal(
    group: GroupEntity?,
    strings: AppStrings,
    onDismiss: () -> Unit,
    onPrintPoster: () -> Unit = {}
) {
    var countdown by remember { mutableIntStateOf(60) }

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000)
            countdown--
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(text = strings.generateQr, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = group?.name ?: "الحصة المباشرة",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                // Interactive QR Box Simulation
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = "Live QR",
                        tint = Color.Black,
                        modifier = Modifier.size(150.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "يتجدد الرمز تلقائياً خلال: $countdown ثانية",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onPrintPoster,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("dialog_print_poster_button")
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.printQrCode, fontSize = 12.sp)
                }
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(strings.cancel, fontSize = 12.sp)
                }
            }
        }
    )
}

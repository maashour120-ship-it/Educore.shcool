package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppStrings
import com.example.data.EducoreDataRepository
import com.example.data.local.GroupEntity
import com.example.data.local.StudentEntity
import com.example.data.local.WarningEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsManagementScreen(
    strings: AppStrings,
    dataRepository: EducoreDataRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val studentsFlow = dataRepository.getAllStudentsFlow()
    val students by (studentsFlow?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })

    val groupsFlow = dataRepository.getAllGroupsFlow()
    val groups by (groupsFlow?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })

    var searchQuery by remember { mutableStateOf("") }
    var selectedGroupId by remember { mutableStateOf<String?>(null) }
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var studentForWarning by remember { mutableStateOf<StudentEntity?>(null) }
    var studentProfileToShow by remember { mutableStateOf<StudentEntity?>(null) }

    val filteredStudents = students.filter { student ->
        val matchesQuery = searchQuery.isBlank() ||
                student.fullName.contains(searchQuery, ignoreCase = true) ||
                student.phone.contains(searchQuery) ||
                student.parentPhone.contains(searchQuery)

        val matchesGroup = selectedGroupId == null || student.groupId == selectedGroupId

        matchesQuery && matchesGroup
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.students,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("students_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.cancel
                        )
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { showAddStudentDialog = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("add_student_top_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.addStudent, fontSize = 13.sp)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddStudentDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_student")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = strings.addStudent)
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
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(strings.searchStudentsPlaceholder, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("search_student_field")
            )

            // Filter Chips by Group
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedGroupId == null,
                        onClick = { selectedGroupId = null },
                        label = { Text(strings.filterAllGroups, fontSize = 12.sp) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
                items(groups) { group ->
                    FilterChip(
                        selected = selectedGroupId == group.id,
                        onClick = { selectedGroupId = if (selectedGroupId == group.id) null else group.id },
                        label = { Text(group.name, fontSize = 12.sp) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Students List
            if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.noDataFound,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredStudents, key = { it.id }) { student ->
                        StudentCardItem(
                            student = student,
                            strings = strings,
                            onCallParent = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${student.parentPhone}")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("الهاتف: ${student.parentPhone}")
                                    }
                                }
                            },
                            onWhatsAppParent = {
                                val cleanPhone = student.parentPhone.replace("+", "").replace(" ", "")
                                val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("واتساب: ${student.parentPhone}")
                                    }
                                }
                            },
                            onIssueWarning = {
                                studentForWarning = student
                            },
                            onDelete = {
                                coroutineScope.launch {
                                    dataRepository.deleteStudent(student.id)
                                    snackbarHostState.showSnackbar("تم حذف الطالب من السجل")
                                }
                            },
                            onClick = {
                                studentProfileToShow = student
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddStudentDialog) {
        AddStudentDialog(
            strings = strings,
            groups = groups,
            onDismiss = { showAddStudentDialog = false },
            onConfirm = { name, phone, parentPhone, email, selectedGroup ->
                coroutineScope.launch {
                    val newStudent = StudentEntity(
                        id = "student-${System.currentTimeMillis()}",
                        fullName = name,
                        email = email,
                        phone = phone,
                        parentPhone = parentPhone,
                        groupId = selectedGroup?.id ?: "group-1",
                        groupName = selectedGroup?.name ?: "مجموعة عامة",
                        teacherCode = "mahmoud123",
                        attendanceRate = 100,
                        averageGrade = 90,
                        warningsCount = 0
                    )
                    dataRepository.addStudent(newStudent)
                    showAddStudentDialog = false
                    snackbarHostState.showSnackbar("تم إضافة الطالب بنجاح")
                }
            }
        )
    }

    if (studentForWarning != null) {
        IssueWarningDialog(
            student = studentForWarning!!,
            strings = strings,
            onDismiss = { studentForWarning = null },
            onConfirm = { reason, severity ->
                coroutineScope.launch {
                    val warning = WarningEntity(
                        id = "warn-${System.currentTimeMillis()}",
                        studentId = studentForWarning!!.id,
                        studentName = studentForWarning!!.fullName,
                        teacherCode = "mahmoud123",
                        teacherName = "أ. محمود عبد العال",
                        reason = reason,
                        severity = severity,
                        date = "اليوم"
                    )
                    dataRepository.addWarning(warning)
                    // Update warnings count
                    val updatedStudent = studentForWarning!!.copy(
                        warningsCount = studentForWarning!!.warningsCount + 1
                    )
                    dataRepository.updateStudent(updatedStudent)

                    studentForWarning = null
                    snackbarHostState.showSnackbar(strings.warningSentSuccess)
                }
            }
        )
    }

    if (studentProfileToShow != null) {
        StudentProfileDialog(
            student = studentProfileToShow!!,
            strings = strings,
            onDismiss = { studentProfileToShow = null }
        )
    }
}

@Composable
fun StudentCardItem(
    student: StudentEntity,
    strings: AppStrings,
    onCallParent: () -> Unit,
    onWhatsAppParent: () -> Unit,
    onIssueWarning: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("student_card_${student.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.fullName.take(1),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = student.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = student.groupName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = strings.delete,
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Text(text = strings.attendanceRate, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${student.attendanceRate}%", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Text(text = strings.averageScore, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${student.averageGrade}%", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.tertiary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (student.warningsCount > 0) MaterialTheme.colorScheme.error.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Text(text = strings.myWarnings, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${student.warningsCount}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (student.warningsCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Contact Parent Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onCallParent,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("اتصال بولي الأمر", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onWhatsAppParent,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("واتساب ولي الأمر", fontSize = 11.sp)
                }

                FilledTonalIconButton(
                    onClick = onIssueWarning,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        Icons.Default.WarningAmber,
                        contentDescription = strings.sendWarning,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddStudentDialog(
    strings: AppStrings,
    groups: List<GroupEntity>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, parentPhone: String, email: String, group: GroupEntity?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var parentPhone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf(groups.firstOrNull()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = strings.addStudent, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.fullName) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_student_name")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(strings.phoneNumber) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_student_phone")
                )
                OutlinedTextField(
                    value = parentPhone,
                    onValueChange = { parentPhone = it },
                    label = { Text(strings.parentPhoneNumber) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_parent_phone")
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(strings.email) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onConfirm(name, phone, parentPhone, email, selectedGroup)
                    }
                },
                modifier = Modifier.testTag("confirm_create_student_button")
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}

@Composable
fun IssueWarningDialog(
    student: StudentEntity,
    strings: AppStrings,
    onDismiss: () -> Unit,
    onConfirm: (reason: String, severity: String) -> Unit
) {
    var reason by remember { mutableStateOf("غياب متكرر بدون إذن مسبق") }
    var severity by remember { mutableStateOf("MEDIUM") }

    val presetReasons = listOf(
        "غياب متكرر بدون إذن مسبق",
        "عدم تسليم الواجب المنزلي",
        "انخفاض درجات الامتحان الشهري",
        "تأخر مستمر عن موعد بدء الحصة",
        "عدم الانتباه والتشويش أثناء الشرح"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "إصدار إنذار للطالب: ${student.fullName}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = strings.warningReason, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "أسباب شائعة:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                presetReasons.take(3).forEach { preset ->
                    FilterChip(
                        selected = reason == preset,
                        onClick = { reason = preset },
                        label = { Text(preset, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(text = strings.warningSeverity, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = severity == "LOW",
                        onClick = { severity = "LOW" },
                        label = { Text(strings.severityLow, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = severity == "MEDIUM",
                        onClick = { severity = "MEDIUM" },
                        label = { Text(strings.severityMedium, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = severity == "HIGH",
                        onClick = { severity = "HIGH" },
                        label = { Text(strings.severityHigh, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reason, severity) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("إصدار الإنذار")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}

@Composable
fun StudentProfileDialog(
    student: StudentEntity,
    strings: AppStrings,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = student.fullName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ProfileDetailRow(label = "المجموعة:", value = student.groupName)
                ProfileDetailRow(label = strings.phoneNumber, value = student.phone)
                ProfileDetailRow(label = strings.parentPhoneNumber, value = student.parentPhone)
                ProfileDetailRow(label = strings.email, value = student.email)
                ProfileDetailRow(label = strings.attendanceRate, value = "${student.attendanceRate}%")
                ProfileDetailRow(label = strings.averageScore, value = "${student.averageGrade}%")
                ProfileDetailRow(label = "عدد الإنذارات المسجلة:", value = "${student.warningsCount}")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

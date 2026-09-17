package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
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
import com.example.data.local.StudentEntity
import com.example.data.local.WarningEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarningsManagementScreen(
    strings: AppStrings,
    dataRepository: EducoreDataRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val warningsFlow = dataRepository.getAllWarningsFlow()
    val warnings by (warningsFlow?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })

    val studentsFlow = dataRepository.getAllStudentsFlow()
    val students by (studentsFlow?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })

    var showCreateWarningDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.myWarnings,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("warnings_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.cancel
                        )
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { showCreateWarningDialog = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("add_warning_top_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إصدار إنذار", fontSize = 12.sp)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateWarningDialog = true },
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                modifier = Modifier.testTag("fab_add_warning")
            ) {
                Icon(Icons.Default.WarningAmber, contentDescription = "إصدار إنذار")
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
            Text(
                text = "سجل الملاحظات والإنذارات الصادرة للطلاب مع إشعار ولي الأمر",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (warnings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "لا توجد إنذارات مسجلة حالياً", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(warnings, key = { it.id }) { warning ->
                        WarningCardItem(warning = warning, strings = strings)
                    }
                }
            }
        }
    }

    if (showCreateWarningDialog) {
        CreateWarningModal(
            strings = strings,
            students = students,
            onDismiss = { showCreateWarningDialog = false },
            onConfirm = { student, reason, severity ->
                coroutineScope.launch {
                    val newWarning = WarningEntity(
                        id = "warn-${System.currentTimeMillis()}",
                        studentId = student.id,
                        studentName = student.fullName,
                        teacherCode = "mahmoud123",
                        teacherName = "أ. محمود عبد العال",
                        reason = reason,
                        severity = severity,
                        date = "اليوم"
                    )
                    dataRepository.addWarning(newWarning)
                    val updated = student.copy(warningsCount = student.warningsCount + 1)
                    dataRepository.updateStudent(updated)

                    showCreateWarningDialog = false
                    snackbarHostState.showSnackbar(strings.warningSentSuccess)
                }
            }
        )
    }
}

@Composable
fun WarningCardItem(warning: WarningEntity, strings: AppStrings) {
    val (badgeColor, badgeText) = when (warning.severity) {
        "HIGH" -> MaterialTheme.colorScheme.error to strings.severityHigh
        "MEDIUM" -> MaterialTheme.colorScheme.tertiary to strings.severityMedium
        else -> MaterialTheme.colorScheme.primary to strings.severityLow
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(badgeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = warning.studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = warning.teacherName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = warning.reason,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = warning.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun CreateWarningModal(
    strings: AppStrings,
    students: List<StudentEntity>,
    onDismiss: () -> Unit,
    onConfirm: (student: StudentEntity, reason: String, severity: String) -> Unit
) {
    var selectedStudent by remember { mutableStateOf(students.firstOrNull()) }
    var reason by remember { mutableStateOf("غياب متكرر بدون إذن") }
    var severity by remember { mutableStateOf("MEDIUM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "إصدار إنذار جديد", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "اختر الطالب:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                if (students.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        students.take(3).forEach { student ->
                            FilterChip(
                                selected = selectedStudent?.id == student.id,
                                onClick = { selectedStudent = student },
                                label = { Text(student.fullName.split(" ").firstOrNull() ?: "", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(strings.warningReason) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = strings.warningSeverity, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                onClick = {
                    if (selectedStudent != null && reason.isNotBlank()) {
                        onConfirm(selectedStudent!!, reason, severity)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("إصدار")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}

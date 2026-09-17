package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppStrings
import com.example.data.EducoreDataRepository
import com.example.data.local.ExamEntity
import com.example.data.local.GradeEntity
import com.example.data.local.GroupEntity
import com.example.data.local.StudentEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamsManagementScreen(
    strings: AppStrings,
    dataRepository: EducoreDataRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val examsFlow = dataRepository.getAllExamsFlow()
    val exams by (examsFlow?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })

    val groupsFlow = dataRepository.getAllGroupsFlow()
    val groups by (groupsFlow?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })

    var showCreateExamDialog by remember { mutableStateOf(false) }
    var examForGrading by remember { mutableStateOf<ExamEntity?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.exams,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("exams_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.cancel
                        )
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { showCreateExamDialog = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("create_exam_top_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.createExam, fontSize = 13.sp)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateExamDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_create_exam")
            ) {
                Icon(Icons.Default.Add, contentDescription = strings.createExam)
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
                text = strings.examsDesc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (exams.isEmpty()) {
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
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(exams, key = { it.id }) { exam ->
                        ExamCardItem(
                            exam = exam,
                            strings = strings,
                            onEnterGrades = {
                                examForGrading = exam
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateExamDialog) {
        CreateExamDialog(
            strings = strings,
            groups = groups,
            onDismiss = { showCreateExamDialog = false },
            onConfirm = { title, subject, group, maxScore, date ->
                coroutineScope.launch {
                    val newExam = ExamEntity(
                        id = "exam-${System.currentTimeMillis()}",
                        title = title,
                        subject = subject,
                        groupId = group?.id ?: "group-1",
                        groupName = group?.name ?: "مجموعة ثالثة ثانوي (أ)",
                        date = date,
                        maxScore = maxScore.toIntOrNull() ?: 100,
                        teacherCode = "mahmoud123"
                    )
                    dataRepository.addExam(newExam)
                    showCreateExamDialog = false
                    snackbarHostState.showSnackbar("تم إنشاء الامتحان بنجاح")
                }
            }
        )
    }

    if (examForGrading != null) {
        EnterGradesDialog(
            exam = examForGrading!!,
            strings = strings,
            dataRepository = dataRepository,
            onDismiss = { examForGrading = null },
            onSave = {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(strings.gradesSavedSuccess)
                }
            }
        )
    }
}

@Composable
fun ExamCardItem(
    exam: ExamEntity,
    strings: AppStrings,
    onEnterGrades: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("exam_card_${exam.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = exam.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "${exam.subject} • ${exam.groupName}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = exam.date, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "${strings.maxMarks}: ${exam.maxScore}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Button(
                    onClick = onEnterGrades,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("enter_grades_button_${exam.id}")
                ) {
                    Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = strings.enterGrades, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun CreateExamDialog(
    strings: AppStrings,
    groups: List<GroupEntity>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, subject: String, group: GroupEntity?, maxScore: String, date: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("فيزياء") }
    var selectedGroup by remember { mutableStateOf(groups.firstOrNull()) }
    var maxScore by remember { mutableStateOf("100") }
    var date by remember { mutableStateOf("2025/05/15") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = strings.createExam, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.examTitle) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_exam_title")
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("المادة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = maxScore,
                    onValueChange = { maxScore = it },
                    label = { Text(strings.maxMarks) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("التاريخ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, subject, selectedGroup, maxScore, date)
                    }
                },
                modifier = Modifier.testTag("confirm_create_exam_button")
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
fun EnterGradesDialog(
    exam: ExamEntity,
    strings: AppStrings,
    dataRepository: EducoreDataRepository,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val studentsFlow = dataRepository.getStudentsByGroupFlow(exam.groupId)
    val students by (studentsFlow?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })

    val studentScores = remember { mutableStateMapOf<String, String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = "${strings.enterGrades}: ${exam.title}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "الدرجة العظمى: ${exam.maxScore}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (students.isEmpty()) {
                    Text(
                        text = "لا يوجد طلاب مسجلين في هذه المجموعة للرصد",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(students) { student ->
                            val currentScore = studentScores[student.id] ?: ""
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = student.fullName,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )

                                    OutlinedTextField(
                                        value = currentScore,
                                        onValueChange = { studentScores[student.id] = it },
                                        placeholder = { Text("/${exam.maxScore}", fontSize = 11.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(50.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        val gradeEntities = students.mapNotNull { student ->
                            val scoreStr = studentScores[student.id]
                            val scoreVal = scoreStr?.toIntOrNull()
                            if (scoreVal != null) {
                                GradeEntity(
                                    id = "grade-${exam.id}-${student.id}",
                                    examId = exam.id,
                                    examTitle = exam.title,
                                    studentId = student.id,
                                    studentName = student.fullName,
                                    subject = exam.subject,
                                    score = scoreVal,
                                    maxScore = exam.maxScore,
                                    date = exam.date,
                                    teacherFeedback = if (scoreVal >= exam.maxScore * 0.85) "ممتاز" else "يحتاج لمزيد من المراجعة"
                                )
                            } else null
                        }
                        dataRepository.addGrades(gradeEntities)
                        onSave()
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_grades_confirm_button")
            ) {
                Text(strings.saveGrades)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}

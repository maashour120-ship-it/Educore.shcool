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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppStrings
import com.example.data.EducoreDataRepository
import com.example.data.local.GroupEntity
import com.example.ui.theme.EducoreSuccess
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PosterTheme(val title: String, val primaryColor: Color, val accentColor: Color) {
    MODERN_BLUE("الأزرق الأكاديمي", Color(0xFF1E3A8A), Color(0xFF3B82F6)),
    EMERALD_GREEN("الأخضر الزمردي", Color(0xFF065F46), Color(0xFF10B981)),
    PURPLE_ROYAL("البنفسجي الملكي", Color(0xFF581C87), Color(0xFF8B5CF6)),
    CLASSIC_DARK("الأسود الكلاسيكي", Color(0xFF111827), Color(0xFF4B5563))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrPrintScreen(
    strings: AppStrings,
    dataRepository: EducoreDataRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val groupsFlow = dataRepository.getAllGroupsFlow()
    val groups by (groupsFlow?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })
    var selectedGroup by remember { mutableStateOf<GroupEntity?>(null) }
    var selectedTheme by remember { mutableStateOf(PosterTheme.MODERN_BLUE) }
    var hallName by remember { mutableStateOf("القاعة الرئيسية 101") }
    var customNotes by remember { mutableStateOf("يرجى المسح فور دخول القاعة - تسجيل الحضور إجباري") }

    val currentDate = remember {
        SimpleDateFormat("yyyy/MM/dd", Locale("ar")).format(Date())
    }
    val sessionCode = remember { "EDU-${(1000..9999).random()}" }

    if (selectedGroup == null && groups.isNotEmpty()) {
        selectedGroup = groups.first()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.printQrPoster,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("qr_print_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.cancel
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            shareQrDetails(
                                context = context,
                                groupName = selectedGroup?.name ?: "المجموعة الدراسية",
                                sessionCode = sessionCode,
                                date = currentDate
                            )
                        },
                        modifier = Modifier.testTag("qr_share_top_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = strings.shareReport)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            shareQrDetails(
                                context = context,
                                groupName = selectedGroup?.name ?: "المجموعة الدراسية",
                                sessionCode = sessionCode,
                                date = currentDate
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("qr_share_whatsapp_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = strings.shareWhatsApp, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            triggerNativePrint(
                                context = context,
                                groupName = selectedGroup?.name ?: "المجموعة الدراسية",
                                subject = selectedGroup?.subject ?: "المادة الدراسية",
                                gradeLevel = selectedGroup?.gradeLevel ?: "",
                                sessionCode = sessionCode,
                                hall = hallName,
                                date = currentDate,
                                notes = customNotes,
                                theme = selectedTheme
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = selectedTheme.primaryColor),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(50.dp)
                            .testTag("qr_print_action_button")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = strings.print, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Group Selection Chips
            item {
                Text(
                    text = "1. اختر المجموعة الدراسية:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(groups) { group ->
                        val isSelected = selectedGroup?.id == group.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedGroup = group },
                            label = { Text(group.name, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = selectedTheme.accentColor.copy(alpha = 0.2f),
                                selectedLabelColor = selectedTheme.primaryColor
                            )
                        )
                    }
                }
            }

            // Poster Theme & Customization Selector
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "2. نمط وألوان البوستر:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                PosterTheme.entries.forEach { theme ->
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(theme.primaryColor)
                                            .border(
                                                width = if (selectedTheme == theme) 3.dp else 1.dp,
                                                color = if (selectedTheme == theme) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .testTag("theme_picker_${theme.name}")
                                            .clickable { selectedTheme = theme }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = hallName,
                                onValueChange = { hallName = it },
                                label = { Text(strings.roomHall, fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = customNotes,
                                onValueChange = { customNotes = it },
                                label = { Text("الملاحظات / الإرشادات", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1.5f)
                            )
                        }
                    }
                }
            }

            // Printable Poster Preview (High-Res Frame)
            item {
                Text(
                    text = "3. معاينة ورقة وملصق الحضور المطبوع:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                PrintablePosterCard(
                    groupName = selectedGroup?.name ?: "المجموعة الدراسية",
                    subject = selectedGroup?.subject ?: "المادة الدراسية",
                    gradeLevel = selectedGroup?.gradeLevel ?: "الصف الثالث الثانوي",
                    hall = hallName,
                    sessionCode = sessionCode,
                    date = currentDate,
                    notes = customNotes,
                    theme = selectedTheme,
                    strings = strings
                )
            }

            // Instructions Box
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "يمكنك طباعة هذا الملصق ولصقه على باب القاعة أو عرضه على شاشة العرض/البروجيكتور ليقوم الطلاب بتسجيل حضورهم فوراً.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrintablePosterCard(
    groupName: String,
    subject: String,
    gradeLevel: String,
    hall: String,
    sessionCode: String,
    date: String,
    notes: String,
    theme: PosterTheme,
    strings: AppStrings
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(2.dp, theme.primaryColor.copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("printable_poster_preview")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(theme.primaryColor, theme.accentColor)
                        )
                    )
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Educore Platform",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "تسجيل الحضور الذكي",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = date,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Class & Group Info
            Text(
                text = groupName,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = Color(0xFF111827),
                textAlign = TextAlign.Center
            )
            Text(
                text = "$subject • $gradeLevel • $hall",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = theme.accentColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stylized Printable QR Container
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF9FAFB))
                    .border(2.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = "Printable QR Code",
                        tint = Color.Black,
                        modifier = Modifier.size(170.dp)
                    )
                }

                // Decorative Center Badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, theme.primaryColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = theme.primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Session Security Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF3F4F6),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${strings.sessionSecurityCode}: ",
                        fontSize = 12.sp,
                        color = Color(0xFF4B5563)
                    )
                    Text(
                        text = sessionCode,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.primaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Student Step-by-Step Instructions
            Text(
                text = strings.posterInstructions,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF374151)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9FAFB), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                PosterStepItem(step = "1", title = "افتح التطبيق", icon = Icons.Default.Smartphone, color = theme.primaryColor)
                PosterStepItem(step = "2", title = "امسح الرمز", icon = Icons.Default.CameraAlt, color = theme.primaryColor)
                PosterStepItem(step = "3", title = "تم الحضور", icon = Icons.Default.CheckCircle, color = EducoreSuccess)
            }

            if (notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = notes,
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PosterStepItem(
    step: String,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2937))
    }
}

fun shareQrDetails(context: Context, groupName: String, sessionCode: String, date: String) {
    val shareBody = """
        📢 *رمز تسجيل الحضور - منصة Educore*
        ━━━━━━━━━━━━━━━━━━━━
        📌 المجموعة: $groupName
        📅 التاريخ: $date
        🔑 رمز الجلسة: $sessionCode
        
        📲 يرجى من جميع الطلاب فتح تطبيق Educore ومسح رمز الـ QR من شاشة القاعة لتسجيل الحضور الفوري.
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "رمز تسجيل الحضور - $groupName")
        putExtra(Intent.EXTRA_TEXT, shareBody)
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة تفاصيل الحضور عبر:"))
}

fun triggerNativePrint(
    context: Context,
    groupName: String,
    subject: String,
    gradeLevel: String,
    sessionCode: String,
    hall: String,
    date: String,
    notes: String,
    theme: PosterTheme
) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
    val jobName = "Educore_Attendance_QR_${groupName.replace(" ", "_")}"

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
                        if (cancellationSignal?.isCanceled == true) {
                            callback?.onLayoutCancelled()
                            return
                        }
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
                        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size in points
                        val page = pdfDocument.startPage(pageInfo)
                        val canvas: Canvas = page.canvas

                        val paint = Paint().apply {
                            isAntiAlias = true
                        }

                        // Background
                        paint.color = android.graphics.Color.WHITE
                        canvas.drawRect(0f, 0f, 595f, 842f, paint)

                        // Header Bar
                        paint.color = android.graphics.Color.rgb(30, 58, 138)
                        canvas.drawRoundRect(40f, 40f, 555f, 120f, 16f, 16f, paint)

                        // Header Title
                        paint.color = android.graphics.Color.WHITE
                        paint.textSize = 22f
                        paint.isFakeBoldText = true
                        paint.textAlign = Paint.Align.CENTER
                        canvas.drawText("Educore Platform - Smart Attendance", 595f / 2, 85f, paint)

                        // Date
                        paint.textSize = 12f
                        paint.isFakeBoldText = false
                        canvas.drawText("Date: $date  |  Session: $sessionCode", 595f / 2, 105f, paint)

                        // Group & Subject
                        paint.color = android.graphics.Color.BLACK
                        paint.textSize = 20f
                        paint.isFakeBoldText = true
                        canvas.drawText(groupName, 595f / 2, 170f, paint)

                        paint.textSize = 14f
                        paint.color = android.graphics.Color.DKGRAY
                        paint.isFakeBoldText = false
                        canvas.drawText("$subject - $gradeLevel - $hall", 595f / 2, 195f, paint)

                        // QR Placeholder box
                        paint.color = android.graphics.Color.LTGRAY
                        canvas.drawRect(147f, 230f, 447f, 530f, paint)

                        paint.color = android.graphics.Color.BLACK
                        paint.textSize = 24f
                        paint.isFakeBoldText = true
                        canvas.drawText("[ QR CODE ]", 595f / 2, 380f, paint)
                        paint.textSize = 14f
                        canvas.drawText("Scan with Educore App", 595f / 2, 410f, paint)

                        // Session Code Box
                        paint.color = android.graphics.Color.rgb(240, 240, 240)
                        canvas.drawRoundRect(160f, 560f, 435f, 610f, 10f, 10f, paint)
                        paint.color = android.graphics.Color.rgb(30, 58, 138)
                        paint.textSize = 16f
                        paint.isFakeBoldText = true
                        canvas.drawText("Security Code: $sessionCode", 595f / 2, 592f, paint)

                        // Instructions
                        paint.color = android.graphics.Color.BLACK
                        paint.textSize = 14f
                        paint.isFakeBoldText = true
                        canvas.drawText("Instructions for Students:", 595f / 2, 650f, paint)

                        paint.textSize = 12f
                        paint.color = android.graphics.Color.DKGRAY
                        paint.isFakeBoldText = false
                        canvas.drawText("1. Open the Educore Student App", 595f / 2, 675f, paint)
                        canvas.drawText("2. Click on 'Scan QR' on your Dashboard", 595f / 2, 695f, paint)
                        canvas.drawText("3. Scan this poster to verify attendance immediately", 595f / 2, 715f, paint)

                        if (notes.isNotBlank()) {
                            paint.textSize = 11f
                            paint.color = android.graphics.Color.GRAY
                            canvas.drawText(notes, 595f / 2, 760f, paint)
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
                PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .build()
            )
        } catch (e: Exception) {
            Toast.makeText(context, "بدء الطباعة: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(context, "تم تجهيز ملف الطباعة بنجاح", Toast.LENGTH_SHORT).show()
    }
}

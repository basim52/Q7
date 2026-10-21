package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import com.example.export.FileSharing
import com.example.model.AppCurrency
import com.example.model.ExportHistoryItem
import com.example.ui.theme.CutLineRed
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LaserCyan400
import com.example.ui.theme.SparkOrange500

@Composable
fun StudioTopHeader(
    workshopName: String,
    currency: AppCurrency,
    exportHistory: List<ExportHistoryItem>,
    isDarkTheme: Boolean,
    isArabic: Boolean,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onSetWorkshopName: (String) -> Unit,
    onSetCurrency: (AppCurrency) -> Unit,
    onOpenExportItem: (ExportHistoryItem) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSettingsDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand Identity
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(LaserCyan400, SparkOrange500)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "LaserForge Studio Logo",
                            tint = Color(0xFF090D16),
                            modifier = Modifier.size(21.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isArabic) "ليزر فورج" else "LASERFORGE",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "CAD•CAM",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = workshopName.ifBlank {
                                if (isArabic) "اضغط ⚙ لإضافة اسم ورشتك" else "Tap ⚙ to add your workshop name"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Controls: Language Switcher (عربي / EN) + Theme Toggle + Workshop Settings
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Language Toggle Button (EN / عربي)
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onToggleLanguage() }
                            .testTag("language_toggle_button"),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Switch Language",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isArabic) "EN" else "عربي",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Dark / Light Theme Toggle
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkTheme) "Light Mode" else "Dark Mode",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Workshop settings (name, currency, export history)
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .testTag("workshop_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = if (isArabic) "إعدادات الورشة" else "Workshop settings",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Subtle Laser Blueprint Accent Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                LaserCyan400.copy(alpha = 0.8f),
                                SparkOrange500.copy(alpha = 0.8f),
                                LaserCyan400.copy(alpha = 0.3f)
                            )
                        )
                    )
            )
        }
    }

    if (showSettingsDialog) {
        WorkshopSettingsDialog(
            workshopName = workshopName,
            currency = currency,
            exportHistory = exportHistory,
            isArabic = isArabic,
            onSetWorkshopName = onSetWorkshopName,
            onSetCurrency = onSetCurrency,
            onClearHistory = onClearHistory,
            onOpenExportItem = { item ->
                showSettingsDialog = false
                onOpenExportItem(item)
            },
            onDismiss = { showSettingsDialog = false }
        )
    }
}

@Composable
fun StatusNotificationBanner(
    message: String?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(visible = message != null) {
        if (message != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss notification",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkshopSettingsDialog(
    workshopName: String,
    currency: AppCurrency,
    exportHistory: List<ExportHistoryItem>,
    isArabic: Boolean,
    onSetWorkshopName: (String) -> Unit,
    onSetCurrency: (AppCurrency) -> Unit,
    onClearHistory: () -> Unit,
    onOpenExportItem: (ExportHistoryItem) -> Unit,
    onDismiss: () -> Unit
) {
    var nameDraft by remember { mutableStateOf(workshopName) }
    Dialog(onDismissRequest = { onSetWorkshopName(nameDraft.trim()); onDismiss() }) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "إعدادات الورشة" else "Workshop Settings",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = { onSetWorkshopName(nameDraft.trim()); onDismiss() }) {
                        Icon(Icons.Default.Close, contentDescription = if (isArabic) "إغلاق" else "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nameDraft,
                    onValueChange = { nameDraft = it.take(60) },
                    label = { Text(if (isArabic) "اسم الورشة (يظهر في عروض الأسعار)" else "Workshop name (shown on quotes)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("workshop_name_input")
                )
                Text(
                    text = if (isArabic) "يُحفظ على جهازك فقط، بدون حساب أو تسجيل دخول." else "Stored on this device only — no account or sign-in.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = if (isArabic) "العملة" else "CURRENCY",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AppCurrency.entries.forEach { c ->
                        FilterChip(
                            selected = c == currency,
                            onClick = { onSetCurrency(c) },
                            label = { Text("${c.symbol(isArabic)} • ${c.displayName(isArabic)}") },
                            modifier = Modifier.testTag("currency_${c.code}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "سجل الملفات المصدرة (${exportHistory.size})" else "EXPORT HISTORY (${exportHistory.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (exportHistory.isNotEmpty()) {
                        TextButton(onClick = onClearHistory) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isArabic) "مسح السجل" else "Clear")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (exportHistory.isEmpty()) {
                    Text(
                        text = if (isArabic) {
                            "لا توجد ملفات مصدرة بعد. ولّد مسار فيكتور في الأداة 1 أو صدّر صندوقاً في الأداة 2."
                        } else {
                            "No exports yet. Generate a vector in Tool 1 or export a box in Tool 2."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 240.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        exportHistory.forEach { item ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onOpenExportItem(item) },
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = item.fileName,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${item.toolName(isArabic)} • ${item.timestampLabel}" +
                                            if (item.dxfContent != null) " • SVG + DXF" else " • SVG",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SvgExportModalDialog(
    exportItem: ExportHistoryItem,
    isArabic: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val saveSvgLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/svg+xml")
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(exportItem.svgContent.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(
                    context,
                    if (isArabic) "تم حفظ الملف ${exportItem.fileName} بنجاح!" else "Saved ${exportItem.fileName} successfully!",
                    Toast.LENGTH_LONG
                ).show()
            }.onFailure {
                Toast.makeText(context, (if (isArabic) "خطأ: " else "Error: ") + it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val saveDxfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/dxf")
    ) { uri ->
        val dxf = exportItem.dxfContent
        if (uri != null && dxf != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(dxf.toByteArray(Charsets.US_ASCII))
                }
                Toast.makeText(
                    context,
                    if (isArabic) "تم حفظ الملف ${exportItem.dxfFileName} بنجاح!" else "Saved ${exportItem.dxfFileName} successfully!",
                    Toast.LENGTH_LONG
                ).show()
            }.onFailure {
                Toast.makeText(context, (if (isArabic) "خطأ: " else "Error: ") + it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (exportItem.dxfContent != null) {
                                    if (isArabic) "ملفات القص SVG و DXF جاهزة" else "SVG & DXF Cut Files Ready"
                                } else {
                                    if (isArabic) "ملف الفيكتور SVG جاهز للقص" else "Clean SVG Path Ready"
                                },
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = if (exportItem.dxfContent != null) {
                                    "${exportItem.fileName} • ${exportItem.dxfFileName}"
                                } else {
                                    exportItem.fileName
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = SparkOrange500
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) {
                                "كود المسار المصدري (جاهز لـ LightBurn / Silhouette)"
                            } else {
                                "PRODUCTION SVG SOURCE (LIGHTBURN / SILHOUETTE)"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${exportItem.svgContent.length} B",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Keep SVG XML code block LTR for clean code readability
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp),
                        color = Color(0xFF090D16),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                                .horizontalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = exportItem.svgContent,
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { saveSvgLauncher.launch(exportItem.fileName) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("download_svg_file_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) {
                            "حفظ ملف ${exportItem.fileName} على الجهاز"
                        } else {
                            "Save ${exportItem.fileName} to Device"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                if (exportItem.dxfContent != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { saveDxfLauncher.launch(exportItem.dxfFileName) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("download_dxf_file_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = SparkOrange500, contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "حفظ DXF" else "Save DXF")
                        }
                        OutlinedButton(
                            onClick = {
                                runCatching {
                                    FileSharing.shareBytes(
                                        context,
                                        exportItem.dxfFileName,
                                        exportItem.dxfContent.toByteArray(Charsets.US_ASCII),
                                        "application/dxf",
                                        if (isArabic) "مشاركة ملف DXF" else "Share DXF"
                                    )
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("share_dxf_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "مشاركة DXF" else "Share DXF")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText(exportItem.fileName, exportItem.svgContent))
                            Toast.makeText(
                                context,
                                if (isArabic) "تم نسخ كود SVG إلى الحافظة!" else "Copied clean SVG code to clipboard!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("copy_svg_code_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabic) "نسخ كود SVG" else "Copy SVG XML")
                    }

                    OutlinedButton(
                        onClick = {
                            runCatching {
                                FileSharing.shareBytes(
                                    context,
                                    exportItem.fileName,
                                    exportItem.svgContent.toByteArray(Charsets.UTF_8),
                                    "image/svg+xml",
                                    if (isArabic) "مشاركة ملف SVG" else "Export SVG to Laser Software"
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("share_svg_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabic) "مشاركة الملف" else "Share SVG")
                    }
                }
            }
        }
    }
}

@Composable
fun WorkshopBlueprintBanner(
    activeToolBadge: String,
    activeToolTitle: String,
    activeToolSubtitle: String,
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = Color(0xFF0B1120),
        border = BorderStroke(1.dp, LaserCyan400.copy(alpha = 0.35f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val step = 24.dp.toPx()
                val gridColor = Color(0xFF1E293B)
                var x = 0f
                while (x < size.width) {
                    drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                    x += step
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                    y += step
                }

                val rx = if (isArabic) size.width * 0.15f else size.width * 0.85f
                val ry = size.height * 0.5f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(LaserCyan400.copy(alpha = 0.25f), Color.Transparent),
                        center = Offset(rx, ry),
                        radius = size.height * 0.9f
                    ),
                    center = Offset(rx, ry),
                    radius = size.height * 0.9f
                )
                drawCircle(
                    color = CutLineRed.copy(alpha = 0.65f),
                    radius = size.height * 0.34f,
                    center = Offset(rx, ry),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                )
                drawLine(
                    color = SparkOrange500.copy(alpha = 0.8f),
                    start = Offset(rx, 0f),
                    end = Offset(rx, ry),
                    strokeWidth = 2.5.dp.toPx()
                )
                drawCircle(
                    color = Color(0xFFFACC15),
                    radius = 4.dp.toPx(),
                    center = Offset(rx, ry)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Surface(
                    color = LaserCyan400.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, LaserCyan400.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = if (isArabic) {
                            "$activeToolBadge • منصة قص الليزر والـ CNC"
                        } else {
                            "$activeToolBadge • CNC & LASER WORKBENCH"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = LaserCyan400,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = activeToolTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFFF8FAFC)
                )
                Text(
                    text = activeToolSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

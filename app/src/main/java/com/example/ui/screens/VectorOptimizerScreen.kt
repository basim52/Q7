package com.example.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Polyline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.SampleArtworkPreset
import com.example.model.TargetLaserSoftware
import com.example.model.VectorOptimizerConfig
import com.example.model.VectorPreviewResult
import com.example.ui.components.WorkshopBlueprintBanner
import com.example.ui.theme.CutLineRed
import com.example.ui.theme.EngraveBlue
import com.example.ui.theme.LaserCyan400
import com.example.ui.theme.SparkOrange500
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun VectorOptimizerScreen(
    isArabic: Boolean,
    selectedPreset: SampleArtworkPreset,
    artworkTitle: String,
    config: VectorOptimizerConfig,
    preview: VectorPreviewResult?,
    isGenerating: Boolean,
    onSelectPreset: (SampleArtworkPreset) -> Unit,
    onCustomBitmapSelected: (android.graphics.Bitmap, String) -> Unit,
    onUpdateConfig: ((VectorOptimizerConfig) -> VectorOptimizerConfig) -> Unit,
    onGenerateSvg: () -> Unit,
    onSyncToCostCalculator: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    fun loadUriBitmap(uri: Uri) {
        runCatching {
            var displayName = if (isArabic) "صورة_مرفوعة" else "Uploaded_Artwork"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    displayName = cursor.getString(nameIndex) ?: displayName
                }
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val bmp = BitmapFactory.decodeStream(stream)
                if (bmp != null) {
                    onCustomBitmapSelected(bmp, displayName)
                }
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) loadUriBitmap(uri)
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) loadUriBitmap(uri)
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 740.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WorkshopBlueprintBanner(
                activeToolBadge = if (isArabic) "الأداة 01" else "TOOL 01",
                activeToolTitle = if (isArabic) {
                    "محول ومحسن الصور إلى فيكتور لقص الليزر"
                } else {
                    "Vector & Image Optimizer for Laser Cutters"
                },
                activeToolSubtitle = if (isArabic) {
                    "تحويل صور PNG و JPG والشعارات إلى مسارات قص فيكتور SVG نظيفة لبرامج LightBurn و Silhouette."
                } else {
                    "Convert PNG, JPG & raster emblems into clean hairline SVG cut paths for LightBurn & Silhouette."
                },
                isArabic = isArabic
            )

            // File Upload Zone + Preset Selector Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("file_upload_zone"),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Upload Artwork",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(34.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isArabic) {
                                    "اسحب أو اختر صورة (PNG / JPG / SVG)"
                                } else {
                                    "Drop or Select Raster / SVG Artwork"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isArabic) {
                                    "يدعم PNG و JPG و SVG • التصميم الحالي: $artworkTitle"
                                } else {
                                    "Supports PNG, JPG, SVG • Active File: $artworkTitle"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier.testTag("pick_image_button")
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isArabic) "اختر صورة من المعرض" else "Pick PNG / JPG")
                                }
                                OutlinedButton(
                                    onClick = {
                                        documentPickerLauncher.launch(arrayOf("image/*", "image/svg+xml"))
                                    },
                                    modifier = Modifier.testTag("browse_files_button")
                                ) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isArabic) "تصفح الملفات" else "Browse Files")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isArabic) {
                            "أو اختر نموذجاً جاهزاً للمعاينة والتجربة الفورية:"
                        } else {
                            "OR LOAD CALIBRATED WORKSHOP TEST ARTWORK:"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SampleArtworkPreset.entries.forEach { preset ->
                            val isSelected = selectedPreset == preset
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectPreset(preset) },
                                label = { Text(preset.title(isArabic)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("preset_chip_${preset.name.lowercase(Locale.US)}")
                            )
                        }
                    }
                }
            }

            if (isWideScreen) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    VectorCanvasPreviewCard(
                        isArabic = isArabic,
                        artworkTitle = artworkTitle,
                        config = config,
                        preview = preview,
                        onUpdateConfig = onUpdateConfig,
                        modifier = Modifier.weight(1.05f)
                    )
                    VectorParametersCard(
                        isArabic = isArabic,
                        config = config,
                        preview = preview,
                        isGenerating = isGenerating,
                        onUpdateConfig = onUpdateConfig,
                        onGenerateSvg = onGenerateSvg,
                        onSyncToCostCalculator = onSyncToCostCalculator,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                VectorCanvasPreviewCard(
                    isArabic = isArabic,
                    artworkTitle = artworkTitle,
                    config = config,
                    preview = preview,
                    onUpdateConfig = onUpdateConfig,
                    modifier = Modifier.fillMaxWidth()
                )
                VectorParametersCard(
                    isArabic = isArabic,
                    config = config,
                    preview = preview,
                    isGenerating = isGenerating,
                    onUpdateConfig = onUpdateConfig,
                    onGenerateSvg = onGenerateSvg,
                    onSyncToCostCalculator = onSyncToCostCalculator,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun VectorCanvasPreviewCard(
    isArabic: Boolean,
    artworkTitle: String,
    config: VectorOptimizerConfig,
    preview: VectorPreviewResult?,
    onUpdateConfig: ((VectorOptimizerConfig) -> VectorOptimizerConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isArabic) "معاينة مسار الليزر المباشرة" else "REAL-TIME LASER TOOLPATH CANVAS",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                FilterChip(
                    selected = config.showVectorOverlay,
                    onClick = { onUpdateConfig { it.copy(showVectorOverlay = !it.showVectorOverlay) } },
                    label = {
                        Text(
                            if (config.showVectorOverlay) {
                                if (isArabic) "مسارات SVG + الصورة" else "SVG Paths + Raster"
                            } else {
                                if (isArabic) "الصورة فقط" else "Threshold Raster Only"
                            },
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Polyline,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    modifier = Modifier.testTag("toggle_vector_overlay_chip")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .testTag("vector_preview_canvas"),
                contentAlignment = Alignment.Center
            ) {
                if (preview != null) {
                    Image(
                        bitmap = preview.processedBitmap.asImageBitmap(),
                        contentDescription = artworkTitle,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val gridDivisions = 10
                        val cellW = size.width / gridDivisions
                        val cellH = size.height / gridDivisions
                        val gridColor = Color(0xFF94A3B8).copy(alpha = 0.28f)

                        for (i in 1 until gridDivisions) {
                            drawLine(
                                color = gridColor,
                                start = Offset(i * cellW, 0f),
                                end = Offset(i * cellW, size.height),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, i * cellH),
                                end = Offset(size.width, i * cellH),
                                strokeWidth = 1f
                            )
                        }

                        drawLine(
                            color = LaserCyan400.copy(alpha = 0.55f),
                            start = Offset(size.width / 2f, 0f),
                            end = Offset(size.width / 2f, size.height),
                            strokeWidth = 1.2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )
                        drawLine(
                            color = LaserCyan400.copy(alpha = 0.55f),
                            start = Offset(0f, size.height / 2f),
                            end = Offset(size.width, size.height / 2f),
                            strokeWidth = 1.2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )

                        if (config.showVectorOverlay) {
                            val strokeColor = when (config.softwarePreset) {
                                TargetLaserSoftware.LIGHTBURN -> CutLineRed
                                TargetLaserSoftware.SILHOUETTE -> SparkOrange500
                                TargetLaserSoftware.GLOWFORGE -> EngraveBlue
                                TargetLaserSoftware.CNC_GRBL -> LaserCyan400
                            }
                            for (contour in preview.contourPaths) {
                                if (contour.size < 2) continue
                                val path = Path()
                                contour.forEachIndexed { idx, pt ->
                                    val px = pt.first * size.width
                                    val py = pt.second * size.height
                                    if (idx == 0) path.moveTo(px, py) else path.lineTo(px, py)
                                }
                                drawPath(
                                    path = path,
                                    color = strokeColor,
                                    style = Stroke(width = 2.2.dp.toPx())
                                )
                            }
                        }
                    }
                } else {
                    CircularProgressIndicator()
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    color = Color(0xDD0F172A),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${config.targetWidthMm.toInt()}×${config.targetWidthMm.toInt()}mm • ${config.softwarePreset.label(isArabic)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (preview != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TelemetryStatChip(
                        label = if (isArabic) "مسارات مغلقة" else "CLOSED LOOPS",
                        value = "${preview.closedLoopCount}",
                        accentColor = LaserCyan400,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryStatChip(
                        label = if (isArabic) "نقاط الفيكتور" else "VECTOR NODES",
                        value = "${preview.nodeCount}",
                        accentColor = SparkOrange500,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryStatChip(
                        label = if (isArabic) "طول القص" else "CUT LENGTH",
                        value = "${(preview.totalCutLengthMm / 10f).roundToInt() / 100f}m",
                        accentColor = CutLineRed,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryStatChip(
                        label = if (isArabic) "وقت الليزر" else "EST. LASER",
                        value = "${preview.estimatedLaserSeconds}s",
                        accentColor = EngraveBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TelemetryStatChip(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = accentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VectorParametersCard(
    isArabic: Boolean,
    config: VectorOptimizerConfig,
    preview: VectorPreviewResult?,
    isGenerating: Boolean,
    onUpdateConfig: ((VectorOptimizerConfig) -> VectorOptimizerConfig) -> Unit,
    onGenerateSvg: () -> Unit,
    onSyncToCostCalculator: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isArabic) "إعدادات تحويل الصورة إلى فيكتور" else "IMAGE CONVERSION PARAMETERS",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // 1. Threshold Slider (0 - 255)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Contrast,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (isArabic) "حساسية التباين (Threshold)" else "Luminance Threshold",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${config.threshold} / 255",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Slider(
                    value = config.threshold.toFloat(),
                    onValueChange = { v -> onUpdateConfig { it.copy(threshold = v.roundToInt()) } },
                    valueRange = 20f..235f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("threshold_slider")
                )
            }

            // 2. Edge Detection Toggle & Invert Colors Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (isArabic) "تتبع الحواف" else "Edge Detection",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                if (isArabic) "استخراج حدود القص" else "Outline cut trace",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = config.edgeDetection,
                            onCheckedChange = { checked ->
                                onUpdateConfig { it.copy(edgeDetection = checked) }
                            },
                            modifier = Modifier.testTag("edge_detection_switch")
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.InvertColors,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (isArabic) "عكس الألوان" else "Invert Colors",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Text(
                                if (isArabic) "تبديل الأبيض والأسود" else "Flip positive/neg",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = config.invertColors,
                            onCheckedChange = { checked ->
                                onUpdateConfig { it.copy(invertColors = checked) }
                            },
                            modifier = Modifier.testTag("invert_colors_switch")
                        )
                    }
                }
            }

            // 3. Path Smoothing & Target Physical Size Sliders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            if (isArabic) "تنعيم المنحنيات" else "Curve Smoothing",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "${(config.pathSmoothing * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = config.pathSmoothing,
                        onValueChange = { v -> onUpdateConfig { it.copy(pathSmoothing = v) } },
                        valueRange = 0f..1f,
                        modifier = Modifier.testTag("smoothing_slider")
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            if (isArabic) "مقاس الإخراج (مم)" else "Output Size (mm)",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "${config.targetWidthMm.roundToInt()} mm",
                            style = MaterialTheme.typography.labelSmall,
                            color = SparkOrange500
                        )
                    }
                    Slider(
                        value = config.targetWidthMm,
                        onValueChange = { v -> onUpdateConfig { it.copy(targetWidthMm = v) } },
                        valueRange = 40f..400f,
                        modifier = Modifier.testTag("output_size_slider")
                    )
                }
            }

            HorizontalDivider()

            // 4. Target Laser Software Profile
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Layers,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isArabic) "تهيئة الملف لبرنامج الليزر:" else "TARGET LASER SOFTWARE PROFILE:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TargetLaserSoftware.entries.forEach { preset ->
                    FilterChip(
                        selected = config.softwarePreset == preset,
                        onClick = { onUpdateConfig { it.copy(softwarePreset = preset) } },
                        label = { Text(preset.label(isArabic)) }
                    )
                }
            }
            Text(
                text = config.softwarePreset.description(isArabic),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 5. Primary CTA: Generate SVG / Vector
            Button(
                onClick = onGenerateSvg,
                enabled = !isGenerating && preview != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("generate_vector_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isArabic) {
                            "جاري تتبع المسارات وتوليد ملف SVG..."
                        } else {
                            "Tracing Contours & Optimizing SVG..."
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                } else {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) {
                            "توليد وتصدير ملف الفيكتور SVG (1 رصيد)"
                        } else {
                            "Generate SVG / Vector (1 Credit)"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            OutlinedButton(
                onClick = onSyncToCostCalculator,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("sync_vector_to_cost_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isArabic) {
                        "احسب تكلفة قص هذا التصميم في الحاسبة"
                    } else {
                        "Quote This Cut Job in Cost Calculator"
                    }
                )
            }
        }
    }
}

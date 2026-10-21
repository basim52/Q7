package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BoxGeneratorConfig
import com.example.model.BoxLayoutSummary
import com.example.model.DEFAULT_MATERIAL_PRESETS
import com.example.model.EnclosureStyle
import com.example.model.MaterialPreset
import com.example.ui.components.WorkshopBlueprintBanner
import com.example.ui.theme.CutLineRed
import com.example.ui.theme.EngraveBlue
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LaserCyan400
import com.example.ui.theme.ScoreAmber
import com.example.ui.theme.SparkOrange500
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun BoxGeneratorScreen(
    isArabic: Boolean,
    config: BoxGeneratorConfig,
    layout: BoxLayoutSummary,
    isExporting: Boolean,
    onUpdateConfig: ((BoxGeneratorConfig) -> BoxGeneratorConfig) -> Unit,
    onApplyMaterialPreset: (MaterialPreset) -> Unit,
    onExportBoxSvg: () -> Unit,
    onSyncToCostCalculator: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                activeToolBadge = if (isArabic) "الأداة 02" else "TOOL 02",
                activeToolTitle = if (isArabic) {
                    "مولد الصناديق والتعشيق الهندسي بالليزر"
                } else {
                    "Custom Box & Enclosure Generator"
                },
                activeToolSubtitle = if (isArabic) {
                    "تصميم صناديق متداخلة بوصلات الأصابع (Finger-Joint) مع تعويض شعاع الليزر وتصدير مخطط الأوجه بصيغة SVG."
                } else {
                    "Parametric interlocking finger-joint CAD generator with kerf compensation & multi-panel SVG export."
                },
                isArabic = isArabic
            )

            // Material Thickness & Enclosure Style Bar
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) {
                            "سماكة الخامة السريعة (خشب MDF / أبلكاش / أكريليك):"
                        } else {
                            "QUICK MATERIAL THICKNESS PRESETS (MDF / PLYWOOD / ACRYLIC):"
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
                        DEFAULT_MATERIAL_PRESETS.forEach { preset ->
                            FilterChip(
                                selected = kotlin.math.abs(config.thicknessMm - preset.thicknessMm) < 0.05f,
                                onClick = { onApplyMaterialPreset(preset) },
                                label = { Text(preset.name(isArabic)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isArabic) "نمط الصندوق والتعشيق:" else "ENCLOSURE TOPOLOGY:",
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
                        EnclosureStyle.entries.forEach { style ->
                            FilterChip(
                                selected = config.enclosureStyle == style,
                                onClick = { onUpdateConfig { it.copy(enclosureStyle = style) } },
                                label = { Text(style.label(isArabic)) },
                                modifier = Modifier.testTag("enclosure_style_${style.name.lowercase(Locale.US)}")
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
                    BoxInteractiveSvgPreviewCard(
                        isArabic = isArabic,
                        config = config,
                        layout = layout,
                        onUpdateConfig = onUpdateConfig,
                        modifier = Modifier.weight(1.1f)
                    )
                    BoxParametersFormCard(
                        isArabic = isArabic,
                        config = config,
                        layout = layout,
                        isExporting = isExporting,
                        onUpdateConfig = onUpdateConfig,
                        onExportBoxSvg = onExportBoxSvg,
                        onSyncToCostCalculator = onSyncToCostCalculator,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                BoxInteractiveSvgPreviewCard(
                    isArabic = isArabic,
                    config = config,
                    layout = layout,
                    onUpdateConfig = onUpdateConfig,
                    modifier = Modifier.fillMaxWidth()
                )
                BoxParametersFormCard(
                    isArabic = isArabic,
                    config = config,
                    layout = layout,
                    isExporting = isExporting,
                    onUpdateConfig = onUpdateConfig,
                    onExportBoxSvg = onExportBoxSvg,
                    onSyncToCostCalculator = onSyncToCostCalculator,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun BoxInteractiveSvgPreviewCard(
    isArabic: Boolean,
    config: BoxGeneratorConfig,
    layout: BoxLayoutSummary,
    onUpdateConfig: ((BoxGeneratorConfig) -> BoxGeneratorConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var zoom by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    val textMeasurer = rememberTextMeasurer()

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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = if (config.showIsometric3D) Icons.Default.ViewInAr else Icons.Default.GridView,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (config.showIsometric3D) {
                            if (isArabic) "معاينة ثلاثية الأبعاد 3D" else "3D ISOMETRIC ASSEMBLY"
                        } else {
                            if (isArabic) "مخطط الأوجه المتداخلة SVG" else "2D FINGER-JOINT SVG PREVIEW"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = !config.showIsometric3D,
                        onClick = { onUpdateConfig { it.copy(showIsometric3D = false) } },
                        label = {
                            Text(
                                if (isArabic) "مخطط 2D" else "2D Layout",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.testTag("view_2d_layout_chip")
                    )
                    FilterChip(
                        selected = config.showIsometric3D,
                        onClick = { onUpdateConfig { it.copy(showIsometric3D = true) } },
                        label = {
                            Text(
                                if (isArabic) "مجسم 3D" else "3D Box",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.testTag("view_3d_box_chip")
                    )
                    IconButton(
                        onClick = {
                            zoom = 1f
                            panOffset = Offset.Zero
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Reset Canvas Zoom",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.25f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF090D16))
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, gestureZoom, _ ->
                            zoom = (zoom * gestureZoom).coerceIn(0.75f, 2.6f)
                            panOffset += pan
                        }
                    }
                    .testTag("box_svg_preview_canvas")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val gridStep = 24.dp.toPx()
                    val gridColor = Color(0xFF1E293B)
                    var gx = 0f
                    while (gx < size.width) {
                        drawLine(gridColor, Offset(gx, 0f), Offset(gx, size.height), strokeWidth = 1f)
                        gx += gridStep
                    }
                    var gy = 0f
                    while (gy < size.height) {
                        drawLine(gridColor, Offset(0f, gy), Offset(size.width, gy), strokeWidth = 1f)
                        gy += gridStep
                    }

                    if (!config.showIsometric3D) {
                        val pad = 18.dp.toPx()
                        val availW = size.width - pad * 2
                        val availH = size.height - pad * 2
                        val baseScale = min(
                            availW / layout.totalSheetWidthMm.coerceAtLeast(1f),
                            availH / layout.totalSheetHeightMm.coerceAtLeast(1f)
                        ) * zoom

                        val contentW = layout.totalSheetWidthMm * baseScale
                        val contentH = layout.totalSheetHeightMm * baseScale
                        val startX = (size.width - contentW) / 2f + panOffset.x
                        val startY = (size.height - contentH) / 2f + panOffset.y

                        drawRect(
                            color = LaserCyan400.copy(alpha = 0.25f),
                            topLeft = Offset(startX, startY),
                            size = androidx.compose.ui.geometry.Size(contentW, contentH),
                            style = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                            )
                        )

                        for (panel in layout.panels) {
                            val path = Path()
                            panel.outlinePointsMm.forEachIndexed { idx, pt ->
                                val px = startX + (panel.offsetXMm + pt.first) * baseScale
                                val py = startY + (panel.offsetYMm + pt.second) * baseScale
                                if (idx == 0) path.moveTo(px, py) else path.lineTo(px, py)
                            }
                            path.close()

                            drawPath(
                                path = path,
                                color = Color(0xFF1E293B).copy(alpha = 0.72f)
                            )
                            drawPath(
                                path = path,
                                color = CutLineRed,
                                style = Stroke(width = 1.75.dp.toPx())
                            )

                            val cx = startX + (panel.offsetXMm + panel.nominalWidthMm / 2f) * baseScale
                            val cy = startY + (panel.offsetYMm + panel.nominalHeightMm / 2f) * baseScale
                            val measured = textMeasurer.measure(
                                text = panel.label,
                                style = TextStyle(
                                    color = EngraveBlue,
                                    fontSize = 9.sp,
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            if (panel.nominalWidthMm * baseScale > measured.size.width + 8) {
                                drawText(
                                    textLayoutResult = measured,
                                    topLeft = Offset(
                                        cx - measured.size.width / 2f,
                                        cy - measured.size.height / 2f
                                    )
                                )
                            }
                        }
                    } else {
                        val w = config.widthMm
                        val h = config.heightMm
                        val d = config.depthMm
                        val maxDim = maxOf(w, h, d).coerceAtLeast(1f)
                        val scale = (min(size.width, size.height) * 0.34f / maxDim) * zoom
                        val cx = size.width / 2f + panOffset.x
                        val cy = size.height * 0.54f + panOffset.y

                        fun iso(x: Float, y: Float, z: Float): Offset {
                            val ix = (x - z) * 0.866f * scale
                            val iy = ((x + z) * 0.5f - y) * scale
                            return Offset(cx + ix, cy + iy)
                        }

                        val hw = w / 2f
                        val hh = h / 2f
                        val hd = d / 2f

                        val p000 = iso(-hw, -hh, -hd)
                        val p100 = iso(hw, -hh, -hd)
                        val p101 = iso(hw, -hh, hd)
                        val p001 = iso(-hw, -hh, hd)

                        val p010 = iso(-hw, hh, -hd)
                        val p110 = iso(hw, hh, -hd)
                        val p111 = iso(hw, hh, hd)
                        val p011 = iso(-hw, hh, hd)

                        val dashed = Stroke(
                            width = 1.4.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )
                        drawLine(LaserCyan400.copy(alpha = 0.4f), p000, p100, strokeWidth = 1.4f, pathEffect = dashed.pathEffect)
                        drawLine(LaserCyan400.copy(alpha = 0.4f), p000, p001, strokeWidth = 1.4f, pathEffect = dashed.pathEffect)
                        drawLine(LaserCyan400.copy(alpha = 0.4f), p000, p010, strokeWidth = 1.4f, pathEffect = dashed.pathEffect)

                        val frontFace = Path().apply {
                            moveTo(p001.x, p001.y)
                            lineTo(p101.x, p101.y)
                            lineTo(p111.x, p111.y)
                            lineTo(p011.x, p011.y)
                            close()
                        }
                        val rightFace = Path().apply {
                            moveTo(p101.x, p101.y)
                            lineTo(p100.x, p100.y)
                            lineTo(p110.x, p110.y)
                            lineTo(p111.x, p111.y)
                            close()
                        }
                        val topFace = Path().apply {
                            moveTo(p011.x, p011.y)
                            lineTo(p111.x, p111.y)
                            lineTo(p110.x, p110.y)
                            lineTo(p010.x, p010.y)
                            close()
                        }

                        drawPath(frontFace, LaserCyan400.copy(alpha = 0.14f))
                        drawPath(rightFace, SparkOrange500.copy(alpha = 0.14f))
                        if (config.enclosureStyle == EnclosureStyle.CLOSED_SIX_PANEL) {
                            drawPath(topFace, EngraveBlue.copy(alpha = 0.2f))
                        }

                        val solidStroke = Stroke(width = 2.2.dp.toPx())
                        drawPath(frontFace, CutLineRed, style = solidStroke)
                        drawPath(rightFace, CutLineRed, style = solidStroke)
                        drawPath(topFace, if (config.enclosureStyle == EnclosureStyle.CLOSED_SIX_PANEL) CutLineRed else ScoreAmber, style = solidStroke)
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    color = Color(0xDD0F172A),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, LaserCyan400.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = if (isArabic) {
                            "مساحة اللوح: ${layout.totalSheetWidthMm.toInt()}×${layout.totalSheetHeightMm.toInt()} مم • خلوص الشعاع: ${config.kerfMm} مم"
                        } else {
                            "Sheet: ${layout.totalSheetWidthMm.toInt()}×${layout.totalSheetHeightMm.toInt()}mm • Kerf: ${config.kerfMm}mm"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BoxStatPill(
                    label = if (isArabic) "عدد الأوجه" else "PANELS",
                    value = if (isArabic) "${layout.panels.size} قطع" else "${layout.panels.size} pcs",
                    color = LaserCyan400,
                    modifier = Modifier.weight(1f)
                )
                BoxStatPill(
                    label = if (isArabic) "وصلات التعشيق" else "FINGER TABS",
                    value = "${layout.totalTabsCount}",
                    color = SparkOrange500,
                    modifier = Modifier.weight(1f)
                )
                BoxStatPill(
                    label = if (isArabic) "مسار القص" else "CUT PATH",
                    value = String.format(Locale.US, "%.2fm", layout.totalCutLengthMeters),
                    color = CutLineRed,
                    modifier = Modifier.weight(1f)
                )
                BoxStatPill(
                    label = if (isArabic) "الحجم الداخلي" else "INT. VOLUME",
                    value = "${layout.internalVolumeCm3.roundToInt()}cm³",
                    color = EngraveBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BoxStatPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
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
private fun BoxParametersFormCard(
    isArabic: Boolean,
    config: BoxGeneratorConfig,
    layout: BoxLayoutSummary,
    isExporting: Boolean,
    onUpdateConfig: ((BoxGeneratorConfig) -> BoxGeneratorConfig) -> Unit,
    onExportBoxSvg: () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Straighten,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isArabic) {
                        "أبعاد الصندوق وإعدادات التعشيق"
                    } else {
                        "ENCLOSURE DIMENSIONS & JOINT PARAMETERS"
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }

            DimensionParameterSlider(
                label = if (isArabic) "العرض (Width - مم)" else "Width (X Axis)",
                valueMm = config.widthMm,
                valueFormat = "${config.widthMm.roundToInt()} mm",
                range = 40f..360f,
                stepDelta = 5f,
                onValueChange = { v -> onUpdateConfig { it.copy(widthMm = v.roundToInt().toFloat()) } },
                testTagPrefix = "box_width"
            )

            DimensionParameterSlider(
                label = if (isArabic) "الارتفاع (Height - مم)" else "Height (Y Axis)",
                valueMm = config.heightMm,
                valueFormat = "${config.heightMm.roundToInt()} mm",
                range = 30f..280f,
                stepDelta = 5f,
                onValueChange = { v -> onUpdateConfig { it.copy(heightMm = v.roundToInt().toFloat()) } },
                testTagPrefix = "box_height"
            )

            DimensionParameterSlider(
                label = if (isArabic) "العمق (Depth - مم)" else "Depth (Z Axis)",
                valueMm = config.depthMm,
                valueFormat = "${config.depthMm.roundToInt()} mm",
                range = 40f..360f,
                stepDelta = 5f,
                onValueChange = { v -> onUpdateConfig { it.copy(depthMm = v.roundToInt().toFloat()) } },
                testTagPrefix = "box_depth"
            )

            HorizontalDivider()

            DimensionParameterSlider(
                label = if (isArabic) "سماكة الخامة (Thickness - مم)" else "Material Thickness",
                valueMm = config.thicknessMm,
                valueFormat = String.format(Locale.US, "%.1f mm", config.thicknessMm),
                range = 1.5f..12.0f,
                stepDelta = 0.5f,
                accentColor = SparkOrange500,
                onValueChange = { v ->
                    val rounded = (v * 10f).roundToInt() / 10f
                    onUpdateConfig { it.copy(thicknessMm = rounded) }
                },
                testTagPrefix = "box_thickness"
            )

            DimensionParameterSlider(
                label = if (isArabic) "عرض سن التعشيق (Finger Tab - مم)" else "Finger Joint / Tab Size",
                valueMm = config.tabSizeMm,
                valueFormat = "${config.tabSizeMm.roundToInt()} mm",
                range = 8f..45f,
                stepDelta = 2f,
                accentColor = SparkOrange500,
                onValueChange = { v -> onUpdateConfig { it.copy(tabSizeMm = v.roundToInt().toFloat()) } },
                testTagPrefix = "box_tab_size"
            )

            DimensionParameterSlider(
                label = if (isArabic) "تعويض شعاع الليزر (Kerf - مم)" else "Laser Kerf Compensation",
                valueMm = config.kerfMm,
                valueFormat = String.format(Locale.US, "%.2f mm", config.kerfMm),
                range = 0.00f..0.35f,
                stepDelta = 0.02f,
                accentColor = EngraveBlue,
                onValueChange = { v ->
                    val rounded = (v * 100f).roundToInt() / 100f
                    onUpdateConfig { it.copy(kerfMm = rounded) }
                },
                testTagPrefix = "box_kerf"
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onExportBoxSvg,
                enabled = !isExporting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("export_box_svg_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        if (isArabic) {
                            "جاري تجهيز مخطط الصندوق بصيغة SVG..."
                        } else {
                            "Generating Multi-Part Finger-Joint SVG..."
                        }
                    )
                } else {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) {
                            "تصدير مخطط الصندوق SVG (${layout.panels.size} أوجه • 1 رصيد)"
                        } else {
                            "Export Multi-Panel SVG (${layout.panels.size} Panels • 1 Credit)"
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
                    .testTag("sync_box_to_cost_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isArabic) {
                        "احسب تكلفة تصنيع هذا الصندوق وسعر البيع"
                    } else {
                        "Calculate Box Production Cost & Retail Price"
                    }
                )
            }
        }
    }
}

@Composable
private fun DimensionParameterSlider(
    label: String,
    valueMm: Float,
    valueFormat: String,
    range: ClosedFloatingPointRange<Float>,
    stepDelta: Float,
    accentColor: Color = LaserCyan400,
    onValueChange: (Float) -> Unit,
    testTagPrefix: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        onValueChange((valueMm - stepDelta).coerceIn(range.start, range.endInclusive))
                    },
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("${testTagPrefix}_minus")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease $label", modifier = Modifier.size(16.dp))
                }
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = valueFormat,
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                IconButton(
                    onClick = {
                        onValueChange((valueMm + stepDelta).coerceIn(range.start, range.endInclusive))
                    },
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("${testTagPrefix}_plus")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase $label", modifier = Modifier.size(16.dp))
                }
            }
        }
        Slider(
            value = valueMm.coerceIn(range.start, range.endInclusive),
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor
            ),
            modifier = Modifier.testTag("${testTagPrefix}_slider")
        )
    }
}

package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.IconButton
import com.example.export.FileSharing
import com.example.export.QuoteDocumentRenderer
import com.example.model.AppCurrency
import com.example.model.LaserType
import com.example.model.CostCalculationResult
import com.example.model.CostCalculatorConfig
import com.example.model.DEFAULT_MATERIAL_PRESETS
import com.example.model.MaterialPreset
import com.example.model.SavedWorkshopQuote
import com.example.ui.components.WorkshopBlueprintBanner
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EngraveBlue
import com.example.ui.theme.LaserCyan400
import com.example.ui.theme.ScoreAmber
import com.example.ui.theme.SparkOrange500
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun CostCalculatorScreen(
    isArabic: Boolean,
    config: CostCalculatorConfig,
    result: CostCalculationResult,
    savedQuotes: List<SavedWorkshopQuote>,
    currency: AppCurrency,
    workshopName: String,
    currentQuote: () -> SavedWorkshopQuote,
    onDeleteQuote: (String) -> Unit,
    onUpdateConfig: ((CostCalculatorConfig) -> CostCalculatorConfig) -> Unit,
    onApplyMaterialPreset: (MaterialPreset) -> Unit,
    onSyncFromVector: () -> Unit,
    onSyncFromBox: () -> Unit,
    onSaveQuote: () -> Unit,
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
                activeToolBadge = if (isArabic) "الأداة 03" else "TOOL 03",
                activeToolTitle = if (isArabic) {
                    "حاسبة التكاليف وتسعير منتجات الورشة"
                } else {
                    "Workshop Cost & Pricing Calculator"
                },
                activeToolSubtitle = if (isArabic) {
                    "حساب تكلفة الخامات، استهلاك اللوح، تكلفة تشغيل ماكينة الليزر، وسعر البيع المقترح للعميل بدقة."
                } else {
                    "Calculate exact sheet utilization, machine laser time, COGS, and profitable retail pricing."
                },
                isArabic = isArabic
            )

            // Quick Sync & Sheet Presets Bar
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSyncFromBox,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pull_from_box_button")
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "استيراد أبعاد الصندوق" else "Import Box CAD Specs")
                        }
                        OutlinedButton(
                            onClick = onSyncFromVector,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pull_from_vector_button")
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "استيراد بيانات الفيكتور" else "Import Vector Specs")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isArabic) {
                            "قوالب أسعار الألواح وسرعة القص الافتراضية:"
                        } else {
                            "MATERIAL SHEET COST & LASER SPEED PRESETS:"
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
                                selected = kotlin.math.abs(config.materialCostPerSheet - preset.defaultSheetCost) < 0.1f,
                                onClick = { onApplyMaterialPreset(preset) },
                                label = {
                                    Text("${preset.name(isArabic)} (${currency.format(preset.defaultSheetCost, isArabic)})")
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
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
                    CostInputsCard(
                        isArabic = isArabic,
                        config = config,
                        currency = currency,
                        result = result,
                        onUpdateConfig = onUpdateConfig,
                        modifier = Modifier.weight(1.05f)
                    )
                    CostOutputDashboardCard(
                        isArabic = isArabic,
                        config = config,
                        result = result,
                        savedQuotes = savedQuotes,
                        currency = currency,
                        workshopName = workshopName,
                        currentQuote = currentQuote,
                        onDeleteQuote = onDeleteQuote,
                        onSaveQuote = onSaveQuote,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                CostOutputDashboardCard(
                    isArabic = isArabic,
                    config = config,
                    result = result,
                    savedQuotes = savedQuotes,
                    currency = currency,
                    workshopName = workshopName,
                    currentQuote = currentQuote,
                    onDeleteQuote = onDeleteQuote,
                    onSaveQuote = onSaveQuote,
                    modifier = Modifier.fillMaxWidth()
                )
                CostInputsCard(
                    isArabic = isArabic,
                    config = config,
                    currency = currency,
                    result = result,
                    onUpdateConfig = onUpdateConfig,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun CostInputsCard(
    isArabic: Boolean,
    config: CostCalculatorConfig,
    currency: AppCurrency,
    result: CostCalculationResult,
    onUpdateConfig: ((CostCalculatorConfig) -> CostCalculatorConfig) -> Unit,
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
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isArabic) {
                        "مدخلات الخامات وتشغيل الماكينة"
                    } else {
                        "WORKSHOP MATERIAL & MACHINE INPUTS"
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }

            OutlinedTextField(
                value = config.projectName,
                onValueChange = { name -> onUpdateConfig { it.copy(projectName = name) } },
                label = { Text(if (isArabic) "اسم المشروع / عرض السعر" else "Job / Client Quote Title") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cost_project_name_input")
            )

            // Row 1: Material Cost Per Sheet + Sheet Width + Sheet Length
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NumericField(
                    label = if (isArabic) "سعر اللوح (${currency.symbol(isArabic)})" else "Sheet Cost (${currency.symbol(isArabic)})",
                    value = config.materialCostPerSheet,
                    onValueChange = { v -> onUpdateConfig { it.copy(materialCostPerSheet = v) } },
                    modifier = Modifier.weight(1f),
                    testTag = "input_sheet_cost"
                )
                NumericField(
                    label = if (isArabic) "عرض اللوح (مم)" else "Sheet W (mm)",
                    value = config.sheetWidthMm,
                    onValueChange = { v -> onUpdateConfig { it.copy(sheetWidthMm = v) } },
                    modifier = Modifier.weight(1f),
                    testTag = "input_sheet_width"
                )
                NumericField(
                    label = if (isArabic) "طول اللوح (مم)" else "Sheet L (mm)",
                    value = config.sheetLengthMm,
                    onValueChange = { v -> onUpdateConfig { it.copy(sheetLengthMm = v) } },
                    modifier = Modifier.weight(1f),
                    testTag = "input_sheet_length"
                )
            }

            // Row 2: Part Width + Part Length + Cut Length (Meters)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NumericField(
                    label = if (isArabic) "عرض القطعة (مم)" else "Part W (mm)",
                    value = config.itemWidthMm,
                    onValueChange = { v -> onUpdateConfig { it.copy(itemWidthMm = v) } },
                    modifier = Modifier.weight(1f),
                    testTag = "input_item_width"
                )
                NumericField(
                    label = if (isArabic) "طول القطعة (مم)" else "Part L (mm)",
                    value = config.itemLengthMm,
                    onValueChange = { v -> onUpdateConfig { it.copy(itemLengthMm = v) } },
                    modifier = Modifier.weight(1f),
                    testTag = "input_item_length"
                )
                NumericField(
                    label = if (isArabic) "طول القص (متر)" else "Cut Length (m)",
                    value = config.cutLengthMeters,
                    onValueChange = { v -> onUpdateConfig { it.copy(cutLengthMeters = v) } },
                    modifier = Modifier.weight(1f),
                    testTag = "input_cut_length_meters"
                )
            }

            // Row 3: Laser Speed (mm/s) + Hourly Machine Rate ($/hr) + Setup Fee ($)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NumericField(
                    label = if (isArabic) "السرعة (مم/ث)" else "Speed (mm/s)",
                    value = config.cuttingSpeedMmPerSec,
                    onValueChange = { v -> onUpdateConfig { it.copy(cuttingSpeedMmPerSec = v) } },
                    modifier = Modifier.weight(1f),
                    testTag = "input_cutting_speed"
                )
                NumericField(
                    label = if (isArabic) "ساعة الماكينة (${currency.symbol(isArabic)})" else "Shop Rate (${currency.symbol(isArabic)}/hr)",
                    value = config.hourlyMachineRate,
                    onValueChange = { v -> onUpdateConfig { it.copy(hourlyMachineRate = v) } },
                    modifier = Modifier.weight(1f),
                    testTag = "input_hourly_rate"
                )
                NumericField(
                    label = if (isArabic) "رسوم التجهيز (${currency.symbol(isArabic)})" else "Setup Fee (${currency.symbol(isArabic)})",
                    value = config.setupAndHandlingFee,
                    onValueChange = { v -> onUpdateConfig { it.copy(setupAndHandlingFee = v) } },
                    modifier = Modifier.weight(1f),
                    testTag = "input_setup_fee"
                )
            }

            HorizontalDivider()

            // Machine profile: laser type + power (scales the material's reference speed)
            Text(
                text = if (isArabic) "ملف الماكينة (نوع الليزر والقدرة)" else "MACHINE PROFILE (LASER TYPE & POWER)",
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LaserType.entries.forEach { type ->
                    FilterChip(
                        selected = config.laserType == type,
                        onClick = {
                            onUpdateConfig {
                                it.copy(
                                    laserType = type,
                                    laserPowerW = type.defaultPowerW,
                                    machineWatts = type.defaultMachineWatts
                                )
                            }
                        },
                        label = { Text(type.label(isArabic)) },
                        modifier = Modifier.testTag("laser_type_${type.name.lowercase(Locale.US)}")
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NumericField(
                    label = if (isArabic) "قدرة الليزر (واط)" else "Laser Power (W)",
                    value = config.laserPowerW,
                    onValueChange = { v -> onUpdateConfig { it.copy(laserPowerW = v) } },
                    modifier = Modifier.weight(1f),
                    testTag = "input_laser_power"
                )
                NumericField(
                    label = if (isArabic) "استهلاك الماكينة (واط)" else "Machine Draw (W)",
                    value = config.machineWatts,
                    onValueChange = { v -> onUpdateConfig { it.copy(machineWatts = v) } },
                    modifier = Modifier.weight(1f),
                    testTag = "input_machine_watts"
                )
                NumericField(
                    label = if (isArabic) "سعر الكيلوواط (${currency.symbol(isArabic)})" else "kWh Price (${currency.symbol(isArabic)})",
                    value = config.kwhPrice,
                    onValueChange = { v -> onUpdateConfig { it.copy(kwhPrice = v) } },
                    modifier = Modifier.weight(1f),
                    testTag = "input_kwh_price"
                )
            }
            Text(
                text = if (isArabic) {
                    "السرعة الفعلية المقدّرة: ${result.effectiveSpeedMmPerSec.roundToInt()} مم/ث (السرعة المرجعية للخامة على CO2 بقدرة 60 واط) • الكهرباء: ${currency.format(result.electricityCostPerItem, true)} للقطعة"
                } else {
                    "Estimated effective speed: ${result.effectiveSpeedMmPerSec.roundToInt()} mm/s (material reference = 60W CO2) • Electricity: ${currency.format(result.electricityCostPerItem, false)}/unit"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            // Profit Margin Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isArabic) "هامش الربح المطلوب" else "Desired Profit Margin",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = if (isArabic) {
                            "ربح ${config.profitMarginPercent.roundToInt()}%"
                        } else {
                            "${config.profitMarginPercent.roundToInt()}% Margin"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldSuccess
                    )
                }
                Slider(
                    value = config.profitMarginPercent,
                    onValueChange = { v -> onUpdateConfig { it.copy(profitMarginPercent = v) } },
                    valueRange = 15f..80f,
                    colors = SliderDefaults.colors(
                        thumbColor = EmeraldSuccess,
                        activeTrackColor = EmeraldSuccess
                    ),
                    modifier = Modifier.testTag("profit_margin_slider")
                )
            }

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
                            if (isArabic) "نسبة الهدر" else "Kerf/Scrap Waste",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "${config.wasteFactorPercent.roundToInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = SparkOrange500
                        )
                    }
                    Slider(
                        value = config.wasteFactorPercent,
                        onValueChange = { v -> onUpdateConfig { it.copy(wasteFactorPercent = v) } },
                        valueRange = 0f..50f
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            if (isArabic) "كمية الدفعة" else "Batch Run Size",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            if (isArabic) "${config.batchQuantity} قطعة" else "${config.batchQuantity} units",
                            style = MaterialTheme.typography.labelSmall,
                            color = LaserCyan400
                        )
                    }
                    Slider(
                        value = config.batchQuantity.toFloat(),
                        onValueChange = { v -> onUpdateConfig { it.copy(batchQuantity = v.roundToInt().coerceAtLeast(1)) } },
                        valueRange = 1f..100f
                    )
                }
            }
        }
    }
}

@Composable
private fun NumericField(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    val displayStr = if (value == value.toLong().toFloat()) {
        value.toLong().toString()
    } else {
        String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
    }

    OutlinedTextField(
        value = displayStr,
        onValueChange = { text ->
            val parsed = text.toFloatOrNull()
            if (parsed != null && parsed >= 0f) {
                onValueChange(parsed)
            }
        },
        label = { Text(label, maxLines = 1) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.testTag(testTag)
    )
}

@Composable
private fun CostOutputDashboardCard(
    isArabic: Boolean,
    config: CostCalculatorConfig,
    result: CostCalculationResult,
    savedQuotes: List<SavedWorkshopQuote>,
    currency: AppCurrency,
    workshopName: String,
    currentQuote: () -> SavedWorkshopQuote,
    onDeleteQuote: (String) -> Unit,
    onSaveQuote: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    fun m(v: Float, signed: Boolean = false) = currency.format(v, isArabic, signed)
    fun exportQuote(q: SavedWorkshopQuote, pdf: Boolean) {
        runCatching {
            val base = QuoteDocumentRenderer.safeFileBase(q)
            if (pdf) {
                FileSharing.shareBytes(
                    context, "$base.pdf", QuoteDocumentRenderer.renderPdf(q, workshopName, isArabic),
                    "application/pdf", if (isArabic) "مشاركة عرض السعر PDF" else "Share quote PDF"
                )
            } else {
                FileSharing.shareBytes(
                    context, "$base.png", QuoteDocumentRenderer.renderPng(q, workshopName, isArabic),
                    "image/png", if (isArabic) "مشاركة عرض السعر كصورة" else "Share quote image"
                )
            }
        }.onFailure {
            Toast.makeText(context, (if (isArabic) "تعذر التصدير: " else "Export failed: ") + it.message, Toast.LENGTH_LONG).show()
        }
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("retail_price_card"),
                color = Color(0xFF090D16),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.55f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isArabic) {
                                    "سعر البيع المقترح للقطعة الواحدة"
                                } else {
                                    "RECOMMENDED RETAIL PRICE / ITEM"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldSuccess
                            )
                            Text(
                                text = m(result.recommendedRetailPrice),
                                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        Surface(
                            color = EmeraldSuccess.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = m(result.profitPerUnit, signed = true),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldSuccess
                                )
                                Text(
                                    text = if (isArabic) "صافي الربح للقطعة" else "Net Profit / Unit",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutputMetricBox(
                            title = if (isArabic) "تكلفة الخامة" else "MATERIAL COST",
                            amount = m(result.materialCostPerItem),
                            subtitle = if (isArabic) "${result.itemsPerSheet} قطع / لوح" else "${result.itemsPerSheet} pcs / sheet",
                            accent = LaserCyan400,
                            modifier = Modifier.weight(1f)
                        )
                        OutputMetricBox(
                            title = if (isArabic) "تكلفة التشغيل" else "MACHINE COST",
                            amount = m(result.machineCostPerItem + result.electricityCostPerItem),
                            subtitle = formatSeconds(result.cutTimeSecondsPerItem.roundToInt(), isArabic),
                            accent = SparkOrange500,
                            modifier = Modifier.weight(1f)
                        )
                        OutputMetricBox(
                            title = if (isArabic) "إجمالي التكلفة" else "UNIT COGS",
                            amount = m(result.totalUnitProductionCost),
                            subtitle = if (isArabic) {
                                "جملة ${m(result.wholesaleUnitPrice)}"
                            } else {
                                "Wholesale ${m(result.wholesaleUnitPrice)}"
                            },
                            accent = EngraveBlue,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Visual Cost Structure Proportion Bar
            Column {
                val totalRetail = max(0.01f, result.recommendedRetailPrice)
                val matFrac = (result.materialCostPerItem / totalRetail).coerceIn(0.04f, 1f)
                val machFrac = ((result.machineCostPerItem + result.electricityCostPerItem) / totalRetail).coerceIn(0.04f, 1f)
                val setupFrac = (result.setupCostPerItem / totalRetail).coerceIn(0.03f, 1f)
                val profFrac = (result.profitPerUnit / totalRetail).coerceIn(0.05f, 1f)

                Text(
                    text = if (isArabic) "تحليل هيكل السعر والتكلفة" else "UNIT ECONOMICS BREAKDOWN",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(50))
                ) {
                    Box(
                        modifier = Modifier
                            .weight(matFrac)
                            .fillMaxHeight()
                            .background(LaserCyan400)
                    )
                    Box(
                        modifier = Modifier
                            .weight(machFrac)
                            .fillMaxHeight()
                            .background(SparkOrange500)
                    )
                    Box(
                        modifier = Modifier
                            .weight(setupFrac)
                            .fillMaxHeight()
                            .background(ScoreAmber)
                    )
                    Box(
                        modifier = Modifier
                            .weight(profFrac)
                            .fillMaxHeight()
                            .background(EmeraldSuccess)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LegendDot(if (isArabic) "الخامة" else "Material", LaserCyan400)
                    LegendDot(if (isArabic) "تشغيل الليزر" else "Laser Time", SparkOrange500)
                    LegendDot(if (isArabic) "التجهيز" else "Setup", ScoreAmber)
                    LegendDot(
                        if (isArabic) "الربح (${config.profitMarginPercent.roundToInt()}%)" else "Profit (${config.profitMarginPercent.roundToInt()}%)",
                        EmeraldSuccess
                    )
                }
            }

            // Batch Production Summary Banner
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isArabic) {
                                "إجمالي دفعة الإنتاج (${config.batchQuantity} قطعة)"
                            } else {
                                "BATCH RUN (${config.batchQuantity} UNITS)"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isArabic) {
                                "إجمالي المبيعات: ${m(result.totalBatchRevenue)}"
                            } else {
                                "Total Revenue: ${m(result.totalBatchRevenue)}"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = if (isArabic) {
                            "صافي الربح: ${m(result.totalBatchProfit, signed = true)}"
                        } else {
                            "Net Profit: ${m(result.totalBatchProfit, signed = true)}"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = EmeraldSuccess
                    )
                }
            }

            // Action Buttons: Save Quote & Share Client Estimate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onSaveQuote,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("save_quote_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isArabic) "حفظ عرض السعر" else "Save Quote")
                }

                OutlinedButton(
                    onClick = {
                        val shopName = workshopName.ifBlank { if (isArabic) "ورشة الليزر" else "Laser Workshop" }
                        val quoteSummary = if (isArabic) {
                            buildString {
                                appendLine("عرض سعر — $shopName")
                                appendLine("المشروع: ${config.projectName}")
                                appendLine("مقاس القطعة: ${config.itemWidthMm.toInt()}×${config.itemLengthMm.toInt()} مم")
                                appendLine("الكمية: ${config.batchQuantity} قطعة")
                                appendLine("سعر القطعة: ${m(result.recommendedRetailPrice)}")
                                appendLine("الإجمالي: ${m(result.totalBatchRevenue)}")
                            }
                        } else {
                            buildString {
                                appendLine("QUOTE — $shopName")
                                appendLine("Project: ${config.projectName}")
                                appendLine("Part size: ${config.itemWidthMm.toInt()}×${config.itemLengthMm.toInt()}mm")
                                appendLine("Quantity: ${config.batchQuantity}")
                                appendLine("Unit price: ${m(result.recommendedRetailPrice)}")
                                appendLine("Total: ${m(result.totalBatchRevenue)}")
                            }
                        }
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, config.projectName)
                            putExtra(Intent.EXTRA_TEXT, quoteSummary)
                        }
                        context.startActivity(
                            Intent.createChooser(
                                sendIntent,
                                if (isArabic) "مشاركة عرض السعر" else "Share Workshop Quote"
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("share_quote_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isArabic) "مشاركة نصية" else "Share Text")
                }
            }

            // PDF / PNG export of the current quote (share sheet → WhatsApp, email, ...)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { exportQuote(currentQuote(), pdf = true) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("export_quote_pdf_button")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isArabic) "عرض سعر PDF" else "Quote PDF")
                }
                OutlinedButton(
                    onClick = { exportQuote(currentQuote(), pdf = false) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("export_quote_png_button")
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isArabic) "عرض سعر صورة" else "Quote Image")
                }
            }

            if (savedQuotes.isNotEmpty()) {
                HorizontalDivider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isArabic) {
                            "عروض الأسعار المحفوظة (${savedQuotes.size})"
                        } else {
                            "SAVED WORKSHOP QUOTES (${savedQuotes.size})"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    savedQuotes.forEach { quote ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = quote.projectName,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = quote.summaryText(isArabic),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = quote.currency.format(quote.retailPrice, isArabic),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = EmeraldSuccess
                                    )
                                    Row {
                                        IconButton(onClick = { exportQuote(quote, pdf = true) }, modifier = Modifier.size(34.dp)) {
                                            Icon(Icons.Default.PictureAsPdf, contentDescription = if (isArabic) "تصدير PDF" else "Export PDF", modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = { exportQuote(quote, pdf = false) }, modifier = Modifier.size(34.dp)) {
                                            Icon(Icons.Default.Image, contentDescription = if (isArabic) "تصدير صورة" else "Export image", modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(onClick = { onDeleteQuote(quote.id) }, modifier = Modifier.size(34.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = if (isArabic) "حذف" else "Delete", modifier = Modifier.size(18.dp))
                                        }
                                    }
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
private fun OutputMetricBox(
    title: String,
    amount: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = accent
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFCBD5E1),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatSeconds(totalSec: Int, isArabic: Boolean): String {
    val mins = totalSec / 60
    val secs = totalSec % 60
    return if (isArabic) {
        if (mins > 0) "${mins}د ${secs}ث قص" else "${secs}ث قص"
    } else {
        if (mins > 0) "${mins}m ${secs}s cut" else "${secs}s cut"
    }
}

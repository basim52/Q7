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
                                    Text("${preset.name(isArabic)} ($${String.format(Locale.US, "%.2f", preset.defaultSheetCost)})")
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
                        onUpdateConfig = onUpdateConfig,
                        modifier = Modifier.weight(1.05f)
                    )
                    CostOutputDashboardCard(
                        isArabic = isArabic,
                        config = config,
                        result = result,
                        savedQuotes = savedQuotes,
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
                    onSaveQuote = onSaveQuote,
                    modifier = Modifier.fillMaxWidth()
                )
                CostInputsCard(
                    isArabic = isArabic,
                    config = config,
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
                    label = if (isArabic) "سعر اللوح ($)" else "Sheet Cost ($)",
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
                    label = if (isArabic) "ساعة الماكينة ($)" else "Shop Rate ($/hr)",
                    value = config.hourlyMachineRate,
                    onValueChange = { v -> onUpdateConfig { it.copy(hourlyMachineRate = v) } },
                    modifier = Modifier.weight(1f),
                    testTag = "input_hourly_rate"
                )
                NumericField(
                    label = if (isArabic) "رسوم التجهيز ($)" else "Setup Fee ($)",
                    value = config.setupAndHandlingFee,
                    onValueChange = { v -> onUpdateConfig { it.copy(setupAndHandlingFee = v) } },
                    modifier = Modifier.weight(1f),
                    testTag = "input_setup_fee"
                )
            }

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
    onSaveQuote: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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
                                text = String.format(Locale.US, "$%.2f", result.recommendedRetailPrice),
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
                                    text = String.format(Locale.US, "+$%.2f", result.profitPerUnit),
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
                            amount = String.format(Locale.US, "$%.2f", result.materialCostPerItem),
                            subtitle = if (isArabic) "${result.itemsPerSheet} قطع / لوح" else "${result.itemsPerSheet} pcs / sheet",
                            accent = LaserCyan400,
                            modifier = Modifier.weight(1f)
                        )
                        OutputMetricBox(
                            title = if (isArabic) "تكلفة التشغيل" else "MACHINE COST",
                            amount = String.format(Locale.US, "$%.2f", result.machineCostPerItem),
                            subtitle = formatSeconds(result.cutTimeSecondsPerItem.roundToInt(), isArabic),
                            accent = SparkOrange500,
                            modifier = Modifier.weight(1f)
                        )
                        OutputMetricBox(
                            title = if (isArabic) "إجمالي التكلفة" else "UNIT COGS",
                            amount = String.format(Locale.US, "$%.2f", result.totalUnitProductionCost),
                            subtitle = if (isArabic) {
                                "جملة $${String.format(Locale.US, "%.2f", result.wholesaleUnitPrice)}"
                            } else {
                                "Wholesale $${String.format(Locale.US, "%.2f", result.wholesaleUnitPrice)}"
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
                val machFrac = (result.machineCostPerItem / totalRetail).coerceIn(0.04f, 1f)
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
                                "إجمالي المبيعات: $${String.format(Locale.US, "%.2f", result.totalBatchRevenue)}"
                            } else {
                                "Total Revenue: $${String.format(Locale.US, "%.2f", result.totalBatchRevenue)}"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = if (isArabic) {
                            "صافي الربح: +$${String.format(Locale.US, "%.2f", result.totalBatchProfit)}"
                        } else {
                            "Net Profit: +$${String.format(Locale.US, "%.2f", result.totalBatchProfit)}"
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
                        val quoteSummary = if (isArabic) {
                            buildString {
                                appendLine("عرض سعر رسمي — استوديو ليزر فورج")
                                appendLine("المشروع: ${config.projectName}")
                                appendLine("مقاس القطعة: ${config.itemWidthMm.toInt()}×${config.itemLengthMm.toInt()} مم")
                                appendLine("طول القص: ${config.cutLengthMeters} م بسرعة ${config.cuttingSpeedMmPerSec.toInt()} مم/ث")
                                appendLine("تكلفة الخامة: $${String.format(Locale.US, "%.2f", result.materialCostPerItem)} للقطعة")
                                appendLine("تكلفة تشغيل الليزر: $${String.format(Locale.US, "%.2f", result.machineCostPerItem)} للقطعة")
                                appendLine("سعر البيع المقترح: $${String.format(Locale.US, "%.2f", result.recommendedRetailPrice)} للقطعة")
                            }
                        } else {
                            buildString {
                                appendLine("LASERFORGE WORKSHOP ESTIMATE")
                                appendLine("Project: ${config.projectName}")
                                appendLine("Part Footprint: ${config.itemWidthMm.toInt()}×${config.itemLengthMm.toInt()}mm")
                                appendLine("Cut Length: ${config.cutLengthMeters}m @ ${config.cuttingSpeedMmPerSec.toInt()}mm/s")
                                appendLine("Material Cost: $${String.format(Locale.US, "%.2f", result.materialCostPerItem)}/unit")
                                appendLine("Machine Cost: $${String.format(Locale.US, "%.2f", result.machineCostPerItem)}/unit")
                                appendLine("Recommended Retail Price: $${String.format(Locale.US, "%.2f", result.recommendedRetailPrice)}/unit")
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
                    Text(if (isArabic) "مشاركة التسعيرة" else "Share Quote")
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
                    savedQuotes.take(4).forEach { quote ->
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
                                Text(
                                    text = String.format(Locale.US, "$%.2f", quote.retailPrice),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldSuccess
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

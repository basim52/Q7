package com.example.viewmodel

import android.graphics.Bitmap
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.LocalStore
import com.example.model.AppCurrency
import com.example.model.EnclosureStyle
import androidx.lifecycle.viewModelScope
import com.example.engine.BoxSvgGenerator
import com.example.engine.SvgVectorEngine
import com.example.model.BoxGeneratorConfig
import com.example.model.BoxLayoutSummary
import com.example.model.CostCalculationResult
import com.example.model.CostCalculatorConfig
import com.example.model.ExportHistoryItem
import com.example.model.MaterialPreset
import com.example.model.SampleArtworkPreset
import com.example.model.SavedWorkshopQuote
import com.example.model.StudioTool
import com.example.model.VectorOptimizerConfig
import com.example.model.VectorPreviewResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LaserForgeUiState(
    val activeTool: StudioTool = StudioTool.VECTOR_OPTIMIZER,
    val isDarkTheme: Boolean = true,
    val isArabic: Boolean = true,
    val workshopName: String = "",
    val currency: AppCurrency = AppCurrency.SAR,
    val statusBannerMessage: String? = null,

    // Tool 1: Vector & Image Optimizer
    val selectedPreset: SampleArtworkPreset = SampleArtworkPreset.GEAR_MANDALA,
    val customBitmap: Bitmap? = null,
    val artworkTitle: String = SampleArtworkPreset.GEAR_MANDALA.titleAr,
    val vectorConfig: VectorOptimizerConfig = VectorOptimizerConfig(),
    val vectorPreview: VectorPreviewResult? = null,
    val isGeneratingVector: Boolean = false,

    // Tool 2: Custom Box & Enclosure Generator
    val boxConfig: BoxGeneratorConfig = BoxGeneratorConfig(),
    val boxLayout: BoxLayoutSummary = BoxSvgGenerator.generateBoxLayout(BoxGeneratorConfig()),
    val isExportingBox: Boolean = false,

    // Tool 3: Workshop Cost & Pricing Calculator
    val costConfig: CostCalculatorConfig = CostCalculatorConfig(),
    val costResult: CostCalculationResult = calculateWorkshopPricing(CostCalculatorConfig()),
    val savedQuotes: List<SavedWorkshopQuote> = emptyList(),

    // Export Modal & History
    val exportHistory: List<ExportHistoryItem> = emptyList(),
    val activeExportModalItem: ExportHistoryItem? = null
)

fun calculateWorkshopPricing(config: CostCalculatorConfig): CostCalculationResult {
    val sheetAreaMm2 = max(1f, config.sheetWidthMm * config.sheetLengthMm)
    val itemAreaMm2 = max(1f, config.itemWidthMm * config.itemLengthMm)

    val colsA = floor(config.sheetWidthMm / max(1f, config.itemWidthMm)).toInt()
    val rowsA = floor(config.sheetLengthMm / max(1f, config.itemLengthMm)).toInt()
    val colsB = floor(config.sheetWidthMm / max(1f, config.itemLengthMm)).toInt()
    val rowsB = floor(config.sheetLengthMm / max(1f, config.itemWidthMm)).toInt()
    val nestedFit = max(1, max(colsA * rowsA, colsB * rowsB))

    val rawAreaCost = config.materialCostPerSheet * (itemAreaMm2 / sheetAreaMm2)
    val wasteMultiplier = 1f + (config.wasteFactorPercent.coerceIn(0f, 100f) / 100f)
    val materialCostPerItem = rawAreaCost * wasteMultiplier

    val cutLengthMm = max(10f, config.cutLengthMeters * 1000f)
    // Material reference speed is tuned for a 60W CO2 laser; scale it by the machine profile.
    val speed = max(0.5f, config.cuttingSpeedMmPerSec * config.laserType.speedMultiplier(config.laserPowerW))
    val cutTimeSeconds = (cutLengthMm / speed) * 1.15f
    val machineCostPerSecond = config.hourlyMachineRate / 3600f
    val machineCostPerItem = cutTimeSeconds * machineCostPerSecond
    val electricityCostPerItem = (cutTimeSeconds / 3600f) * (max(0f, config.machineWatts) / 1000f) * max(0f, config.kwhPrice)

    val setupPerUnit = config.setupAndHandlingFee / max(1, config.batchQuantity).toFloat()
    val totalUnitProductionCost = materialCostPerItem + machineCostPerItem + electricityCostPerItem + setupPerUnit

    val marginFraction = (config.profitMarginPercent.coerceIn(5f, 90f)) / 100f
    val recommendedRetailPrice = totalUnitProductionCost / (1f - marginFraction)
    val wholesaleUnitPrice = totalUnitProductionCost * 1.35f
    val profitPerUnit = recommendedRetailPrice - totalUnitProductionCost

    val qty = max(1, config.batchQuantity)
    return CostCalculationResult(
        sheetAreaCm2 = sheetAreaMm2 / 100f,
        itemAreaCm2 = itemAreaMm2 / 100f,
        itemsPerSheet = nestedFit,
        materialCostPerItem = materialCostPerItem,
        cutTimeSecondsPerItem = cutTimeSeconds,
        machineCostPerItem = machineCostPerItem,
        electricityCostPerItem = electricityCostPerItem,
        effectiveSpeedMmPerSec = speed,
        setupCostPerItem = setupPerUnit,
        totalUnitProductionCost = totalUnitProductionCost,
        wholesaleUnitPrice = wholesaleUnitPrice,
        recommendedRetailPrice = recommendedRetailPrice,
        profitPerUnit = profitPerUnit,
        totalBatchRevenue = recommendedRetailPrice * qty,
        totalBatchProfit = profitPerUnit * qty
    )
}

class LaserForgeViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LaserForgeUiState())
    val uiState: StateFlow<LaserForgeUiState> = _uiState.asStateFlow()
    private val store = LocalStore(application)

    init {
        refreshVectorPreview()
        viewModelScope.launch {
            val saved = runCatching { store.load() }.getOrNull() ?: return@launch
            _uiState.update {
                it.copy(
                    workshopName = saved.workshopName,
                    currency = saved.currency,
                    savedQuotes = saved.savedQuotes,
                    exportHistory = saved.exportHistory
                )
            }
        }
    }

    private fun money(amount: Float, signed: Boolean = false): String {
        val st = _uiState.value
        return st.currency.format(amount, st.isArabic, signed)
    }

    // --- WORKSHOP SETTINGS (stored on device) ---

    fun setWorkshopName(name: String) {
        val trimmed = name.take(60)
        _uiState.update { it.copy(workshopName = trimmed) }
        viewModelScope.launch { store.saveWorkshopName(trimmed) }
    }

    fun setCurrency(currency: AppCurrency) {
        _uiState.update {
            it.copy(
                currency = currency,
                statusBannerMessage = if (it.isArabic) {
                    "تم تغيير العملة إلى ${currency.nameAr} (${currency.symbolAr})"
                } else {
                    "Currency changed to ${currency.nameEn} (${currency.code})"
                }
            )
        }
        viewModelScope.launch { store.saveCurrency(currency) }
    }

    fun deleteQuote(id: String) {
        _uiState.update { it.copy(savedQuotes = it.savedQuotes.filterNot { q -> q.id == id }) }
        persistQuotes()
    }

    fun clearExportHistory() {
        _uiState.update { it.copy(exportHistory = emptyList()) }
        persistHistory()
    }

    private fun persistQuotes() {
        val quotes = _uiState.value.savedQuotes
        viewModelScope.launch { store.saveQuotes(quotes) }
    }

    private fun persistHistory() {
        val items = _uiState.value.exportHistory
        viewModelScope.launch { store.saveHistory(items) }
    }

    private fun addExport(item: ExportHistoryItem) {
        _uiState.update {
            it.copy(exportHistory = (listOf(item) + it.exportHistory).take(LocalStore.MAX_HISTORY))
        }
        persistHistory()
    }

    fun selectTool(tool: StudioTool) {
        _uiState.update { it.copy(activeTool = tool, statusBannerMessage = null) }
    }

    fun toggleTheme() {
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    fun toggleLanguage() {
        _uiState.update { state ->
            val nextAr = !state.isArabic
            val updatedArtworkTitle = if (state.customBitmap == null) {
                state.selectedPreset.title(nextAr)
            } else {
                state.artworkTitle
            }
            state.copy(
                isArabic = nextAr,
                artworkTitle = updatedArtworkTitle,
                statusBannerMessage = if (nextAr) {
                    "تم التبديل إلى اللغة العربية (واجهة من اليمين إلى اليسار)"
                } else {
                    "Switched language to English (LTR interface)"
                }
            )
        }
    }

    fun dismissBanner() {
        _uiState.update { it.copy(statusBannerMessage = null) }
    }

    fun dismissExportModal() {
        _uiState.update { it.copy(activeExportModalItem = null) }
    }

    fun openExportModal(item: ExportHistoryItem) {
        _uiState.update { it.copy(activeExportModalItem = item) }
    }

    // --- TOOL 1: VECTOR & IMAGE OPTIMIZER ---

    fun selectSamplePreset(preset: SampleArtworkPreset) {
        _uiState.update { state ->
            state.copy(
                selectedPreset = preset,
                customBitmap = null,
                artworkTitle = preset.title(state.isArabic),
                vectorConfig = state.vectorConfig.copy(threshold = preset.recommendedThreshold)
            )
        }
        refreshVectorPreview()
    }

    fun onCustomBitmapLoaded(bitmap: Bitmap, fileName: String) {
        _uiState.update { state ->
            state.copy(
                customBitmap = bitmap,
                artworkTitle = fileName,
                statusBannerMessage = if (state.isArabic) {
                    "تم رفع الصورة \"$fileName\" إلى محول الفيكتور"
                } else {
                    "Loaded \"$fileName\" into Vector Optimizer"
                }
            )
        }
        refreshVectorPreview()
    }

    fun updateVectorConfig(transform: (VectorOptimizerConfig) -> VectorOptimizerConfig) {
        _uiState.update { state ->
            state.copy(vectorConfig = transform(state.vectorConfig))
        }
        refreshVectorPreview()
    }

    private fun refreshVectorPreview() {
        val state = _uiState.value
        val config = state.vectorConfig
        val preset = state.selectedPreset
        val customBmp = state.customBitmap

        viewModelScope.launch {
            val preview = withContext(Dispatchers.Default) {
                val sourceBmp = customBmp ?: SvgVectorEngine.createSampleArtworkBitmap(preset, 320)
                SvgVectorEngine.processArtwork(sourceBmp, config)
            }
            _uiState.update { it.copy(vectorPreview = preview) }
        }
    }

    fun generateAndExportVectorSvg() {
        val state = _uiState.value
        val ar = state.isArabic
        val preview = state.vectorPreview ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingVector = true, statusBannerMessage = null) }
            delay(550)
            val svgContent = withContext(Dispatchers.Default) {
                SvgVectorEngine.generateCleanSvg(
                    preview = preview,
                    config = state.vectorConfig,
                    artworkTitle = state.selectedPreset.titleEn
                )
            }
            val safeName = state.selectedPreset.titleEn
                .lowercase(Locale.US)
                .replace(Regex("[^a-z0-9]+"), "_")
                .trim('_')
                .ifEmpty { "laser_vector" }
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
            val exportItem = ExportHistoryItem(
                id = UUID.randomUUID().toString(),
                title = "${state.artworkTitle} (${state.vectorConfig.softwarePreset.label(ar)})",
                toolNameEn = "Vector Optimizer",
                toolNameAr = "محول الفيكتور",
                fileName = "${safeName}_cut.svg",
                timestampLabel = timestamp,
                svgContent = svgContent
            )
            addExport(exportItem)
            _uiState.update { curr ->
                curr.copy(
                    isGeneratingVector = false,
                    activeExportModalItem = exportItem,
                    statusBannerMessage = if (curr.isArabic) {
                        "تم توليد ملف SVG بنجاح (${preview.closedLoopCount} مسار مغلق)"
                    } else {
                        "Generated clean SVG (${preview.closedLoopCount} closed paths)"
                    }
                )
            }
        }
    }

    // --- TOOL 2: CUSTOM BOX & ENCLOSURE GENERATOR ---

    fun updateBoxConfig(transform: (BoxGeneratorConfig) -> BoxGeneratorConfig) {
        _uiState.update { state ->
            val updated = transform(state.boxConfig)
            val layout = BoxSvgGenerator.generateBoxLayout(updated)
            state.copy(boxConfig = updated, boxLayout = layout)
        }
    }

    fun applyBoxMaterialPreset(preset: MaterialPreset) {
        updateBoxConfig { it.copy(thicknessMm = preset.thicknessMm) }
        _uiState.update { state ->
            val ar = state.isArabic
            state.copy(
                statusBannerMessage = if (ar) {
                    "تم تطبيق خامة ${preset.nameAr} (سماكة ${preset.thicknessMm} مم)"
                } else {
                    "Applied ${preset.nameEn} (${preset.thicknessMm}mm thickness) to Box CAD"
                }
            )
        }
    }

    fun exportBoxLayoutSvg() {
        val state = _uiState.value
        val ar = state.isArabic

        viewModelScope.launch {
            _uiState.update { it.copy(isExportingBox = true, statusBannerMessage = null) }
            delay(450)
            val cfg = state.boxConfig
            val layout = state.boxLayout
            val (svgContent, dxfContent) = withContext(Dispatchers.Default) {
                BoxSvgGenerator.exportBoxSvg(cfg, layout) to BoxSvgGenerator.exportBoxDxf(cfg, layout)
            }
            val prefix = when (cfg.enclosureStyle) {
                EnclosureStyle.DRAWER -> "drawer"
                EnclosureStyle.PHONE_STAND -> "phone_stand"
                EnclosureStyle.PICTURE_FRAME -> "frame"
                else -> "box"
            }
            val dims = "${cfg.widthMm.toInt()}x${cfg.heightMm.toInt()}x${cfg.depthMm.toInt()}mm"
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
            val exportItem = ExportHistoryItem(
                id = UUID.randomUUID().toString(),
                title = "${cfg.enclosureStyle.label(ar)} $dims (${cfg.thicknessMm}mm)",
                toolNameEn = "Box Generator",
                toolNameAr = "مولد الصناديق",
                fileName = "${prefix}_${dims}_${cfg.thicknessMm}mm.svg",
                timestampLabel = timestamp,
                svgContent = svgContent,
                dxfContent = dxfContent
            )
            addExport(exportItem)
            _uiState.update { curr ->
                curr.copy(
                    isExportingBox = false,
                    activeExportModalItem = exportItem,
                    statusBannerMessage = if (curr.isArabic) {
                        "تم تجهيز المخطط (${layout.panels.size} قطع - $dims) بصيغتي SVG و DXF"
                    } else {
                        "Prepared ${layout.panels.size}-panel layout ($dims) as SVG + DXF"
                    }
                )
            }
        }
    }

    // --- TOOL 3: WORKSHOP COST & PRICING CALCULATOR ---

    fun updateCostConfig(transform: (CostCalculatorConfig) -> CostCalculatorConfig) {
        _uiState.update { state ->
            val next = transform(state.costConfig)
            state.copy(
                costConfig = next,
                costResult = calculateWorkshopPricing(next)
            )
        }
    }

    fun applyPricingMaterialPreset(preset: MaterialPreset) {
        updateCostConfig {
            it.copy(
                materialCostPerSheet = preset.defaultSheetCost,
                sheetWidthMm = preset.sheetWidthMm,
                sheetLengthMm = preset.sheetHeightMm,
                cuttingSpeedMmPerSec = preset.recommendedSpeedMmPerSec
            )
        }
        _uiState.update { state ->
            val ar = state.isArabic
            state.copy(
                statusBannerMessage = if (ar) {
                    "تم اختيار ${preset.nameAr} (${money(preset.defaultSheetCost)} للوح بسرعة ${preset.recommendedSpeedMmPerSec.toInt()} مم/ث)"
                } else {
                    "Loaded ${preset.nameEn} sheet rates (${money(preset.defaultSheetCost)}/sheet @ ${preset.recommendedSpeedMmPerSec.toInt()}mm/s)"
                }
            )
        }
    }

    fun syncCostFromBoxGenerator() {
        val state = _uiState.value
        val box = state.boxConfig
        val layout = state.boxLayout
        val ar = state.isArabic
        updateCostConfig {
            it.copy(
                projectName = if (ar) {
                    "صندوق تعشيق (${box.widthMm.toInt()}×${box.heightMm.toInt()}×${box.depthMm.toInt()} مم)"
                } else {
                    "Finger-Joint Box (${box.widthMm.toInt()}×${box.heightMm.toInt()}×${box.depthMm.toInt()}mm)"
                },
                itemWidthMm = ceil(layout.totalSheetWidthMm),
                itemLengthMm = ceil(layout.totalSheetHeightMm),
                cutLengthMeters = ((layout.totalCutLengthMeters * 100f).toInt() / 100f).coerceAtLeast(0.2f)
            )
        }
        _uiState.update {
            it.copy(
                activeTool = StudioTool.COST_CALCULATOR,
                statusBannerMessage = if (ar) {
                    "تم استيراد أبعاد الصندوق (${layout.totalSheetWidthMm.toInt()}×${layout.totalSheetHeightMm.toInt()} مم، طول القص ${String.format(Locale.US, "%.2f", layout.totalCutLengthMeters)} م) إلى حاسبة التكلفة!"
                } else {
                    "Synced Box Layout (${layout.totalSheetWidthMm.toInt()}×${layout.totalSheetHeightMm.toInt()}mm, ${String.format(Locale.US, "%.2f", layout.totalCutLengthMeters)}m cut) into Cost Calculator!"
                }
            )
        }
    }

    fun syncCostFromVectorOptimizer() {
        val state = _uiState.value
        val vecConfig = state.vectorConfig
        val preview = state.vectorPreview ?: return
        val ar = state.isArabic
        val cutMeters = ((preview.totalCutLengthMm / 1000f) * 100f).toInt() / 100f
        updateCostConfig {
            it.copy(
                projectName = if (ar) "قص فيكتور: ${state.artworkTitle}" else "${state.artworkTitle} Vector Cut",
                itemWidthMm = vecConfig.targetWidthMm,
                itemLengthMm = vecConfig.targetWidthMm,
                cutLengthMeters = cutMeters.coerceAtLeast(0.15f)
            )
        }
        _uiState.update {
            it.copy(
                activeTool = StudioTool.COST_CALCULATOR,
                statusBannerMessage = if (ar) {
                    "تم استيراد بيانات الفيكتور (${vecConfig.targetWidthMm.toInt()}×${vecConfig.targetWidthMm.toInt()} مم، طول القص ${String.format(Locale.US, "%.2f", cutMeters)} م) إلى حاسبة التكلفة!"
                } else {
                    "Synced Vector Job (${vecConfig.targetWidthMm.toInt()}×${vecConfig.targetWidthMm.toInt()}mm, ${String.format(Locale.US, "%.2f", cutMeters)}m cut) into Cost Calculator!"
                }
            )
        }
    }

    fun saveCurrentQuote() {
        val quote = currentQuoteSnapshot()
        val state = _uiState.value
        val ar = state.isArabic
        val cur = state.currency
        _uiState.update {
            it.copy(
                savedQuotes = listOf(quote) + it.savedQuotes,
                statusBannerMessage = if (ar) {
                    "تم حفظ عرض السعر \"${quote.projectName}\" على الجهاز (سعر البيع: ${cur.format(quote.retailPrice, true)})"
                } else {
                    "Saved quote \"${quote.projectName}\" on device (${cur.format(quote.retailPrice, false)} retail)"
                }
            )
        }
        persistQuotes()
    }

    /** Builds a quote from the current calculator state without saving it. */
    fun currentQuoteSnapshot(): SavedWorkshopQuote {
        val state = _uiState.value
        val cfg = state.costConfig
        val res = state.costResult
        val ar = state.isArabic
        val cur = state.currency
        val quote = SavedWorkshopQuote(
            id = UUID.randomUUID().toString(),
            projectName = cfg.projectName.ifBlank { if (ar) "مشروع قص ليزر مخصص" else "Custom Laser Job" },
            retailPrice = res.recommendedRetailPrice,
            unitCost = res.totalUnitProductionCost,
            marginPercent = cfg.profitMarginPercent.toInt(),
            summaryTextEn = String.format(
                Locale.US,
                "Sheet: %dx%dmm (%s) • Cut: %.2fm @ %dmm/s • Retail: %s/unit",
                cfg.sheetWidthMm.toInt(),
                cfg.sheetLengthMm.toInt(),
                cur.format(cfg.materialCostPerSheet, false),
                cfg.cutLengthMeters,
                res.effectiveSpeedMmPerSec.toInt(),
                cur.format(res.recommendedRetailPrice, false)
            ),
            summaryTextAr = String.format(
                Locale.US,
                "اللوح: %dx%d مم (%s) • القص: %.2f م بسرعة %d مم/ث • سعر البيع: %s للقطعة",
                cfg.sheetWidthMm.toInt(),
                cfg.sheetLengthMm.toInt(),
                cur.format(cfg.materialCostPerSheet, true),
                cfg.cutLengthMeters,
                res.effectiveSpeedMmPerSec.toInt(),
                cur.format(res.recommendedRetailPrice, true)
            ),
            currencyCode = cur.code,
            itemWidthMm = cfg.itemWidthMm,
            itemLengthMm = cfg.itemLengthMm,
            batchQuantity = cfg.batchQuantity,
            materialCost = res.materialCostPerItem,
            machineCost = res.machineCostPerItem,
            electricityCost = res.electricityCostPerItem,
            setupCost = res.setupCostPerItem
        )
        return quote
    }
}

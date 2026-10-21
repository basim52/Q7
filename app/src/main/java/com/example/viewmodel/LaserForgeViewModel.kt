package com.example.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
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
import com.example.model.WorkshopProfile
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
    val creditsRemaining: Int = 15,
    val currentProfile: WorkshopProfile = AVAILABLE_PROFILES.first(),
    val availableProfiles: List<WorkshopProfile> = AVAILABLE_PROFILES,
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
    val savedQuotes: List<SavedWorkshopQuote> = INITIAL_SAVED_QUOTES,

    // Export Modal & History
    val exportHistory: List<ExportHistoryItem> = emptyList(),
    val activeExportModalItem: ExportHistoryItem? = null
)

val AVAILABLE_PROFILES = listOf(
    WorkshopProfile(
        id = "pro_operator",
        nameEn = "Alex Rivera",
        nameAr = "م. باسم العتيبي",
        roleEn = "Lead CNC & Laser Operator",
        roleAr = "مهندس تشغيل ليزر و CNC رئيسي",
        workshopNameEn = "Apex FabWorks Studio",
        workshopNameAr = "استوديو ليزر فورج للتصنيع الرقمي",
        planTierEn = "Pro Workshop Tier",
        planTierAr = "باقة الورش الاحترافية Pro",
        avatarInitials = "ب ع",
        isSignedIn = true
    ),
    WorkshopProfile(
        id = "maker_lead",
        nameEn = "Maya Lin",
        nameAr = "م. سارة المنصور",
        roleEn = "Industrial Enclosure Designer",
        roleAr = "مصممة صناديق ومنتجات ليزر",
        workshopNameEn = "Kinetix Laser Lab",
        workshopNameAr = "مختبر كينتكس للقص بالليزر",
        planTierEn = "Studio Team Tier",
        planTierAr = "باقة فرق العمل Studio",
        avatarInitials = "س م",
        isSignedIn = true
    ),
    WorkshopProfile(
        id = "guest_mode",
        nameEn = "Guest Operator",
        nameAr = "زائر (وضع المعاينة)",
        roleEn = "Unauthenticated Session",
        roleAr = "جلسة بدون تسجيل دخول",
        workshopNameEn = "Local Bench Mode",
        workshopNameAr = "وضع التجربة المحلي",
        planTierEn = "Sign In Required for Export",
        planTierAr = "يتطلب تسجيل الدخول لتصدير SVG",
        avatarInitials = "ز",
        isSignedIn = false
    )
)

val INITIAL_SAVED_QUOTES = listOf(
    SavedWorkshopQuote(
        id = "q1",
        projectName = "صندوق راسبيري باي (خشب بتولا 3 مم) / Raspberry Pi Enclosure",
        retailPrice = 24.80f,
        unitCost = 11.16f,
        marginPercent = 55,
        summaryTextEn = "Sheet: 600×400mm ($19.00) • Cut: 3.85m @ 28mm/s • Retail: $24.80/unit",
        summaryTextAr = "اللوح: 600×400 مم (19.00$) • القص: 3.85 م بسرعة 28 مم/ث • سعر البيع: 24.80$ للقطعة"
    ),
    SavedWorkshopQuote(
        id = "q2",
        projectName = "طقم قواعد أكواب ماندالا (خشب جوز) / Mandala Coaster Set",
        retailPrice = 34.50f,
        unitCost = 14.49f,
        marginPercent = 58,
        summaryTextEn = "Sheet: 600×400mm ($42.00) • Cut: 6.20m @ 18mm/s • Retail: $34.50/set",
        summaryTextAr = "اللوح: 600×400 مم (42.00$) • القص: 6.20 م بسرعة 18 مم/ث • سعر البيع: 34.50$ للطقم"
    )
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
    val speed = max(1f, config.cuttingSpeedMmPerSec)
    val cutTimeSeconds = (cutLengthMm / speed) * 1.15f
    val machineCostPerSecond = config.hourlyMachineRate / 3600f
    val machineCostPerItem = cutTimeSeconds * machineCostPerSecond

    val setupPerUnit = config.setupAndHandlingFee / max(1, config.batchQuantity).toFloat()
    val totalUnitProductionCost = materialCostPerItem + machineCostPerItem + setupPerUnit

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
        setupCostPerItem = setupPerUnit,
        totalUnitProductionCost = totalUnitProductionCost,
        wholesaleUnitPrice = wholesaleUnitPrice,
        recommendedRetailPrice = recommendedRetailPrice,
        profitPerUnit = profitPerUnit,
        totalBatchRevenue = recommendedRetailPrice * qty,
        totalBatchProfit = profitPerUnit * qty
    )
}

class LaserForgeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LaserForgeUiState())
    val uiState: StateFlow<LaserForgeUiState> = _uiState.asStateFlow()

    init {
        refreshVectorPreview()
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

    fun selectProfile(profile: WorkshopProfile) {
        _uiState.update { state ->
            val ar = state.isArabic
            state.copy(
                currentProfile = profile,
                statusBannerMessage = if (profile.isSignedIn) {
                    if (ar) {
                        "تم تبديل المشغل إلى ${profile.nameAr} (${profile.workshopNameAr})"
                    } else {
                        "Switched operator to ${profile.nameEn} (${profile.workshopNameEn})"
                    }
                } else {
                    if (ar) {
                        "تم التبديل إلى وضع الزائر — يرجى تسجيل الدخول لتصدير ملفات SVG"
                    } else {
                        "Switched to Guest Mode — Sign in to export production SVGs"
                    }
                }
            )
        }
    }

    fun topUpCredits(amount: Int = 10) {
        _uiState.update { state ->
            val next = state.creditsRemaining + amount
            state.copy(
                creditsRemaining = next,
                statusBannerMessage = if (state.isArabic) {
                    "تم شحن +$amount رصيد بنجاح! (الرصيد المتبقي: $next)"
                } else {
                    "+$amount Workshop Credits added! ($next Credits Remaining)"
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
        if (!state.currentProfile.isSignedIn) {
            _uiState.update {
                it.copy(
                    statusBannerMessage = if (ar) {
                        "يلزم تسجيل الدخول: اختر حساب مشغل فعال من القائمة العلوية لتصدير ملفات SVG."
                    } else {
                        "Authentication required: Select an active Workshop Operator profile in the header to export SVGs."
                    }
                )
            }
            return
        }
        if (state.creditsRemaining <= 0) {
            _uiState.update {
                it.copy(
                    statusBannerMessage = if (ar) {
                        "رصيدك 0! اضغط على شارة الرصيد في الأعلى لشحن +10 رصيد مجاني."
                    } else {
                        "0 Credits Remaining! Tap the Credits badge in the top bar to top up +10 credits."
                    }
                )
            }
            return
        }
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
            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
            val exportItem = ExportHistoryItem(
                id = UUID.randomUUID().toString(),
                title = "${state.artworkTitle} (${state.vectorConfig.softwarePreset.label(ar)})",
                toolNameEn = "Vector Optimizer",
                toolNameAr = "محول الفيكتور",
                fileName = "${safeName}_cut.svg",
                timestampLabel = timestamp,
                svgContent = svgContent,
                creditsUsed = 1
            )
            _uiState.update { curr ->
                val remaining = (curr.creditsRemaining - 1).coerceAtLeast(0)
                curr.copy(
                    isGeneratingVector = false,
                    creditsRemaining = remaining,
                    exportHistory = listOf(exportItem) + curr.exportHistory,
                    activeExportModalItem = exportItem,
                    statusBannerMessage = if (curr.isArabic) {
                        "تم توليد ملف SVG بنجاح (${preview.closedLoopCount} مسار مغلق) • خُصم 1 رصيد (المتبقي $remaining)"
                    } else {
                        "Generated clean SVG (${preview.closedLoopCount} closed paths) • 1 Credit deducted ($remaining left)"
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
        if (!state.currentProfile.isSignedIn) {
            _uiState.update {
                it.copy(
                    statusBannerMessage = if (ar) {
                        "يلزم تسجيل الدخول: اختر حساب مشغل من القائمة العلوية لتصدير ملف SVG."
                    } else {
                        "Authentication required: Switch to an authenticated Operator profile in the header to export SVGs."
                    }
                )
            }
            return
        }
        if (state.creditsRemaining <= 0) {
            _uiState.update {
                it.copy(
                    statusBannerMessage = if (ar) {
                        "رصيدك 0! اضغط على شارة الرصيد في الأعلى لشحن +10 رصيد."
                    } else {
                        "0 Credits Remaining! Tap the Credits badge in the header to top up credits."
                    }
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isExportingBox = true, statusBannerMessage = null) }
            delay(450)
            val cfg = state.boxConfig
            val layout = state.boxLayout
            val svgContent = withContext(Dispatchers.Default) {
                BoxSvgGenerator.exportBoxSvg(cfg, layout)
            }
            val dims = "${cfg.widthMm.toInt()}x${cfg.heightMm.toInt()}x${cfg.depthMm.toInt()}mm"
            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
            val exportItem = ExportHistoryItem(
                id = UUID.randomUUID().toString(),
                title = "Box $dims (${cfg.thicknessMm}mm)",
                toolNameEn = "Box Generator",
                toolNameAr = "مولد الصناديق",
                fileName = "box_${dims}_${cfg.thicknessMm}mm.svg",
                timestampLabel = timestamp,
                svgContent = svgContent,
                creditsUsed = 1
            )
            _uiState.update { curr ->
                val remaining = (curr.creditsRemaining - 1).coerceAtLeast(0)
                curr.copy(
                    isExportingBox = false,
                    creditsRemaining = remaining,
                    exportHistory = listOf(exportItem) + curr.exportHistory,
                    activeExportModalItem = exportItem,
                    statusBannerMessage = if (curr.isArabic) {
                        "تم تصدير مخطط الصندوق (${layout.panels.size} ألواح - $dims) • خُصم 1 رصيد (المتبقي $remaining)"
                    } else {
                        "Exported ${layout.panels.size}-panel Box SVG ($dims) • 1 Credit deducted ($remaining left)"
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
                    "تم اختيار ${preset.nameAr} (${preset.defaultSheetCost}$ للوح بسرعة ${preset.recommendedSpeedMmPerSec.toInt()} مم/ث)"
                } else {
                    "Loaded ${preset.nameEn} sheet rates ($${preset.defaultSheetCost}/sheet @ ${preset.recommendedSpeedMmPerSec.toInt()}mm/s)"
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
        val state = _uiState.value
        val cfg = state.costConfig
        val res = state.costResult
        val ar = state.isArabic
        val quote = SavedWorkshopQuote(
            id = UUID.randomUUID().toString(),
            projectName = cfg.projectName.ifBlank { if (ar) "مشروع قص ليزر مخصص" else "Custom Laser Job" },
            retailPrice = res.recommendedRetailPrice,
            unitCost = res.totalUnitProductionCost,
            marginPercent = cfg.profitMarginPercent.toInt(),
            summaryTextEn = String.format(
                Locale.US,
                "Sheet: %dx%dmm ($%.2f) • Cut: %.2fm @ %dmm/s • Retail: $%.2f/unit",
                cfg.sheetWidthMm.toInt(),
                cfg.sheetLengthMm.toInt(),
                cfg.materialCostPerSheet,
                cfg.cutLengthMeters,
                cfg.cuttingSpeedMmPerSec.toInt(),
                res.recommendedRetailPrice
            ),
            summaryTextAr = String.format(
                Locale.US,
                "اللوح: %dx%d مم ($%.2f) • القص: %.2f م بسرعة %d مم/ث • سعر البيع: $%.2f للقطعة",
                cfg.sheetWidthMm.toInt(),
                cfg.sheetLengthMm.toInt(),
                cfg.materialCostPerSheet,
                cfg.cutLengthMeters,
                cfg.cuttingSpeedMmPerSec.toInt(),
                res.recommendedRetailPrice
            )
        )
        _uiState.update {
            it.copy(
                savedQuotes = listOf(quote) + it.savedQuotes,
                statusBannerMessage = if (ar) {
                    "تم حفظ عرض السعر \"${quote.projectName}\" (سعر البيع: $${String.format(Locale.US, "%.2f", quote.retailPrice)})"
                } else {
                    "Saved quote for \"${quote.projectName}\" ($${String.format(Locale.US, "%.2f", quote.retailPrice)} retail)"
                }
            )
        }
    }
}

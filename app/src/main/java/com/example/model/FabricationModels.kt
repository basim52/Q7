package com.example.model

import android.graphics.Bitmap

enum class StudioTool(
    val route: String,
    val titleEn: String,
    val titleAr: String,
    val shortTitleEn: String,
    val shortTitleAr: String,
    val badgeEn: String,
    val badgeAr: String
) {
    VECTOR_OPTIMIZER(
        route = "vector_optimizer",
        titleEn = "Vector & Image Optimizer",
        titleAr = "محول ومحسن الصور إلى فيكتور",
        shortTitleEn = "Vectorizer",
        shortTitleAr = "محول الفيكتور",
        badgeEn = "TOOL 01",
        badgeAr = "الأداة 01"
    ),
    BOX_GENERATOR(
        route = "box_generator",
        titleEn = "Box & Enclosure Generator",
        titleAr = "مولد الصناديق والتعشيق بالليزر",
        shortTitleEn = "Box CAD",
        shortTitleAr = "مولد الصناديق",
        badgeEn = "TOOL 02",
        badgeAr = "الأداة 02"
    ),
    COST_CALCULATOR(
        route = "cost_calculator",
        titleEn = "Workshop Cost & Pricing",
        titleAr = "حاسبة التكاليف وتسعير الورشة",
        shortTitleEn = "Pricing",
        shortTitleAr = "حاسبة التسعير",
        badgeEn = "TOOL 03",
        badgeAr = "الأداة 03"
    );

    fun title(isArabic: Boolean) = if (isArabic) titleAr else titleEn
    fun shortTitle(isArabic: Boolean) = if (isArabic) shortTitleAr else shortTitleEn
    fun badge(isArabic: Boolean) = if (isArabic) badgeAr else badgeEn
}

data class WorkshopProfile(
    val id: String,
    val nameEn: String,
    val nameAr: String,
    val roleEn: String,
    val roleAr: String,
    val workshopNameEn: String,
    val workshopNameAr: String,
    val planTierEn: String,
    val planTierAr: String,
    val avatarInitials: String,
    val isSignedIn: Boolean = true
) {
    fun name(isArabic: Boolean) = if (isArabic) nameAr else nameEn
    fun role(isArabic: Boolean) = if (isArabic) roleAr else roleEn
    fun workshopName(isArabic: Boolean) = if (isArabic) workshopNameAr else workshopNameEn
    fun planTier(isArabic: Boolean) = if (isArabic) planTierAr else planTierEn
}

enum class SampleArtworkPreset(
    val titleEn: String,
    val titleAr: String,
    val subtitleEn: String,
    val subtitleAr: String,
    val recommendedThreshold: Int
) {
    GEAR_MANDALA(
        "Mechanical Gear Mandala",
        "ماندالا التروس الميكانيكية",
        "Precision planetary gear & ornament",
        "زخرفة تروس هندسية دقيقة للقص",
        135
    ),
    WOLF_EMBLEM(
        "Geometric Wolf Crest",
        "شعار الذئب الهندسي",
        "Angular poly-art CNC plaque",
        "تصميم مضلعات هندسية للنقش والقص",
        120
    ),
    SPINDLE_BADGE(
        "Laser Workshop Seal",
        "ختم ورشة الليزر الصناعي",
        "Industrial shop emblem & crosshair",
        "شارة ورشة صناعية مع فتحات تثبيت",
        140
    ),
    FILIGREE_COASTER(
        "Botanical Lattice Coaster",
        "قاعدة أكواب زخرفية مفرغة",
        "Interlaced floral cutout coaster",
        "نقش نباتي متداخل مهيأ للتفريغ بالليزر",
        128
    );

    fun title(isArabic: Boolean) = if (isArabic) titleAr else titleEn
    fun subtitle(isArabic: Boolean) = if (isArabic) subtitleAr else subtitleEn
}

enum class TargetLaserSoftware(
    val labelEn: String,
    val labelAr: String,
    val strokeColorHex: String,
    val strokeWidthMm: Float,
    val descriptionEn: String,
    val descriptionAr: String
) {
    LIGHTBURN(
        "LightBurn (C00 Cut)",
        "لايت بيرن LightBurn (قص C00)",
        "#FF0000",
        0.05f,
        "0.05mm Red Hairline • Layer C00",
        "خط قص أحمر 0.05 مم • طبقة C00 القياسية"
    ),
    SILHOUETTE(
        "Silhouette / Cricut",
        "سيلويت Silhouette / كريكيت",
        "#000000",
        0.10f,
        "0.10mm Compound Vector Cut Path",
        "مسار قص فيكتور مركب بسماكة 0.10 مم"
    ),
    GLOWFORGE(
        "Glowforge UI Ready",
        "جلوفورج Glowforge SVG",
        "#0066FF",
        0.08f,
        "Color-coded Cut & Score Vector SVG",
        "ملف SVG مرمز بالألوان للقص والتحديد"
    ),
    CNC_GRBL(
        "CNC Router / GRBL",
        "راوتر CNC / نظام GRBL",
        "#06B6D4",
        0.15f,
        "Closed Polyline Offset Toolpath",
        "مسار أداة مغلق جاهز للتحويل إلى G-Code"
    );

    fun label(isArabic: Boolean) = if (isArabic) labelAr else labelEn
    fun description(isArabic: Boolean) = if (isArabic) descriptionAr else descriptionEn
}

data class VectorOptimizerConfig(
    val threshold: Int = 135,
    val edgeDetection: Boolean = true,
    val invertColors: Boolean = false,
    val speckleSuppression: Int = 2,
    val pathSmoothing: Float = 0.45f,
    val targetWidthMm: Float = 120f,
    val softwarePreset: TargetLaserSoftware = TargetLaserSoftware.LIGHTBURN,
    val showVectorOverlay: Boolean = true
)

data class VectorPreviewResult(
    val processedBitmap: Bitmap,
    val contourPaths: List<List<Pair<Float, Float>>>,
    val nodeCount: Int,
    val closedLoopCount: Int,
    val totalCutLengthMm: Float,
    val estimatedLaserSeconds: Int
)

enum class EnclosureStyle(
    val labelEn: String,
    val labelAr: String,
    val panelCount: Int
) {
    CLOSED_SIX_PANEL("Closed 6-Panel Box", "صندوق مغلق (6 أوجه)", 6),
    OPEN_TOP_TRAY("Open-Top Organizer", "صندوق منظم مفتوح من الأعلى (5 أوجه)", 5),
    DIVIDED_TRAY("Open Tray + Center Divider", "صندوق منظم مع قاطع وسطي (6 قطع)", 6);

    fun label(isArabic: Boolean) = if (isArabic) labelAr else labelEn
}

data class MaterialPreset(
    val nameEn: String,
    val nameAr: String,
    val thicknessMm: Float,
    val defaultSheetCost: Float,
    val sheetWidthMm: Float,
    val sheetHeightMm: Float,
    val recommendedSpeedMmPerSec: Float
) {
    fun name(isArabic: Boolean) = if (isArabic) nameAr else nameEn
}

val DEFAULT_MATERIAL_PRESETS = listOf(
    MaterialPreset("3.0mm Laser MDF", "خشب MDF ليزر 3.0 مم", 3.0f, 14.50f, 600f, 400f, 28f),
    MaterialPreset("3.2mm (1/8\") Birch Ply", "أبلكاش بتولا 3.2 مم", 3.2f, 19.00f, 600f, 400f, 32f),
    MaterialPreset("3.0mm Cast Acrylic", "أكريليك صب 3.0 مم", 3.0f, 29.50f, 600f, 400f, 20f),
    MaterialPreset("5.0mm Baltic Birch", "خشب بتولا 5.0 مم", 5.0f, 26.00f, 600f, 400f, 18f),
    MaterialPreset("6.0mm Walnut Veneer", "خشب جوز طبيعي 6.0 مم", 6.0f, 42.00f, 600f, 400f, 15f)
)

data class BoxGeneratorConfig(
    val widthMm: Float = 120f,
    val heightMm: Float = 75f,
    val depthMm: Float = 90f,
    val thicknessMm: Float = 3.0f,
    val tabSizeMm: Float = 15.0f,
    val kerfMm: Float = 0.12f,
    val enclosureStyle: EnclosureStyle = EnclosureStyle.CLOSED_SIX_PANEL,
    val showIsometric3D: Boolean = false
)

data class BoxPanelGeometry(
    val id: String,
    val label: String,
    val dimensionsLabel: String,
    val offsetXMm: Float,
    val offsetYMm: Float,
    val nominalWidthMm: Float,
    val nominalHeightMm: Float,
    val outlinePointsMm: List<Pair<Float, Float>>
)

data class BoxLayoutSummary(
    val panels: List<BoxPanelGeometry>,
    val totalSheetWidthMm: Float,
    val totalSheetHeightMm: Float,
    val totalCutLengthMeters: Float,
    val totalTabsCount: Int,
    val internalVolumeCm3: Float
)

data class CostCalculatorConfig(
    val projectName: String = "صندوق تعشيق بالليزر مخصص",
    val materialCostPerSheet: Float = 19.00f,
    val sheetWidthMm: Float = 600f,
    val sheetLengthMm: Float = 400f,
    val itemWidthMm: Float = 260f,
    val itemLengthMm: Float = 190f,
    val cutLengthMeters: Float = 3.85f,
    val cuttingSpeedMmPerSec: Float = 28f,
    val hourlyMachineRate: Float = 45.00f,
    val setupAndHandlingFee: Float = 2.50f,
    val wasteFactorPercent: Float = 15f,
    val profitMarginPercent: Float = 55f,
    val batchQuantity: Int = 10
)

data class CostCalculationResult(
    val sheetAreaCm2: Float,
    val itemAreaCm2: Float,
    val itemsPerSheet: Int,
    val materialCostPerItem: Float,
    val cutTimeSecondsPerItem: Float,
    val machineCostPerItem: Float,
    val setupCostPerItem: Float,
    val totalUnitProductionCost: Float,
    val wholesaleUnitPrice: Float,
    val recommendedRetailPrice: Float,
    val profitPerUnit: Float,
    val totalBatchRevenue: Float,
    val totalBatchProfit: Float
)

data class ExportHistoryItem(
    val id: String,
    val title: String,
    val toolNameEn: String,
    val toolNameAr: String,
    val fileName: String,
    val timestampLabel: String,
    val svgContent: String,
    val creditsUsed: Int = 1
) {
    fun toolName(isArabic: Boolean) = if (isArabic) toolNameAr else toolNameEn
}

data class SavedWorkshopQuote(
    val id: String,
    val projectName: String,
    val retailPrice: Float,
    val unitCost: Float,
    val marginPercent: Int,
    val summaryTextEn: String,
    val summaryTextAr: String
) {
    fun summaryText(isArabic: Boolean) = if (isArabic) summaryTextAr else summaryTextEn
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.StudioTool
import com.example.ui.components.StatusNotificationBanner
import com.example.ui.components.StudioTopHeader
import com.example.ui.components.SvgExportModalDialog
import com.example.ui.screens.BoxGeneratorScreen
import com.example.ui.screens.CostCalculatorScreen
import com.example.ui.screens.VectorOptimizerScreen
import com.example.ui.theme.LaserForgeTheme
import com.example.viewmodel.LaserForgeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LaserForgeViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaserForgeTheme(
                darkTheme = uiState.isDarkTheme,
                isArabic = uiState.isArabic
            ) {
                LaserForgeStudioApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LaserForgeStudioApp(
    viewModel: LaserForgeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isArabic = uiState.isArabic

    BackHandler(enabled = uiState.activeTool != StudioTool.VECTOR_OPTIMIZER) {
        viewModel.selectTool(StudioTool.VECTOR_OPTIMIZER)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 680.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                StudioTopHeader(
                    workshopName = uiState.workshopName,
                    currency = uiState.currency,
                    exportHistory = uiState.exportHistory,
                    isDarkTheme = uiState.isDarkTheme,
                    isArabic = isArabic,
                    onToggleTheme = viewModel::toggleTheme,
                    onToggleLanguage = viewModel::toggleLanguage,
                    onSetWorkshopName = viewModel::setWorkshopName,
                    onSetCurrency = viewModel::setCurrency,
                    onOpenExportItem = viewModel::openExportModal,
                    onClearHistory = viewModel::clearExportHistory
                )
            },
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        StudioTool.entries.forEach { tool ->
                            val selected = uiState.activeTool == tool
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectTool(tool) },
                                icon = {
                                    Icon(
                                        imageVector = tool.icon(selected),
                                        contentDescription = tool.title(isArabic)
                                    )
                                },
                                label = { Text(tool.shortTitle(isArabic)) },
                                modifier = Modifier.testTag("nav_tab_${tool.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        StudioTool.entries.forEach { tool ->
                            val selected = uiState.activeTool == tool
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.selectTool(tool) },
                                icon = {
                                    Icon(
                                        imageVector = tool.icon(selected),
                                        contentDescription = tool.title(isArabic)
                                    )
                                },
                                label = { Text(tool.shortTitle(isArabic)) },
                                modifier = Modifier.testTag("nav_rail_${tool.route}")
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    StatusNotificationBanner(
                        message = uiState.statusBannerMessage,
                        onDismiss = viewModel::dismissBanner
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        when (uiState.activeTool) {
                            StudioTool.VECTOR_OPTIMIZER -> {
                                VectorOptimizerScreen(
                                    isArabic = isArabic,
                                    selectedPreset = uiState.selectedPreset,
                                    artworkTitle = uiState.artworkTitle,
                                    config = uiState.vectorConfig,
                                    preview = uiState.vectorPreview,
                                    isGenerating = uiState.isGeneratingVector,
                                    onSelectPreset = viewModel::selectSamplePreset,
                                    onCustomBitmapSelected = viewModel::onCustomBitmapLoaded,
                                    onUpdateConfig = viewModel::updateVectorConfig,
                                    onGenerateSvg = viewModel::generateAndExportVectorSvg,
                                    onSyncToCostCalculator = viewModel::syncCostFromVectorOptimizer
                                )
                            }

                            StudioTool.BOX_GENERATOR -> {
                                BoxGeneratorScreen(
                                    isArabic = isArabic,
                                    config = uiState.boxConfig,
                                    layout = uiState.boxLayout,
                                    isExporting = uiState.isExportingBox,
                                    onUpdateConfig = viewModel::updateBoxConfig,
                                    onApplyMaterialPreset = viewModel::applyBoxMaterialPreset,
                                    onExportBoxSvg = viewModel::exportBoxLayoutSvg,
                                    onSyncToCostCalculator = viewModel::syncCostFromBoxGenerator
                                )
                            }

                            StudioTool.COST_CALCULATOR -> {
                                CostCalculatorScreen(
                                    isArabic = isArabic,
                                    config = uiState.costConfig,
                                    result = uiState.costResult,
                                    savedQuotes = uiState.savedQuotes,
                                    currency = uiState.currency,
                                    workshopName = uiState.workshopName,
                                    currentQuote = viewModel::currentQuoteSnapshot,
                                    onDeleteQuote = viewModel::deleteQuote,
                                    onUpdateConfig = viewModel::updateCostConfig,
                                    onApplyMaterialPreset = viewModel::applyPricingMaterialPreset,
                                    onSyncFromVector = viewModel::syncCostFromVectorOptimizer,
                                    onSyncFromBox = viewModel::syncCostFromBoxGenerator,
                                    onSaveQuote = viewModel::saveCurrentQuote
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    uiState.activeExportModalItem?.let { exportItem ->
        SvgExportModalDialog(
            exportItem = exportItem,
            isArabic = isArabic,
            onDismiss = viewModel::dismissExportModal
        )
    }
}

private fun StudioTool.icon(selected: Boolean): ImageVector = when (this) {
    StudioTool.VECTOR_OPTIMIZER -> if (selected) Icons.Filled.AutoFixHigh else Icons.Outlined.AutoFixHigh
    StudioTool.BOX_GENERATOR -> if (selected) Icons.Filled.ViewInAr else Icons.Outlined.ViewInAr
    StudioTool.COST_CALCULATOR -> if (selected) Icons.Filled.Calculate else Icons.Outlined.Calculate
}

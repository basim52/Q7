package com.example.engine

import com.example.model.BoxGeneratorConfig
import com.example.model.BoxLayoutSummary
import com.example.model.BoxPanelGeometry
import com.example.model.EnclosureStyle
import java.util.Locale
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt

object BoxSvgGenerator {

    /**
     * Generates all interlocking finger-joint panels dynamically from the user's BoxGeneratorConfig.
     * Supports Closed 6-Panel Box, Open-Top Organizer Tray (5 panels), and Divided Component Bin (6 panels).
     */
    fun generateBoxLayout(config: BoxGeneratorConfig): BoxLayoutSummary {
        val w = config.widthMm.coerceIn(30f, 500f)
        val h = config.heightMm.coerceIn(25f, 400f)
        val d = config.depthMm.coerceIn(30f, 500f)
        val t = config.thicknessMm.coerceIn(1.5f, 15f)
        val tab = config.tabSizeMm.coerceIn(6f, 60f)
        val kerf = config.kerfMm.coerceIn(0f, 0.5f)

        val spacing = 10f
        val panels = mutableListOf<BoxPanelGeometry>()
        var totalTabs = 0

        // Helper to count odd number of finger segments along a given length
        fun computeOddSegments(length: Float): Int {
            val raw = (length / tab).roundToInt().coerceAtLeast(3)
            return if (raw % 2 == 0) raw + 1 else raw
        }

        val segW = computeOddSegments(w)
        val segH = computeOddSegments(h)
        val segD = computeOddSegments(d)

        totalTabs += (segW + segH + segD) * 4

        // Row 1: Front (W x H), Back (W x H), Bottom (W x D)
        val frontPts = buildFingerPanelOutline(
            width = w,
            height = h,
            thickness = t,
            kerf = kerf,
            topOut = config.enclosureStyle != EnclosureStyle.OPEN_TOP_TRAY,
            topFlat = config.enclosureStyle == EnclosureStyle.OPEN_TOP_TRAY,
            bottomOut = true,
            leftOut = true,
            rightOut = true,
            segX = segW,
            segY = segH
        )
        panels.add(
            BoxPanelGeometry(
                id = "front",
                label = "FRONT PANEL",
                dimensionsLabel = "${w.toInt()}×${h.toInt()}mm",
                offsetXMm = spacing,
                offsetYMm = spacing,
                nominalWidthMm = w,
                nominalHeightMm = h,
                outlinePointsMm = frontPts
            )
        )

        val backPts = buildFingerPanelOutline(
            width = w,
            height = h,
            thickness = t,
            kerf = kerf,
            topOut = config.enclosureStyle != EnclosureStyle.OPEN_TOP_TRAY,
            topFlat = config.enclosureStyle == EnclosureStyle.OPEN_TOP_TRAY,
            bottomOut = true,
            leftOut = true,
            rightOut = true,
            segX = segW,
            segY = segH
        )
        panels.add(
            BoxPanelGeometry(
                id = "back",
                label = "BACK PANEL",
                dimensionsLabel = "${w.toInt()}×${h.toInt()}mm",
                offsetXMm = spacing * 2 + w,
                offsetYMm = spacing,
                nominalWidthMm = w,
                nominalHeightMm = h,
                outlinePointsMm = backPts
            )
        )

        val bottomPts = buildFingerPanelOutline(
            width = w,
            height = d,
            thickness = t,
            kerf = kerf,
            topOut = false,
            topFlat = false,
            bottomOut = false,
            leftOut = false,
            rightOut = false,
            segX = segW,
            segY = segD
        )
        panels.add(
            BoxPanelGeometry(
                id = "bottom",
                label = "BOTTOM BASE",
                dimensionsLabel = "${w.toInt()}×${d.toInt()}mm",
                offsetXMm = spacing * 3 + w * 2,
                offsetYMm = spacing,
                nominalWidthMm = w,
                nominalHeightMm = d,
                outlinePointsMm = bottomPts
            )
        )

        // Row 2: Left (D x H), Right (D x H), Top Lid or Center Divider
        val row2Y = spacing * 2 + max(h, d)
        val leftPts = buildFingerPanelOutline(
            width = d,
            height = h,
            thickness = t,
            kerf = kerf,
            topOut = config.enclosureStyle != EnclosureStyle.OPEN_TOP_TRAY,
            topFlat = config.enclosureStyle == EnclosureStyle.OPEN_TOP_TRAY,
            bottomOut = true,
            leftOut = false,
            rightOut = false,
            segX = segD,
            segY = segH
        )
        panels.add(
            BoxPanelGeometry(
                id = "left",
                label = "LEFT WALL",
                dimensionsLabel = "${d.toInt()}×${h.toInt()}mm",
                offsetXMm = spacing,
                offsetYMm = row2Y,
                nominalWidthMm = d,
                nominalHeightMm = h,
                outlinePointsMm = leftPts
            )
        )

        val rightPts = buildFingerPanelOutline(
            width = d,
            height = h,
            thickness = t,
            kerf = kerf,
            topOut = config.enclosureStyle != EnclosureStyle.OPEN_TOP_TRAY,
            topFlat = config.enclosureStyle == EnclosureStyle.OPEN_TOP_TRAY,
            bottomOut = true,
            leftOut = false,
            rightOut = false,
            segX = segD,
            segY = segH
        )
        panels.add(
            BoxPanelGeometry(
                id = "right",
                label = "RIGHT WALL",
                dimensionsLabel = "${d.toInt()}×${h.toInt()}mm",
                offsetXMm = spacing * 2 + d,
                offsetYMm = row2Y,
                nominalWidthMm = d,
                nominalHeightMm = h,
                outlinePointsMm = rightPts
            )
        )

        if (config.enclosureStyle == EnclosureStyle.CLOSED_SIX_PANEL) {
            val topPts = buildFingerPanelOutline(
                width = w,
                height = d,
                thickness = t,
                kerf = kerf,
                topOut = false,
                topFlat = false,
                bottomOut = false,
                leftOut = false,
                rightOut = false,
                segX = segW,
                segY = segD
            )
            panels.add(
                BoxPanelGeometry(
                    id = "top",
                    label = "TOP LID",
                    dimensionsLabel = "${w.toInt()}×${d.toInt()}mm",
                    offsetXMm = spacing * 3 + d * 2,
                    offsetYMm = row2Y,
                    nominalWidthMm = w,
                    nominalHeightMm = d,
                    outlinePointsMm = topPts
                )
            )
        } else if (config.enclosureStyle == EnclosureStyle.DIVIDED_TRAY) {
            val dividerPts = buildFingerPanelOutline(
                width = d - 2 * t,
                height = h - t,
                thickness = t,
                kerf = kerf,
                topOut = false,
                topFlat = true,
                bottomOut = true,
                leftOut = true,
                rightOut = true,
                segX = segD,
                segY = segH
            )
            panels.add(
                BoxPanelGeometry(
                    id = "divider",
                    label = "CENTER DIVIDER",
                    dimensionsLabel = "${(d - 2 * t).toInt()}×${(h - t).toInt()}mm",
                    offsetXMm = spacing * 3 + d * 2,
                    offsetYMm = row2Y,
                    nominalWidthMm = d - 2 * t,
                    nominalHeightMm = h - t,
                    outlinePointsMm = dividerPts
                )
            )
        }

        // Calculate total sheet footprint & cut path length
        var maxX = 0f
        var maxY = 0f
        var totalPerimeterMm = 0f

        for (panel in panels) {
            maxX = max(maxX, panel.offsetXMm + panel.nominalWidthMm + spacing)
            maxY = max(maxY, panel.offsetYMm + panel.nominalHeightMm + spacing)
            val pts = panel.outlinePointsMm
            for (i in 1 until pts.size) {
                totalPerimeterMm += hypot(
                    pts[i].first - pts[i - 1].first,
                    pts[i].second - pts[i - 1].second
                )
            }
        }

        val internalW = max(1f, w - 2 * t)
        val internalH = max(1f, h - 2 * t)
        val internalD = max(1f, d - 2 * t)
        val volumeCm3 = (internalW * internalH * internalD) / 1000f

        return BoxLayoutSummary(
            panels = panels,
            totalSheetWidthMm = maxX,
            totalSheetHeightMm = maxY,
            totalCutLengthMeters = totalPerimeterMm / 1000f,
            totalTabsCount = totalTabs,
            internalVolumeCm3 = volumeCm3
        )
    }

    /**
     * Builds the closed polyline path of a rectangular panel with interlocking finger joints along its 4 edges.
     */
    private fun buildFingerPanelOutline(
        width: Float,
        height: Float,
        thickness: Float,
        kerf: Float,
        topOut: Boolean,
        topFlat: Boolean,
        bottomOut: Boolean,
        leftOut: Boolean,
        rightOut: Boolean,
        segX: Int,
        segY: Int
    ): List<Pair<Float, Float>> {
        val pts = mutableListOf<Pair<Float, Float>>()
        val stepX = width / segX
        val stepY = height / segY
        val halfKerf = kerf / 2f

        // Start at top-left corner (thickness inset)
        pts.add(thickness to thickness)

        // 1. TOP EDGE (left to right)
        if (topFlat) {
            pts.add((width - thickness) to thickness)
        } else {
            for (i in 0 until segX) {
                val isTab = if (topOut) (i % 2 == 1) else (i % 2 == 0)
                val x0 = (i * stepX).coerceIn(thickness, width - thickness) - (if (isTab) halfKerf else 0f)
                val x1 = ((i + 1) * stepX).coerceIn(thickness, width - thickness) + (if (isTab) halfKerf else 0f)
                val yLevel = if (isTab) 0f else thickness
                pts.add(x0 to yLevel)
                pts.add(x1 to yLevel)
            }
            pts.add((width - thickness) to thickness)
        }

        // 2. RIGHT EDGE (top to bottom)
        for (i in 0 until segY) {
            val isTab = if (rightOut) (i % 2 == 1) else (i % 2 == 0)
            val y0 = (i * stepY).coerceIn(thickness, height - thickness) - (if (isTab) halfKerf else 0f)
            val y1 = ((i + 1) * stepY).coerceIn(thickness, height - thickness) + (if (isTab) halfKerf else 0f)
            val xLevel = if (isTab) width else (width - thickness)
            pts.add(xLevel to y0)
            pts.add(xLevel to y1)
        }
        pts.add((width - thickness) to (height - thickness))

        // 3. BOTTOM EDGE (right to left)
        for (i in segX - 1 downTo 0) {
            val isTab = if (bottomOut) (i % 2 == 1) else (i % 2 == 0)
            val x1 = ((i + 1) * stepX).coerceIn(thickness, width - thickness) + (if (isTab) halfKerf else 0f)
            val x0 = (i * stepX).coerceIn(thickness, width - thickness) - (if (isTab) halfKerf else 0f)
            val yLevel = if (isTab) height else (height - thickness)
            pts.add(x1 to yLevel)
            pts.add(x0 to yLevel)
        }
        pts.add(thickness to (height - thickness))

        // 4. LEFT EDGE (bottom to top)
        for (i in segY - 1 downTo 0) {
            val isTab = if (leftOut) (i % 2 == 1) else (i % 2 == 0)
            val y1 = ((i + 1) * stepY).coerceIn(thickness, height - thickness) + (if (isTab) halfKerf else 0f)
            val y0 = (i * stepY).coerceIn(thickness, height - thickness) - (if (isTab) halfKerf else 0f)
            val xLevel = if (isTab) 0f else thickness
            pts.add(xLevel to y1)
            pts.add(xLevel to y0)
        }
        pts.add(thickness to thickness)

        return pts
    }

    /**
     * Generates a downloadable multi-part SVG file containing all interlocking box panels and optional engrave labels.
     */
    fun exportBoxSvg(config: BoxGeneratorConfig, layout: BoxLayoutSummary): String {
        val sb = StringBuilder()
        sb.appendLine("""<?xml version="1.0" encoding="UTF-8" standalone="no"?>""")
        sb.appendLine(
            """<!-- Generated by LaserForge Studio • Box ${config.widthMm.toInt()}x${config.heightMm.toInt()}x${config.depthMm.toInt()}mm | Thickness: ${config.thicknessMm}mm | Tab: ${config.tabSizeMm}mm | Kerf: ${config.kerfMm}mm -->"""
        )
        sb.appendLine(
            String.format(
                Locale.US,
                """<svg xmlns="http://www.w3.org/2000/svg" width="%.2fmm" height="%.2fmm" viewBox="0 0 %.2f %.2f">""",
                layout.totalSheetWidthMm,
                layout.totalSheetHeightMm,
                layout.totalSheetWidthMm,
                layout.totalSheetHeightMm
            )
        )

        // Cut Layer (Red #FF0000 0.05mm hairline for LightBurn / Laser CAD)
        sb.appendLine("""  <g id="CUT_PANELS" fill="none" stroke="#FF0000" stroke-width="0.08" stroke-linejoin="miter">""")
        for (panel in layout.panels) {
            val pathStr = StringBuilder()
            for ((idx, pt) in panel.outlinePointsMm.withIndex()) {
                val x = panel.offsetXMm + pt.first
                val y = panel.offsetYMm + pt.second
                if (idx == 0) {
                    pathStr.append(String.format(Locale.US, "M %.3f %.3f", x, y))
                } else {
                    pathStr.append(String.format(Locale.US, " L %.3f %.3f", x, y))
                }
            }
            pathStr.append(" Z")
            sb.appendLine("""    <path id="panel_${panel.id}" d="$pathStr" />""")
        }
        sb.appendLine("  </g>")

        // Score/Annotation Layer (Blue #0066FF)
        sb.appendLine("""  <g id="SCORE_LABELS" fill="#0066FF" font-family="monospace" font-size="4.5" text-anchor="middle">""")
        for (panel in layout.panels) {
            val cx = panel.offsetXMm + panel.nominalWidthMm / 2f
            val cy = panel.offsetYMm + panel.nominalHeightMm / 2f
            sb.appendLine(
                String.format(
                    Locale.US,
                    """    <text x="%.2f" y="%.2f">%s (%s)</text>""",
                    cx, cy, panel.label, panel.dimensionsLabel
                )
            )
        }
        sb.appendLine("  </g>")
        sb.appendLine("</svg>")
        return sb.toString()
    }
}

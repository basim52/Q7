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
        when (config.enclosureStyle) {
            EnclosureStyle.PHONE_STAND -> return summarize(buildPhoneStand(w, h, d, t, spacing), spacing, w, h, d, t, 0)
            EnclosureStyle.PICTURE_FRAME -> return summarize(buildPictureFrame(w, h, t, spacing), spacing, w, h, t * 3, 0f, 0)
            else -> Unit
        }
        val openTop = config.enclosureStyle == EnclosureStyle.OPEN_TOP_TRAY ||
            config.enclosureStyle == EnclosureStyle.DRAWER
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
            topOut = !openTop,
            topFlat = openTop,
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
            topOut = !openTop,
            topFlat = openTop,
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
            topOut = !openTop,
            topFlat = openTop,
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
            topOut = !openTop,
            topFlat = openTop,
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
        } else if (config.enclosureStyle == EnclosureStyle.DRAWER) {
            // Flat decorative drawer face, overhanging the drawer body by one thickness on each side
            val faceW = w + 2 * t
            val faceH = h + t
            panels.add(
                BoxPanelGeometry(
                    id = "drawer_face",
                    label = "DRAWER FACE",
                    dimensionsLabel = "${faceW.toInt()}×${faceH.toInt()}mm",
                    offsetXMm = spacing * 3 + d * 2,
                    offsetYMm = row2Y,
                    nominalWidthMm = faceW,
                    nominalHeightMm = faceH,
                    outlinePointsMm = rect(faceW, faceH)
                )
            )
        }

        return summarize(panels, spacing, w, h, d, t, totalTabs)
    }

    private fun summarize(
        panels: List<BoxPanelGeometry>,
        spacing: Float,
        w: Float,
        h: Float,
        d: Float,
        t: Float,
        totalTabs: Int
    ): BoxLayoutSummary {
        // Calculate total sheet footprint & cut path length
        var maxX = 0f
        var maxY = 0f
        var totalPerimeterMm = 0f

        for (panel in panels) {
            maxX = max(maxX, panel.offsetXMm + panel.nominalWidthMm + spacing)
            maxY = max(maxY, panel.offsetYMm + panel.nominalHeightMm + spacing)
            maxX = max(maxX, panel.offsetXMm + (panel.outlinePointsMm.maxOfOrNull { it.first } ?: 0f) + spacing)
            maxY = max(maxY, panel.offsetYMm + (panel.outlinePointsMm.maxOfOrNull { it.second } ?: 0f) + spacing)
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

    private fun rect(width: Float, height: Float): List<Pair<Float, Float>> =
        listOf(0f to 0f, width to 0f, width to height, 0f to height, 0f to 0f)

    /** Phone stand: two slanted side cheeks joined by a back rest and a front lip (slotted to the cheeks). */
    private fun buildPhoneStand(w: Float, h: Float, d: Float, t: Float, spacing: Float): List<BoxPanelGeometry> {
        val panels = mutableListOf<BoxPanelGeometry>()
        val lipH = (h * 0.18f).coerceIn(10f, 25f)
        // Side cheek profile (d wide, h tall): flat base, front lip notch, slanted back rest.
        val side = listOf(
            0f to h,
            d to h,
            d * 0.42f to 0f,
            d * 0.42f - t * 2.5f to 0f,
            d * 0.18f to h - lipH,
            d * 0.18f to h - lipH - t,
            d * 0.08f to h - lipH - t,
            d * 0.08f to h - lipH,
            0f to h - lipH,
            0f to h
        )
        listOf("left", "right").forEachIndexed { i, id ->
            panels.add(
                BoxPanelGeometry(
                    id = "stand_$id",
                    label = if (i == 0) "LEFT CHEEK" else "RIGHT CHEEK",
                    dimensionsLabel = "${d.toInt()}×${h.toInt()}mm",
                    offsetXMm = spacing + i * (d + spacing),
                    offsetYMm = spacing,
                    nominalWidthMm = d,
                    nominalHeightMm = h,
                    outlinePointsMm = side
                )
            )
        }
        val restH = (h * 0.75f).coerceAtLeast(20f)
        val row2 = spacing * 2 + h
        panels.add(
            BoxPanelGeometry(
                id = "stand_back", label = "BACK REST",
                dimensionsLabel = "${w.toInt()}×${restH.toInt()}mm",
                offsetXMm = spacing, offsetYMm = row2,
                nominalWidthMm = w, nominalHeightMm = restH,
                outlinePointsMm = rect(w, restH)
            )
        )
        panels.add(
            BoxPanelGeometry(
                id = "stand_lip", label = "FRONT LIP",
                dimensionsLabel = "${w.toInt()}×${lipH.toInt()}mm",
                offsetXMm = spacing * 2 + w, offsetYMm = row2,
                nominalWidthMm = w, nominalHeightMm = lipH,
                outlinePointsMm = rect(w, lipH)
            )
        )
        return panels
    }

    /** Picture frame: four border strips, a back plate and a fold-out easel leg. */
    private fun buildPictureFrame(w: Float, h: Float, t: Float, spacing: Float): List<BoxPanelGeometry> {
        val border = (minOf(w, h) * 0.15f).coerceIn(12f, 40f)
        val panels = mutableListOf<BoxPanelGeometry>()
        fun add(id: String, label: String, pw: Float, ph: Float, x: Float, y: Float, pts: List<Pair<Float, Float>> = rect(pw, ph)) {
            panels.add(BoxPanelGeometry(id, label, "${pw.toInt()}×${ph.toInt()}mm", x, y, pw, ph, pts))
        }
        // Mitred top/bottom strips (trapezoids) and left/right strips
        val topPts = listOf(0f to 0f, w to 0f, w - border to border, border to border, 0f to 0f)
        val sidePts = listOf(0f to 0f, border to border, border to h - border, 0f to h, 0f to 0f)
        add("frame_top", "FRAME TOP", w, border, spacing, spacing, topPts)
        add("frame_bottom", "FRAME BOTTOM", w, border, spacing, spacing * 2 + border, topPts)
        add("frame_left", "FRAME LEFT", border, h, spacing * 2 + w, spacing, sidePts)
        add("frame_right", "FRAME RIGHT", border, h, spacing * 3 + w + border, spacing, sidePts)
        val row2 = spacing * 2 + h
        add("frame_back", "BACK PLATE", w - t, h - t, spacing, row2)
        val legW = (w * 0.25f).coerceAtLeast(20f)
        val legH = h * 0.6f
        val legPts = listOf(0f to 0f, legW to 0f, legW * 0.7f to legH, legW * 0.3f to legH, 0f to 0f)
        add("frame_leg", "EASEL LEG", legW, legH, spacing * 2 + w, row2, legPts)
        return panels
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

    /**
     * Exports the same cut geometry as an AutoCAD R12 ASCII DXF (millimetres, Y axis flipped to DXF's
     * bottom-up convention). Uses only LINE entities on layer CUT for maximum compatibility
     * (LightBurn, RDWorks, LaserGRBL, Inkscape, AutoCAD).
     */
    fun exportBoxDxf(config: BoxGeneratorConfig, layout: BoxLayoutSummary): String {
        val sb = StringBuilder()
        fun pair(code: Int, value: String) { sb.append(code).append('\n').append(value).append('\n') }
        fun num(v: Float) = String.format(Locale.US, "%.4f", v)
        val sheetH = layout.totalSheetHeightMm
        pair(999, "LaserForge Studio ${config.enclosureStyle.name} ${config.widthMm.toInt()}x${config.heightMm.toInt()}x${config.depthMm.toInt()}mm t=${config.thicknessMm}")
        pair(0, "SECTION"); pair(2, "HEADER")
        pair(9, "\$ACADVER"); pair(1, "AC1009")
        pair(9, "\$INSUNITS"); pair(70, "4")
        pair(9, "\$EXTMIN"); pair(10, "0.0"); pair(20, "0.0")
        pair(9, "\$EXTMAX"); pair(10, num(layout.totalSheetWidthMm)); pair(20, num(sheetH))
        pair(0, "ENDSEC")
        pair(0, "SECTION"); pair(2, "TABLES")
        pair(0, "TABLE"); pair(2, "LAYER"); pair(70, "1")
        pair(0, "LAYER"); pair(2, "CUT"); pair(70, "0"); pair(62, "1"); pair(6, "CONTINUOUS")
        pair(0, "ENDTAB")
        pair(0, "ENDSEC")
        pair(0, "SECTION"); pair(2, "ENTITIES")
        for (panel in layout.panels) {
            val pts = panel.outlinePointsMm
            for (i in 1 until pts.size) {
                val x0 = panel.offsetXMm + pts[i - 1].first
                val y0 = sheetH - (panel.offsetYMm + pts[i - 1].second)
                val x1 = panel.offsetXMm + pts[i].first
                val y1 = sheetH - (panel.offsetYMm + pts[i].second)
                if (kotlin.math.abs(x0 - x1) < 1e-4f && kotlin.math.abs(y0 - y1) < 1e-4f) continue
                pair(0, "LINE"); pair(8, "CUT")
                pair(10, num(x0)); pair(20, num(y0)); pair(30, "0.0")
                pair(11, num(x1)); pair(21, num(y1)); pair(31, "0.0")
            }
        }
        pair(0, "ENDSEC")
        pair(0, "EOF")
        return sb.toString()
    }
}

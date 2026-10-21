package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.example.model.SampleArtworkPreset
import com.example.model.VectorOptimizerConfig
import com.example.model.VectorPreviewResult
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object SvgVectorEngine {

    private const val PROCESS_SIZE = 160

    /**
     * Generates a high-contrast procedural sample artwork bitmap for instant testing in the workshop.
     */
    fun createSampleArtworkBitmap(preset: SampleArtworkPreset, size: Int = 320): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }
        val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        val cx = size / 2f
        val cy = size / 2f
        val rMax = size * 0.43f

        when (preset) {
            SampleArtworkPreset.GEAR_MANDALA -> {
                // Outer gear teeth
                val teeth = 24
                val outerR = rMax
                val rootR = rMax * 0.86f
                val gearPath = Path()
                for (i in 0 until teeth) {
                    val a1 = (i * 2.0 * PI / teeth).toFloat()
                    val a2 = ((i + 0.35) * 2.0 * PI / teeth).toFloat()
                    val a3 = ((i + 0.5) * 2.0 * PI / teeth).toFloat()
                    val a4 = ((i + 0.85) * 2.0 * PI / teeth).toFloat()
                    val x1 = cx + rootR * cos(a1)
                    val y1 = cy + rootR * sin(a1)
                    val x2 = cx + outerR * cos(a2)
                    val y2 = cy + outerR * sin(a2)
                    val x3 = cx + outerR * cos(a3)
                    val y3 = cy + outerR * sin(a3)
                    val x4 = cx + rootR * cos(a4)
                    val y4 = cy + rootR * sin(a4)
                    if (i == 0) gearPath.moveTo(x1, y1) else gearPath.lineTo(x1, y1)
                    gearPath.lineTo(x2, y2)
                    gearPath.lineTo(x3, y3)
                    gearPath.lineTo(x4, y4)
                }
                gearPath.close()
                canvas.drawPath(gearPath, fillPaint)
                canvas.drawCircle(cx, cy, rMax * 0.72f, clearPaint)

                // Inner spokes & botanical petals
                paint.strokeWidth = size * 0.032f
                canvas.drawCircle(cx, cy, rMax * 0.60f, paint)
                for (i in 0 until 8) {
                    val angle = (i * PI / 4.0).toFloat()
                    val xEnd = cx + rMax * 0.72f * cos(angle)
                    val yEnd = cy + rMax * 0.72f * sin(angle)
                    canvas.drawLine(cx, cy, xEnd, yEnd, paint)

                    val px = cx + rMax * 0.42f * cos(angle + (PI / 8.0).toFloat())
                    val py = cy + rMax * 0.42f * sin(angle + (PI / 8.0).toFloat())
                    canvas.drawCircle(px, py, rMax * 0.11f, paint)
                }
                canvas.drawCircle(cx, cy, rMax * 0.24f, fillPaint)
                canvas.drawCircle(cx, cy, rMax * 0.11f, clearPaint)
            }

            SampleArtworkPreset.WOLF_EMBLEM -> {
                // Hexagonal shield border
                paint.strokeWidth = size * 0.035f
                val hex = Path()
                for (i in 0 until 6) {
                    val a = (i * PI / 3.0 - PI / 2.0).toFloat()
                    val x = cx + rMax * cos(a)
                    val y = cy + rMax * sin(a)
                    if (i == 0) hex.moveTo(x, y) else hex.lineTo(x, y)
                }
                hex.close()
                canvas.drawPath(hex, paint)

                // Geometric wolf head facets
                paint.strokeWidth = size * 0.025f
                val lines = listOf(
                    // Ears
                    floatArrayOf(cx - rMax * 0.50f, cy - rMax * 0.15f, cx - rMax * 0.38f, cy - rMax * 0.65f),
                    floatArrayOf(cx - rMax * 0.38f, cy - rMax * 0.65f, cx - rMax * 0.15f, cy - rMax * 0.35f),
                    floatArrayOf(cx + rMax * 0.50f, cy - rMax * 0.15f, cx + rMax * 0.38f, cy - rMax * 0.65f),
                    floatArrayOf(cx + rMax * 0.38f, cy - rMax * 0.65f, cx + rMax * 0.15f, cy - rMax * 0.35f),
                    // Brow & Crown
                    floatArrayOf(cx - rMax * 0.15f, cy - rMax * 0.35f, cx + rMax * 0.15f, cy - rMax * 0.35f),
                    floatArrayOf(cx - rMax * 0.50f, cy - rMax * 0.15f, cx - rMax * 0.56f, cy + rMax * 0.22f),
                    floatArrayOf(cx + rMax * 0.50f, cy - rMax * 0.15f, cx + rMax * 0.56f, cy + rMax * 0.22f),
                    // Cheeks to snout
                    floatArrayOf(cx - rMax * 0.56f, cy + rMax * 0.22f, cx, cy + rMax * 0.72f),
                    floatArrayOf(cx + rMax * 0.56f, cy + rMax * 0.22f, cx, cy + rMax * 0.72f),
                    // Inner snout bridge
                    floatArrayOf(cx - rMax * 0.15f, cy - rMax * 0.35f, cx, cy + rMax * 0.45f),
                    floatArrayOf(cx + rMax * 0.15f, cy - rMax * 0.35f, cx, cy + rMax * 0.45f),
                    floatArrayOf(cx - rMax * 0.50f, cy - rMax * 0.15f, cx - rMax * 0.18f, cy + rMax * 0.12f),
                    floatArrayOf(cx + rMax * 0.50f, cy - rMax * 0.15f, cx + rMax * 0.18f, cy + rMax * 0.12f),
                    floatArrayOf(cx - rMax * 0.18f, cy + rMax * 0.12f, cx, cy + rMax * 0.72f),
                    floatArrayOf(cx + rMax * 0.18f, cy + rMax * 0.12f, cx, cy + rMax * 0.72f)
                )
                for (seg in lines) {
                    canvas.drawLine(seg[0], seg[1], seg[2], seg[3], paint)
                }
                // Nose triangle
                val nose = Path().apply {
                    moveTo(cx - rMax * 0.14f, cy + rMax * 0.44f)
                    lineTo(cx + rMax * 0.14f, cy + rMax * 0.44f)
                    lineTo(cx, cy + rMax * 0.68f)
                    close()
                }
                canvas.drawPath(nose, fillPaint)
            }

            SampleArtworkPreset.SPINDLE_BADGE -> {
                paint.strokeWidth = size * 0.036f
                // Outer octagonal plate
                val oct = Path()
                for (i in 0 until 8) {
                    val a = (i * PI / 4.0 + PI / 8.0).toFloat()
                    val x = cx + rMax * cos(a)
                    val y = cy + rMax * sin(a)
                    if (i == 0) oct.moveTo(x, y) else oct.lineTo(x, y)
                }
                oct.close()
                canvas.drawPath(oct, paint)

                // Corner mounting holes
                val holeOffset = rMax * 0.68f
                val holeR = size * 0.035f
                canvas.drawCircle(cx - holeOffset, cy - holeOffset * 0.4f, holeR, fillPaint)
                canvas.drawCircle(cx + holeOffset, cy - holeOffset * 0.4f, holeR, fillPaint)
                canvas.drawCircle(cx - holeOffset, cy + holeOffset * 0.4f, holeR, fillPaint)
                canvas.drawCircle(cx + holeOffset, cy + holeOffset * 0.4f, holeR, fillPaint)

                // Laser nozzle silhouette in center
                val nozzle = Path().apply {
                    moveTo(cx - rMax * 0.25f, cy - rMax * 0.55f)
                    lineTo(cx + rMax * 0.25f, cy - rMax * 0.55f)
                    lineTo(cx + rMax * 0.25f, cy - rMax * 0.20f)
                    lineTo(cx + rMax * 0.08f, cy + rMax * 0.08f)
                    lineTo(cx - rMax * 0.08f, cy + rMax * 0.08f)
                    lineTo(cx - rMax * 0.25f, cy - rMax * 0.20f)
                    close()
                }
                canvas.drawPath(nozzle, fillPaint)

                // Laser spark rays
                paint.strokeWidth = size * 0.028f
                canvas.drawLine(cx, cy + rMax * 0.08f, cx, cy + rMax * 0.55f, paint)
                canvas.drawLine(cx, cy + rMax * 0.45f, cx - rMax * 0.35f, cy + rMax * 0.22f, paint)
                canvas.drawLine(cx, cy + rMax * 0.45f, cx + rMax * 0.35f, cy + rMax * 0.22f, paint)
                canvas.drawLine(cx - rMax * 0.45f, cy + rMax * 0.55f, cx + rMax * 0.45f, cy + rMax * 0.55f, paint)
            }

            SampleArtworkPreset.FILIGREE_COASTER -> {
                paint.strokeWidth = size * 0.032f
                canvas.drawCircle(cx, cy, rMax, paint)
                canvas.drawCircle(cx, cy, rMax * 0.88f, paint)

                // Interlocking lotus rings
                val ringCount = 12
                for (i in 0 until ringCount) {
                    val angle = (i * 2.0 * PI / ringCount).toFloat()
                    val rx = cx + rMax * 0.46f * cos(angle)
                    val ry = cy + rMax * 0.46f * sin(angle)
                    canvas.drawCircle(rx, ry, rMax * 0.32f, paint)
                }
                canvas.drawCircle(cx, cy, rMax * 0.16f, fillPaint)
            }
        }
        return bitmap
    }

    /**
     * Runs real-time thresholding, optional color inversion, optional Sobel edge detection,
     * and contour vectorization on the source bitmap.
     */
    fun processArtwork(
        source: Bitmap,
        config: VectorOptimizerConfig
    ): VectorPreviewResult {
        val scaled = Bitmap.createScaledBitmap(source, PROCESS_SIZE, PROCESS_SIZE, true)
        val w = scaled.width
        val h = scaled.height
        val pixels = IntArray(w * h)
        scaled.getPixels(pixels, 0, w, 0, 0, w, h)

        // Step 1: Luminance + Threshold + Invert
        val binary = BooleanArray(w * h)
        val threshold = config.threshold.coerceIn(1, 254)
        for (i in pixels.indices) {
            val c = pixels[i]
            val a = (c ushr 24) and 0xFF
            val r = (c ushr 16) and 0xFF
            val g = (c ushr 8) and 0xFF
            val b = c and 0xFF
            val lum = if (a < 40) 255 else ((0.299f * r + 0.587f * g + 0.114f * b).roundToInt())
            var isDark = lum < threshold
            if (config.invertColors) {
                isDark = !isDark
            }
            binary[i] = isDark
        }

        // Step 2: Optional Despeckle (remove isolated single pixels)
        val cleaned = BooleanArray(w * h)
        if (config.speckleSuppression > 0) {
            for (y in 1 until h - 1) {
                for (x in 1 until w - 1) {
                    val idx = y * w + x
                    var neighbors = 0
                    for (dy in -1..1) {
                        for (dx in -1..1) {
                            if (dx == 0 && dy == 0) continue
                            if (binary[(y + dy) * w + (x + dx)]) neighbors++
                        }
                    }
                    cleaned[idx] = if (binary[idx]) {
                        neighbors >= config.speckleSuppression.coerceAtMost(4)
                    } else {
                        neighbors >= 7
                    }
                }
            }
        } else {
            System.arraycopy(binary, 0, cleaned, 0, binary.size)
        }

        // Step 3: Edge extraction (boundary pixels where foreground meets background)
        val edgeMap = BooleanArray(w * h)
        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                val idx = y * w + x
                if (cleaned[idx]) {
                    val isBoundary = !cleaned[(y - 1) * w + x] ||
                        !cleaned[(y + 1) * w + x] ||
                        !cleaned[y * w + (x - 1)] ||
                        !cleaned[y * w + (x + 1)]
                    edgeMap[idx] = isBoundary
                }
            }
        }

        // Step 4: Render preview bitmap showing thresholded raster or edge map
        val outPixels = IntArray(w * h)
        val activeColor = Color.rgb(15, 23, 42) // Deep slate ink
        val edgeColor = Color.rgb(239, 68, 68)   // Laser hairline red
        val bgColor = Color.rgb(248, 250, 252)

        for (i in outPixels.indices) {
            outPixels[i] = when {
                config.edgeDetection && edgeMap[i] -> edgeColor
                !config.edgeDetection && cleaned[i] -> activeColor
                else -> bgColor
            }
        }
        val previewBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        previewBitmap.setPixels(outPixels, 0, w, 0, 0, w, h)

        // Step 5: Trace boundary chains into continuous vector polylines
        val visited = BooleanArray(w * h)
        val contours = mutableListOf<List<Pair<Float, Float>>>()
        val dirsX = intArrayOf(1, 1, 0, -1, -1, -1, 0, 1)
        val dirsY = intArrayOf(0, 1, 1, 1, 0, -1, -1, -1)

        val stride = if (config.pathSmoothing > 0.6f) 3 else if (config.pathSmoothing > 0.25f) 2 else 1

        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                val startIdx = y * w + x
                if (edgeMap[startIdx] && !visited[startIdx]) {
                    val chain = mutableListOf<Pair<Float, Float>>()
                    var cx = x
                    var cy = y
                    var step = 0
                    while (step < 600) {
                        val currIdx = cy * w + cx
                        visited[currIdx] = true
                        if (step % stride == 0) {
                            chain.add((cx.toFloat() / w.toFloat()) to (cy.toFloat() / h.toFloat()))
                        }
                        var foundNext = false
                        for (d in 0 until 8) {
                            val nx = cx + dirsX[d]
                            val ny = cy + dirsY[d]
                            if (nx in 1 until (w - 1) && ny in 1 until (h - 1)) {
                                val nIdx = ny * w + nx
                                if (edgeMap[nIdx] && !visited[nIdx]) {
                                    cx = nx
                                    cy = ny
                                    foundNext = true
                                    break
                                }
                            }
                        }
                        if (!foundNext) break
                        step++
                    }
                    if (chain.size >= 4) {
                        contours.add(chain)
                    }
                }
            }
        }

        // Compute telemetry metrics
        var nodeCount = 0
        var totalNormalizedLen = 0f
        for (path in contours) {
            nodeCount += path.size
            for (i in 1 until path.size) {
                val dx = path[i].first - path[i - 1].first
                val dy = path[i].second - path[i - 1].second
                totalNormalizedLen += hypot(dx, dy)
            }
        }
        val cutLengthMm = (totalNormalizedLen * config.targetWidthMm).coerceAtLeast(12f)
        val estimatedSeconds = max(4, (cutLengthMm / 22f).roundToInt())

        return VectorPreviewResult(
            processedBitmap = previewBitmap,
            contourPaths = contours,
            nodeCount = nodeCount,
            closedLoopCount = contours.size,
            totalCutLengthMm = cutLengthMm,
            estimatedLaserSeconds = estimatedSeconds
        )
    }

    /**
     * Generates a production-ready SVG file string optimized for LightBurn, Silhouette, Glowforge, or CNC.
     */
    fun generateCleanSvg(
        preview: VectorPreviewResult,
        config: VectorOptimizerConfig,
        artworkTitle: String
    ): String {
        val sizeMm = config.targetWidthMm
        val strokeHex = config.softwarePreset.strokeColorHex
        val strokeWidth = config.softwarePreset.strokeWidthMm

        val sb = StringBuilder()
        sb.appendLine("""<?xml version="1.0" encoding="UTF-8" standalone="no"?>""")
        sb.appendLine("""<!-- Generated by LaserForge Studio • Target: ${config.softwarePreset.labelEn} -->""")
        sb.appendLine("""<!-- Artwork: $artworkTitle | Loops: ${preview.closedLoopCount} | Nodes: ${preview.nodeCount} -->""")
        sb.appendLine(
            String.format(
                Locale.US,
                """<svg xmlns="http://www.w3.org/2000/svg" width="%.2fmm" height="%.2fmm" viewBox="0 0 %.2f %.2f">""",
                sizeMm, sizeMm, sizeMm, sizeMm
            )
        )
        sb.appendLine(
            String.format(
                Locale.US,
                """  <g id="laser_cut_layer" fill="none" stroke="%s" stroke-width="%.3f" stroke-linecap="round" stroke-linejoin="round">""",
                strokeHex, strokeWidth
            )
        )

        for ((index, contour) in preview.contourPaths.withIndex()) {
            if (contour.size < 2) continue
            val pathData = StringBuilder()
            for ((ptIdx, pt) in contour.withIndex()) {
                val xMm = pt.first * sizeMm
                val yMm = pt.second * sizeMm
                if (ptIdx == 0) {
                    pathData.append(String.format(Locale.US, "M %.2f %.2f", xMm, yMm))
                } else {
                    pathData.append(String.format(Locale.US, " L %.2f %.2f", xMm, yMm))
                }
            }
            // Close loop if endpoints are within 3.5mm
            val first = contour.first()
            val last = contour.last()
            val dist = hypot((first.first - last.first) * sizeMm, (first.second - last.second) * sizeMm)
            if (dist < 3.5f) {
                pathData.append(" Z")
            }
            sb.appendLine("""    <path id="path_$index" d="$pathData" />""")
        }

        sb.appendLine("  </g>")
        sb.appendLine("</svg>")
        return sb.toString()
    }
}

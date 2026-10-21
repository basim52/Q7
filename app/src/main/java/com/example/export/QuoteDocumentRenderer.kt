package com.example.export

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import com.example.model.SavedWorkshopQuote
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Renders a client quote as a PDF page (A4) or a PNG image, using the workshop name as letterhead.
 * Drawn with android.graphics so Arabic shaping and RTL layout are handled by the platform.
 */
object QuoteDocumentRenderer {

    private const val NAVY = 0xFF0B1120.toInt()
    private const val CYAN = 0xFF22D3EE.toInt()
    private const val ORANGE = 0xFFF97316.toInt()
    private const val GREEN = 0xFF10B981.toInt()
    private const val GREY = 0xFF64748B.toInt()
    private const val INK = 0xFF0F172A.toInt()

    private fun rows(q: SavedWorkshopQuote, ar: Boolean): List<Pair<String, String>> {
        val c = q.currency
        val list = mutableListOf<Pair<String, String>>()
        if (q.itemWidthMm > 0f && q.itemLengthMm > 0f) {
            list += (if (ar) "مقاس القطعة" else "Part size") to
                (if (ar) "${q.itemWidthMm.toInt()}×${q.itemLengthMm.toInt()} مم" else "${q.itemWidthMm.toInt()}×${q.itemLengthMm.toInt()} mm")
        }
        list += (if (ar) "الكمية" else "Quantity") to (if (ar) "${q.batchQuantity} قطعة" else "${q.batchQuantity} pcs")
        list += (if (ar) "سعر القطعة" else "Unit price") to c.format(q.retailPrice, ar)
        return list
    }

    private fun costRows(q: SavedWorkshopQuote, ar: Boolean): List<Pair<String, String>> {
        val c = q.currency
        return listOf(
            (if (ar) "تكلفة الخامة / قطعة" else "Material / unit") to c.format(q.materialCost, ar),
            (if (ar) "تشغيل الليزر / قطعة" else "Laser time / unit") to c.format(q.machineCost, ar),
            (if (ar) "الكهرباء / قطعة" else "Electricity / unit") to c.format(q.electricityCost, ar),
            (if (ar) "التجهيز / قطعة" else "Setup / unit") to c.format(q.setupCost, ar),
            (if (ar) "إجمالي التكلفة / قطعة" else "Total cost / unit") to c.format(q.unitCost, ar)
        )
    }

    /**
     * Draws the document at logical width 595 (A4 points), scaled by [scale]. Returns logical height used.
     * [includeCosts] adds the internal cost breakdown (off by default: client copy shows price only).
     */
    private fun draw(canvas: Canvas?, q: SavedWorkshopQuote, workshopName: String, ar: Boolean, scale: Float, includeCosts: Boolean): Float {
        val w = 595f
        val margin = 40f
        val inner = w - margin * 2
        canvas?.save()
        canvas?.scale(scale, scale)
        val dir = if (ar) TextDirectionHeuristics.RTL else TextDirectionHeuristics.LTR

        fun layout(text: String, size: Float, color: Int, bold: Boolean, width: Float, align: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL): StaticLayout {
            val p = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = size; this.color = color; isFakeBoldText = bold
            }
            return StaticLayout.Builder.obtain(text, 0, text.length, p, width.toInt())
                .setAlignment(align).setTextDirection(dir).build()
        }
        fun drawAt(l: StaticLayout, x: Float, y: Float) {
            canvas?.save(); canvas?.translate(x, y); canvas?.let { l.draw(it) }; canvas?.restore()
        }

        // Header band
        val bandH = 92f
        canvas?.drawRect(0f, 0f, w, bandH, Paint().apply { color = NAVY })
        canvas?.drawRect(0f, bandH, w, bandH + 4f, Paint().apply { color = ORANGE })
        val name = workshopName.ifBlank { if (ar) "ورشة الليزر" else "Laser Workshop" }
        drawAt(layout(name, 22f, Color.WHITE, true, inner), margin, 20f)
        drawAt(layout(if (ar) "عرض سعر" else "Price Quote", 13f, CYAN, false, inner), margin, 54f)

        var y = bandH + 28f
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(q.createdAtMillis))
        drawAt(layout((if (ar) "التاريخ: " else "Date: ") + date, 11f, GREY, false, inner), margin, y)
        y += 22f
        val title = layout(q.projectName, 17f, INK, true, inner)
        drawAt(title, margin, y)
        y += title.height + 16f

        val linePaint = Paint().apply { color = 0xFFE2E8F0.toInt(); strokeWidth = 1f }
        fun rowsBlock(items: List<Pair<String, String>>, size: Float) {
            for ((label, value) in items) {
                val l = layout(label, size, GREY, false, inner)
                val v = layout(value, size, INK, true, inner, Layout.Alignment.ALIGN_OPPOSITE)
                drawAt(l, margin, y); drawAt(v, margin, y)
                y += maxOf(l.height, v.height) + 10f
                canvas?.drawLine(margin, y - 5f, w - margin, y - 5f, linePaint)
            }
        }
        rowsBlock(rows(q, ar), 13f)

        // Total box
        y += 10f
        val boxH = 58f
        canvas?.drawRoundRect(RectF(margin, y, w - margin, y + boxH), 10f, 10f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFECFDF5.toInt() })
        val total = q.retailPrice * q.batchQuantity.coerceAtLeast(1)
        drawAt(layout(if (ar) "الإجمالي" else "Total", 14f, INK, true, inner - 32f), margin + 16f, y + 19f)
        drawAt(layout(q.currency.format(total, ar), 20f, GREEN, true, inner - 32f, Layout.Alignment.ALIGN_OPPOSITE), margin + 16f, y + 15f)
        y += boxH + 24f

        if (includeCosts) {
            drawAt(layout(if (ar) "تفاصيل التكلفة الداخلية" else "Internal cost breakdown", 12f, ORANGE, true, inner), margin, y)
            y += 24f
            rowsBlock(costRows(q, ar), 11f)
            y += 8f
        }

        val footer = layout(
            if (ar) "الأسعار تقديرية وقد تتغير حسب التصميم النهائي والخامة." else "Prices are estimates and may change with the final design and material.",
            10f, GREY, false, inner
        )
        drawAt(footer, margin, y)
        y += footer.height + margin
        canvas?.restore()
        return y
    }

    fun renderPdf(q: SavedWorkshopQuote, workshopName: String, ar: Boolean, includeCosts: Boolean = false): ByteArray {
        val doc = PdfDocument()
        val height = maxOf(842f, draw(null, q, workshopName, ar, 1f, includeCosts)).toInt()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(595, height, 1).create())
        page.canvas.drawColor(Color.WHITE)
        draw(page.canvas, q, workshopName, ar, 1f, includeCosts)
        doc.finishPage(page)
        val out = ByteArrayOutputStream()
        doc.writeTo(out)
        doc.close()
        return out.toByteArray()
    }

    fun renderPng(q: SavedWorkshopQuote, workshopName: String, ar: Boolean, includeCosts: Boolean = false): ByteArray {
        val scale = 2f
        val h = draw(null, q, workshopName, ar, scale, includeCosts)
        val bmp = Bitmap.createBitmap((595 * scale).toInt(), (h * scale).toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.WHITE)
        draw(canvas, q, workshopName, ar, scale, includeCosts)
        val out = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
        bmp.recycle()
        return out.toByteArray()
    }

    fun safeFileBase(q: SavedWorkshopQuote): String {
        val base = q.projectName.replace(Regex("[^\\p{L}\\p{N}]+"), "_").trim('_').take(40)
        return "quote_" + base.ifEmpty { "laser" }
    }
}

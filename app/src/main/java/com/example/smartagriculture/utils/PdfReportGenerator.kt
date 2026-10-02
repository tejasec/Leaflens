package com.example.smartagriculture.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * PDF Report Data Model containing scan details and treatments.
 */
data class DiagnosticDossier(
    val cropSpecies: String,
    val diseaseName: String,
    val calibratedConfidence: Float,
    val healthIndexScore: Float,
    val organicTreatment: String,
    val chemicalTreatment: String,
    val originalLeafImage: Bitmap?,
    val gradCamOverlayImage: Bitmap?,
)

/**
 * Diagnostic PDF Dossier Generator & Exporter (Feature 11).
 * Generates an A4 PDF report with side-by-side leaf captures, telemetry metrics,
 * treatment advisories, and non-diagnostic legal disclaimers, with one-tap WhatsApp / Email sharing.
 */
object PdfReportGenerator {

    private const val A4_WIDTH = 595  // A4 width in points (72 dpi)
    private const val A4_HEIGHT = 842 // A4 height in points (72 dpi)

    /**
     * Generates a standard A4 PDF document file in app cache directory.
     */
    fun generatePdfReport(context: Context, dossier: DiagnosticDossier): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 18f
            isFakeBoldText = true
            color = Color.rgb(16, 185, 129) // Emerald primary
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 13f
            isFakeBoldText = true
            color = Color.rgb(17, 24, 39)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 10f
            color = Color.rgb(55, 65, 81)
        }
        val disclaimerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            color = Color.rgb(107, 114, 128)
        }

        var y = 40f

        // 1. Header Banner
        canvas.drawText("BAI-03 Edge AI Crop Health Diagnostic Dossier", 40f, y, titlePaint)
        y += 20f

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        canvas.drawText("Generated on: ${dateFormat.format(Date())}", 40f, y, bodyPaint)
        y += 25f

        // Horizontal Line
        paint.color = Color.rgb(229, 231, 235)
        paint.strokeWidth = 1f
        canvas.drawLine(40f, y, (A4_WIDTH - 40).toFloat(), y, paint)
        y += 20f

        // 2. Crop Metrics & Summary
        canvas.drawText("DIAGNOSTIC SUMMARY", 40f, y, headerPaint)
        y += 18f
        canvas.drawText("• Crop Species: ${dossier.cropSpecies}", 50f, y, bodyPaint)
        y += 15f
        canvas.drawText("• Primary Diagnosis: ${dossier.diseaseName}", 50f, y, bodyPaint)
        y += 15f
        canvas.drawText("• Calibrated Confidence: ${String.format(Locale.US, "%.1f%%", dossier.calibratedConfidence * 100f)}", 50f, y, bodyPaint)
        y += 15f
        canvas.drawText("• Crop Health Index Score: ${String.format(Locale.US, "%.1f / 100", dossier.healthIndexScore)}", 50f, y, bodyPaint)
        y += 30f

        // 3. Embedded Leaf Captures Side-by-Side (Original vs Grad-CAM)
        canvas.drawText("SALIENCY & FEATURE ATTRITION MAPS", 40f, y, headerPaint)
        y += 15f

        val imgWidth = 220
        val imgHeight = 180

        // Draw Original Leaf Capture
        dossier.originalLeafImage?.let { orig ->
            val srcRect = Rect(0, 0, orig.width, orig.height)
            val destRect = Rect(40, y.toInt(), 40 + imgWidth, y.toInt() + imgHeight)
            canvas.drawBitmap(orig, srcRect, destRect, paint)
            canvas.drawText("Original Leaf Capture", 70f, y + imgHeight + 15f, bodyPaint)
        }

        // Draw Grad-CAM Saliency Overlay
        dossier.gradCamOverlayImage?.let { gradCam ->
            val srcRect = Rect(0, 0, gradCam.width, gradCam.height)
            val destRect = Rect(290, y.toInt(), 290 + imgWidth, y.toInt() + imgHeight)
            canvas.drawBitmap(gradCam, srcRect, destRect, paint)
            canvas.drawText("Grad-CAM Saliency Map", 320f, y + imgHeight + 15f, bodyPaint)
        }

        y += imgHeight + 35f

        // 4. Prescribed Treatment Advisory
        canvas.drawText("PRESCRIBED TREATMENT ADVISORY", 40f, y, headerPaint)
        y += 18f
        canvas.drawText("🌱 Organic / Bio-control:", 50f, y, bodyPaint)
        y += 14f
        canvas.drawText(dossier.organicTreatment.take(120), 60f, y, bodyPaint)
        y += 20f

        canvas.drawText("🧪 Chemical Solution:", 50f, y, bodyPaint)
        y += 14f
        canvas.drawText(dossier.chemicalTreatment.take(120), 60f, y, bodyPaint)
        y += 35f

        // 5. Mandatory Non-Diagnostic Legal Disclaimer
        paint.color = Color.rgb(229, 231, 235)
        canvas.drawLine(40f, y, (A4_WIDTH - 40).toFloat(), y, paint)
        y += 18f

        canvas.drawText("MANDATORY NON-DIAGNOSTIC LEGAL DISCLAIMER:", 40f, y, disclaimerPaint)
        y += 12f
        canvas.drawText("BAI-03 is an AI-assisted diagnostic tool designed for preliminary field guidance only.", 40f, y, disclaimerPaint)
        y += 10f
        canvas.drawText("Results must be verified by a certified agronomist before applying large-scale chemical treatments.", 40f, y, disclaimerPaint)

        document.finishPage(page)

        // Save PDF file to app cache directory
        val pdfFile = File(context.cacheDir, "Crop_Diagnostic_Report_${System.currentTimeMillis()}.pdf")
        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return pdfFile
    }

    /**
     * Shares the generated PDF dossier file via WhatsApp, Email, or System Share Sheet.
     */
    fun sharePdfReport(context: Context, pdfFile: File) {
        val authority = "${context.packageName}.fileprovider"
        val contentUri = FileProvider.getUriForFile(context, authority, pdfFile)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "Crop Health Diagnostic Report (Leaflens AI)")
            putExtra(Intent.EXTRA_TEXT, "Attached is the AI-generated Crop Health Diagnostic Dossier for field review.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Share Diagnostic Report via")
        context.startActivity(chooserIntent)
    }
}

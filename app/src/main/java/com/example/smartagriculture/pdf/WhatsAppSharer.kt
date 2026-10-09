package com.example.smartagriculture.pdf

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object WhatsAppSharer {

    fun shareDossierViaWhatsApp(context: Context, pdfFile: File, payload: DiagnosisPayload) {
        val authority = "${context.packageName}.fileprovider"
        val contentUri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

        val captionMessage = if (payload.isHealthy) {
            "🌱 *LeafLens AI Crop Health Record*\n" +
            "Crop: ${payload.cropName}\n" +
            "Status: Healthy\n" +
            "Confidence: ${(payload.confidenceScore * 100).toInt()}%\n\n" +
            "Attached is the official diagnostic PDF dossier."
        } else {
            "🚨 *LeafLens AI Diagnostic Advisory*\n" +
            "Crop: ${payload.cropName}\n" +
            "Detected Disease: ${payload.diseaseName}\n" +
            "Confidence: ${(payload.confidenceScore * 100).toInt()}%\n" +
            "Primary Remedy: ${payload.organicRemedy}\n\n" +
            "Attached is the official diagnostic PDF dossier."
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_SUBJECT, "LeafLens AI Crop Diagnostic Report")
            putExtra(Intent.EXTRA_TEXT, captionMessage)
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.whatsapp") // Directly target WhatsApp
        }

        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback to general system share dialog if WhatsApp is not installed
            val chooserIntent = Intent.createChooser(intent, "Share Diagnostic Dossier via")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
        }
    }
}

package com.example.smartagriculture.fragments

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.smartagriculture.R
import com.example.smartagriculture.database.AppDatabase
import com.example.smartagriculture.databinding.FragmentScanResultBinding
import com.example.smartagriculture.model.DiseaseAnalysisResult
import com.example.smartagriculture.model.ScanHistoryItem
import com.example.smartagriculture.utils.DiagnosticDossier
import com.example.smartagriculture.utils.PdfReportGenerator
import com.example.smartagriculture.utils.VoiceAssistantService
import com.example.smartagriculture.utils.VoiceDiagnosisResult
import kotlinx.coroutines.launch

class ScanResultFragment : Fragment(R.layout.fragment_scan_result) {

    private var binding: FragmentScanResultBinding? = null
    private var imageUriStr: String? = null
    private var analysisResult: DiseaseAnalysisResult? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScanResultBinding.bind(view)

        imageUriStr = arguments?.getString("imageUri")
        @Suppress("DEPRECATION")
        analysisResult = arguments?.getSerializable("analysisResult") as? DiseaseAnalysisResult

        val result = analysisResult ?: DiseaseAnalysisResult(
            diseaseName = "Tomato Early Blight",
            scientificName = "Alternaria solani",
            confidence = 87,
            isLowConfidence = false,
            aiExplanation = "The highlighted regions show brown concentric spots on leaves indicating fungal infection typical of Early Blight.",
            organicCare = "• Remove affected leaves and dispose of them properly\n• Improve air circulation and avoid overhead watering\n• Use neem oil spray (5 ml per litre of water)\n• Apply compost to improve soil health",
            chemicalCare = "• Apply copper-based fungicide at initial symptom appearance\n• Spray Chlorothalonil or Mancozeb at 7-10 day intervals\n• Rotate fungicide classes to prevent resistance"
        )

        // Load leaf image
        if (!imageUriStr.isNullOrBlank()) {
            binding?.ivResultLeaf?.let {
                Glide.with(this)
                    .load(Uri.parse(imageUriStr))
                    .placeholder(R.drawable.bg_1)
                    .into(it)
            }
        }

        // Bind data
        binding?.tvDiseaseName?.text = result.diseaseName
        binding?.tvScientificName?.text = result.scientificName
        binding?.tvConfidenceValue?.text = "${result.confidence}%"
        binding?.pbConfidence?.progress = result.confidence
        binding?.tvAiExplanation?.text = result.aiExplanation

        binding?.btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        binding?.btnViewHeatmap?.setOnClickListener {
            val bundle = bundleOf(
                "imageUri" to imageUriStr,
                "analysisResult" to result
            )
            findNavController().navigate(R.id.action_scanResultFragment_to_scanHeatmapFragment, bundle)
        }

        binding?.btnViewCareGuide?.setOnClickListener {
            val bundle = bundleOf("analysisResult" to result)
            findNavController().navigate(R.id.action_scanResultFragment_to_scanCareGuideFragment, bundle)
        }

        // Voice Doctor Assistant (Feature 13)
        val voiceService = VoiceAssistantService(requireContext())
        binding?.btnVoiceDoctor?.setOnClickListener {
            val voiceResult = VoiceDiagnosisResult(
                diseaseName = result.diseaseName,
                severityGrade = "Grade 2 (Moderate)",
                healthScore = (100 - result.confidence).toFloat().coerceIn(0f, 100f),
                organicTreatment = result.organicCare.take(80)
            )
            voiceService.speakDiagnosis(voiceResult)
            Toast.makeText(requireContext(), "Voice Doctor reading diagnosis aloud...", Toast.LENGTH_SHORT).show()
        }

        // Dual-Track Advisory Sheet & Wiki (Features 2, 3, 4)
        binding?.btnOpenAdvisory?.setOnClickListener {
            val sheet = AdvisorySheetDialogFragment.newInstance("early_blight")
            sheet.show(parentFragmentManager, "AdvisorySheet")
        }

        // PDF Dossier Export & Share (Feature 11)
        binding?.btnExportPdf?.setOnClickListener {
            val leafBitmap = loadBitmapForPdf(imageUriStr)
            val dossier = DiagnosticDossier(
                cropSpecies = "Tomato",
                diseaseName = result.diseaseName,
                calibratedConfidence = result.confidence / 100.0f,
                healthIndexScore = (100 - result.confidence).toFloat().coerceIn(0f, 100f),
                organicTreatment = result.organicCare,
                chemicalTreatment = result.chemicalCare,
                originalLeafImage = leafBitmap,
                gradCamOverlayImage = leafBitmap
            )
            try {
                val pdfFile = PdfReportGenerator.generatePdfReport(requireContext(), dossier)
                PdfReportGenerator.sharePdfReport(requireContext(), pdfFile)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error exporting PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }

        binding?.btnSaveHistory?.setOnClickListener {
            saveToHistory(result)
        }

        binding?.btnScanAgain?.setOnClickListener {
            findNavController().popBackStack(R.id.scanFragment, false)
        }
    }

    private fun saveToHistory(result: DiseaseAnalysisResult) {
        val historyItem = ScanHistoryItem(
            imagePath = imageUriStr ?: "",
            diseaseName = result.diseaseName,
            scientificName = result.scientificName,
            confidence = result.confidence,
            isLowConfidence = result.isLowConfidence,
            aiExplanation = result.aiExplanation,
            organicCare = result.organicCare,
            chemicalCare = result.chemicalCare
        )

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            db.scanHistoryDao().insertScan(historyItem)
            Toast.makeText(requireContext(), "Saved to scan history!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadBitmapForPdf(uriStr: String?): Bitmap? {
        if (uriStr.isNullOrBlank()) return null
        return try {
            val uri = Uri.parse(uriStr)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(requireContext().contentResolver, uri)
                ) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

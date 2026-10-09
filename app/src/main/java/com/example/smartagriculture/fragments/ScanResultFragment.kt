package com.example.smartagriculture.fragments

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.smartagriculture.R
import com.example.smartagriculture.audio.AudioDossierManager
import com.example.smartagriculture.audio.AudioState
import com.example.smartagriculture.database.AppDatabase
import com.example.smartagriculture.databinding.FragmentScanResultBinding
import com.example.smartagriculture.ml.GradCamEngine
import com.example.smartagriculture.model.ChatMessage
import com.example.smartagriculture.model.DiseaseAnalysisResult
import com.example.smartagriculture.model.ScanHistoryItem
import com.example.smartagriculture.pdf.DiagnosisPayload
import com.example.smartagriculture.pdf.WhatsAppSharer
import com.example.smartagriculture.utils.DiagnosticDossier
import com.example.smartagriculture.utils.ImageStorageManager
import com.example.smartagriculture.utils.PdfReportGenerator
import com.google.gson.Gson
import kotlinx.coroutines.launch

class ScanResultFragment : Fragment(R.layout.fragment_scan_result) {

    private var binding: FragmentScanResultBinding? = null
    private var imageUriStr: String? = null
    private var analysisResult: DiseaseAnalysisResult? = null
    private var savedScanId: Long? = null
    private val currentChatMessages = mutableListOf<ChatMessage>()

    private var audioDossierManager: AudioDossierManager? = null
    private var activeLanguageCode: String = "hi"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScanResultBinding.bind(view)

        audioDossierManager = AudioDossierManager(requireContext())

        // Read saved app language or default to Hindi
        val prefs = requireContext().getSharedPreferences("smart_agri_prefs", 0)
        activeLanguageCode = prefs.getString("selected_language", "hi") ?: "hi"
        updateLanguageChipsUI(activeLanguageCode)

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
        binding?.ivResultLeaf?.let { iv ->
            if (!imageUriStr.isNullOrBlank()) {
                val model = ImageStorageManager.getImageModel(imageUriStr) ?: imageUriStr
                Glide.with(this)
                    .load(model)
                    .placeholder(R.drawable.rounded_button)
                    .error(R.drawable.rounded_button)
                    .into(iv)
            } else {
                iv.setImageResource(R.drawable.rounded_button)
            }
        }

        // Display crop and disease name
        val displayTitle = if (result.cropName.isNotBlank() && !result.diseaseName.startsWith(result.cropName, ignoreCase = true)) {
            "${result.cropName} - ${result.diseaseName}"
        } else {
            result.diseaseName
        }
        binding?.tvDiseaseName?.text = displayTitle
        binding?.tvScientificName?.text = result.scientificName
        binding?.tvConfidenceValue?.text = "${result.confidence}%"
        binding?.pbConfidence?.progress = result.confidence
        binding?.tvAiExplanation?.text = result.aiExplanation

        // Status badge configuration
        if (result.isNewFinding) {
            binding?.tvBadge?.text = "New Finding"
            binding?.tvBadge?.setTextColor(Color.parseColor("#A78BFA"))
            binding?.tvBadge?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#338B5CF6"))
        } else if (result.diseaseName.contains("healthy", ignoreCase = true)) {
            binding?.tvBadge?.text = "Healthy"
            binding?.tvBadge?.setTextColor(Color.parseColor("#10B981"))
            binding?.tvBadge?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#3310B981"))
        } else if (result.isLowConfidence || result.confidence < 70) {
            binding?.tvBadge?.text = "Uncertain"
            binding?.tvBadge?.setTextColor(Color.parseColor("#F59E0B"))
            binding?.tvBadge?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#33F59E0B"))
        } else {
            binding?.tvBadge?.text = "Disease Detected"
            binding?.tvBadge?.setTextColor(Color.parseColor("#EF4444"))
            binding?.tvBadge?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#33EF4444"))
        }

        binding?.btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        binding?.btnViewHeatmap?.setOnClickListener {
            @Suppress("UNCHECKED_CAST")
            val activationMatrix = result.activationMatrix
                ?: (arguments?.getSerializable("activationMatrix") as? Array<FloatArray>)

            val bundle = bundleOf(
                "imageUri" to imageUriStr,
                "analysisResult" to result,
                "activationMatrix" to activationMatrix,
            )
            findNavController().navigate(R.id.action_scanResultFragment_to_scanHeatmapFragment, bundle)
        }

        binding?.btnViewCareGuide?.setOnClickListener {
            val bundle = bundleOf("analysisResult" to result)
            findNavController().navigate(R.id.action_scanResultFragment_to_scanCareGuideFragment, bundle)
        }

        // Multilingual Voice Doctor Assistant (Feature 13)
        setupVoiceDoctorSection(result)

        // Dual-Track Advisory Sheet & Wiki (Features 2, 3, 4)
        binding?.btnOpenAdvisory?.setOnClickListener {
            val sheet = AdvisorySheetDialogFragment.newInstance("early_blight")
            sheet.show(parentFragmentManager, "AdvisorySheet")
        }

        // WhatsApp PDF Dossier Sharing (Feature 8 & 14)
        binding?.btnShareWhatsapp?.setOnClickListener {
            val payload = DiagnosisPayload(
                cropName = if (result.cropName.isNotBlank()) result.cropName else "Crop",
                diseaseName = result.diseaseName,
                confidenceScore = result.confidence / 100f,
                isHealthy = result.diseaseName.contains("healthy", ignoreCase = true),
                organicRemedy = result.organicCare,
                chemicalRemedy = result.chemicalCare
            )
            try {
                val pdfFile = PdfReportGenerator.generateDiagnosticDossier(requireContext(), payload)
                WhatsAppSharer.shareDossierViaWhatsApp(requireContext(), pdfFile, payload)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error sharing to WhatsApp: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }

        // PDF Dossier Export & System Share Sheet (Feature 11)
        binding?.btnExportPdf?.setOnClickListener {
            val leafBitmap = loadBitmapForPdf(imageUriStr)
            @Suppress("UNCHECKED_CAST")
            val activationMatrix = result.activationMatrix
                ?: (arguments?.getSerializable("activationMatrix") as? Array<FloatArray>)
                ?: leafBitmap?.let { GradCamEngine.generateActivationMatrix(it) }

            val gradCamOverlay = if (leafBitmap != null && activationMatrix != null) {
                GradCamEngine.createBlendedHeatmapBitmap(leafBitmap, activationMatrix, 0.70f)
            } else {
                leafBitmap
            }

            val crop = if (result.cropName.isNotBlank()) result.cropName else "Crop"
            val dossier = DiagnosticDossier(
                cropSpecies = crop,
                diseaseName = result.diseaseName,
                calibratedConfidence = result.confidence / 100.0f,
                healthIndexScore = (100 - result.confidence).toFloat().coerceIn(0f, 100f),
                organicTreatment = result.organicCare,
                chemicalTreatment = result.chemicalCare,
                originalLeafImage = leafBitmap,
                gradCamOverlayImage = gradCamOverlay
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

        // Ask Crop Doctor AI Chatbot (BottomSheet)
        binding?.btnAskCropDoctor?.setOnClickListener {
            val bottomSheet = AskCropDoctorBottomSheet.newInstance(
                diseaseName = result.diseaseName,
                scientificName = result.scientificName,
                confidence = result.confidence,
                organicCare = result.organicCare,
                chemicalCare = result.chemicalCare,
                scanId = savedScanId ?: 0L,
                chatHistoryJson = if (currentChatMessages.isNotEmpty()) Gson().toJson(currentChatMessages) else null
            )
            bottomSheet.onChatUpdated = { updatedMessages ->
                currentChatMessages.clear()
                currentChatMessages.addAll(updatedMessages)
            }
            bottomSheet.show(childFragmentManager, "AskCropDoctorBottomSheet")
        }
    }

    private fun setupVoiceDoctorSection(result: DiseaseAnalysisResult) {
        // Setup language chips click listeners
        binding?.chipLangEn?.setOnClickListener { selectAudioLanguage("en") }
        binding?.chipLangHi?.setOnClickListener { selectAudioLanguage("hi") }
        binding?.chipLangMr?.setOnClickListener { selectAudioLanguage("mr") }

        // Observe TTS playback state flow
        viewLifecycleOwner.lifecycleScope.launch {
            audioDossierManager?.playbackState?.collect { state ->
                when (state) {
                    AudioState.PLAYING -> {
                        binding?.btnVoiceDoctor?.text = "Stop Voice Advisory / आवाज़ रोकें"
                        binding?.btnVoiceDoctor?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#C62828"))
                        binding?.btnVoiceDoctor?.setIconResource(R.drawable.ic_stop)
                        binding?.tvAudioStatus?.visibility = View.VISIBLE
                        val langUpper = when (activeLanguageCode) {
                            "hi" -> "HINDI"
                            "mr" -> "MARATHI"
                            else -> "ENGLISH"
                        }
                        binding?.tvAudioStatus?.text = "🔊 Reading diagnostic advisory in $langUpper…"
                    }
                    AudioState.ERROR -> {
                        binding?.btnVoiceDoctor?.text = "Audio Unavailable / पुनः प्रयास करें"
                        binding?.btnVoiceDoctor?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F59E0B"))
                        binding?.btnVoiceDoctor?.setIconResource(R.drawable.ic_volume_up)
                        binding?.tvAudioStatus?.visibility = View.GONE
                    }
                    AudioState.IDLE -> {
                        binding?.btnVoiceDoctor?.text = "Play Voice Advisory / आवाज़ सुनें"
                        binding?.btnVoiceDoctor?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#10B981"))
                        binding?.btnVoiceDoctor?.setIconResource(R.drawable.ic_volume_up)
                        binding?.tvAudioStatus?.visibility = View.GONE
                    }
                }
            }
        }

        // Toggle Audio action
        binding?.btnVoiceDoctor?.setOnClickListener {
            val isPlaying = audioDossierManager?.playbackState?.value == AudioState.PLAYING
            if (isPlaying) {
                audioDossierManager?.stopAudio()
            } else {
                val crop = if (result.cropName.isNotBlank()) result.cropName else "Crop"
                audioDossierManager?.speakDossier(
                    cropName = crop,
                    diseaseName = result.diseaseName,
                    confidencePct = result.confidence,
                    organicRemedy = result.organicCare,
                    languageCode = activeLanguageCode
                )
            }
        }
    }

    private fun selectAudioLanguage(langCode: String) {
        activeLanguageCode = langCode
        updateLanguageChipsUI(langCode)
        // If already playing, re-speak in new language
        if (audioDossierManager?.playbackState?.value == AudioState.PLAYING) {
            analysisResult?.let { res ->
                val crop = if (res.cropName.isNotBlank()) res.cropName else "Crop"
                audioDossierManager?.speakDossier(
                    cropName = crop,
                    diseaseName = res.diseaseName,
                    confidencePct = res.confidence,
                    organicRemedy = res.organicCare,
                    languageCode = activeLanguageCode
                )
            }
        }
    }

    private fun updateLanguageChipsUI(selectedCode: String) {
        val isEn = selectedCode.equals("en", ignoreCase = true)
        val isHi = selectedCode.equals("hi", ignoreCase = true)
        val isMr = selectedCode.equals("mr", ignoreCase = true)

        setChipState(binding?.chipLangEn, isEn)
        setChipState(binding?.chipLangHi, isHi)
        setChipState(binding?.chipLangMr, isMr)
    }

    private fun setChipState(chip: TextView?, isSelected: Boolean) {
        if (chip == null) return
        if (isSelected) {
            chip.setBackgroundResource(R.drawable.bg_chip_selected)
            chip.setTextColor(Color.parseColor("#0B1711"))
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_unselected)
            chip.setTextColor(Color.parseColor("#FFFFFF"))
        }
    }

    private fun saveToHistory(result: DiseaseAnalysisResult) {
        val status = if (result.isNewFinding) {
            "New Finding"
        } else if (result.isLowConfidence || result.confidence < 70) {
            "Uncertain"
        } else if (result.diseaseName.contains("healthy", ignoreCase = true)) {
            "Healthy"
        } else {
            "Diseased"
        }

        val persistentImagePath = ImageStorageManager.persistScanImage(requireContext(), imageUriStr)

        val historyItem = ScanHistoryItem(
            imagePath = persistentImagePath,
            cropName = if (result.cropName.isNotBlank()) result.cropName else "Crop",
            diseaseName = result.diseaseName,
            status = status,
            scientificName = result.scientificName,
            confidence = result.confidence,
            isLowConfidence = result.isLowConfidence || result.confidence < 70,
            aiExplanation = result.aiExplanation,
            organicCare = result.organicCare,
            chemicalCare = result.chemicalCare,
            chatHistoryJson = if (currentChatMessages.isNotEmpty()) Gson().toJson(currentChatMessages) else null
        )

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            savedScanId = db.scanHistoryDao().insertScan(historyItem)
            binding?.btnSaveHistory?.text = "✓ Saved to History"
            val chatMsg = if (currentChatMessages.isNotEmpty()) " with ${currentChatMessages.size} consultation messages" else ""
            Toast.makeText(requireContext(), "Saved to scan history$chatMsg!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadBitmapForPdf(uriStr: String?): Bitmap? {
        if (uriStr.isNullOrBlank()) return null
        if (uriStr.startsWith("/")) {
            val file = java.io.File(uriStr)
            if (file.exists()) {
                return BitmapFactory.decodeFile(uriStr)
            }
        }
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

    override fun onPause() {
        super.onPause()
        audioDossierManager?.stopAudio()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        audioDossierManager?.shutdown()
        audioDossierManager = null
        binding = null
    }
}

package com.example.smartagriculture.fragments

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.smartagriculture.R
import com.example.smartagriculture.analysis.SeverityAnalyzer
import com.example.smartagriculture.database.AppDatabase
import com.example.smartagriculture.databinding.FragmentScanAnalyzingBinding
import com.example.smartagriculture.ml.CropHealthClassifier
import com.example.smartagriculture.ml.FeatureEmbeddingExtractor
import com.example.smartagriculture.ml.GradCamEngine
import com.example.smartagriculture.model.DiseaseAnalysisResult
import com.example.smartagriculture.network.GeminiService
import com.example.smartagriculture.repository.FewShotRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Dedicated Analysis Processing Screen (Step 2 of New Workflow).
 * Triggered ONLY when the user clicks "ANALYZE LEAF ➔" from the validation screen.
 * Displays progress checklist and executes TFLite/Gemini disease model inference.
 */
class ScanAnalyzingFragment : Fragment(R.layout.fragment_scan_analyzing) {

    private var binding: FragmentScanAnalyzingBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScanAnalyzingBinding.bind(view)

        val imageUriStr = arguments?.getString("imageUri")

        // Load thumbnail preview
        if (!imageUriStr.isNullOrBlank()) {
            binding?.ivAnalyzingLeaf?.let {
                Glide.with(this)
                    .load(Uri.parse(imageUriStr))
                    .placeholder(R.drawable.bg_1)
                    .into(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Load bitmap
            val bitmap = loadBitmapFromUri(imageUriStr)

            // Step 1: Animate Characteristics Step
            delay(400)
            binding?.icStep1?.text = "✓"
            binding?.tvStep1?.setTextColor(Color.parseColor("#10B981"))

            // Step 2: Edge TFLite Classifier Inference (Features 7 & 10)
            delay(400)
            val tfliteClassifier = CropHealthClassifier(requireContext())
            val classificationResult = tfliteClassifier.classifyImage(bitmap)
            binding?.icStep2?.text = "✓"
            binding?.tvStep2?.setTextColor(Color.parseColor("#10B981"))

            // Step 3: Lesion Severity & Health Index Calculation (Feature 1)
            delay(400)
            val severityResult = SeverityAnalyzer.analyzeSeverity(bitmap)
            binding?.icStep3?.text = "✓"
            binding?.tvStep3?.setTextColor(Color.parseColor("#10B981"))

            // Step 4: Explainability Grad-CAM Generation & Results Assembly
            delay(400)
            val activationMatrix = GradCamEngine.generateActivationMatrix(bitmap)
            val isBypassed = arguments?.getBoolean("bypassQualityGate", false) ?: arguments?.getBoolean("humanOverride", false) ?: false

            // Check On-Device Few-Shot Prototypes for Novel / Uncataloged Pathogen (Feature 6)
            val fewShotDao = AppDatabase.getDatabase(requireContext().applicationContext).prototypeDao()
            val fewShotRepo = FewShotRepository(fewShotDao)
            val queryEmbedding = FeatureEmbeddingExtractor.extractEmbedding(bitmap)
            val fewShotMatch = fewShotRepo.classifyFewShot(queryEmbedding, threshold = 0.82)

            var isLowConfidenceRoute = false
            val baseResult = if (fewShotMatch != null) {
                // Matched a locally enrolled pathogen!
                val scorePct = (fewShotMatch.similarityScore * 100).toInt()
                DiseaseAnalysisResult(
                    diseaseName = fewShotMatch.className,
                    scientificName = "Locally Enrolled Pathogen",
                    confidence = scorePct,
                    isLowConfidence = false,
                    aiExplanation = "Diagnosed via on-device Few-Shot prototype (${String.format(java.util.Locale.US, "%.1f%%", fewShotMatch.similarityScore * 100)} similarity). ${severityResult.summary}",
                    organicCare = "• Isolate infected crop foliage and sanitize field tools\n• Apply preventative organic bio-agent (Trichoderma / Neem extract)",
                    chemicalCare = "• Consult local agrarian extension officer (KVK) for localized chemical spray guidelines",
                    activationMatrix = activationMatrix,
                )
            } else if (classificationResult.predictions.isNotEmpty() && !classificationResult.isUncertain) {
                val topDiagnosis = classificationResult.predictions[0]
                DiseaseAnalysisResult(
                    diseaseName = topDiagnosis.label,
                    scientificName = "Pathogen species",
                    confidence = (topDiagnosis.confidence * 100).toInt(),
                    isLowConfidence = false,
                    aiExplanation = "${classificationResult.feedbackMessage} ${severityResult.summary}",
                    organicCare = "• Neem oil extract (3%) or Trichoderma viride\n• Prune infected foliage and improve canopy airflow",
                    chemicalCare = "• Mancozeb 75% WP or Copper Oxychloride 50% WP (2.5g/L water)\n• Observe 7-day PHI and 24-hr REI safety intervals",
                    activationMatrix = activationMatrix,
                )
            } else {
                // Uncertain / uncataloged disease -> Check cloud or route to low confidence screen
                isLowConfidenceRoute = true
                try {
                    GeminiService.analyzeCropDisease(bitmap).copy(activationMatrix = activationMatrix)
                } catch (e: Exception) {
                    val label = if (classificationResult.predictions.isNotEmpty()) classificationResult.predictions[0].label else "Uncataloged Pathogen"
                    val conf = if (classificationResult.predictions.isNotEmpty()) (classificationResult.predictions[0].confidence * 100).toInt() else 35
                    DiseaseAnalysisResult(
                        diseaseName = "$label (Uncertain)",
                        scientificName = "Unconfirmed pathogen",
                        confidence = conf,
                        isLowConfidence = true,
                        aiExplanation = "Uncertain diagnosis: The model could not confidently identify this disease with edge models. Enroll it under 'Enroll Pathogen' to teach the app.",
                        organicCare = "• Isolate affected leaves to prevent spread\n• Avoid overhead irrigation",
                        chemicalCare = "• Consult an agronomist before spraying broad-spectrum fungicides",
                        activationMatrix = activationMatrix,
                    )
                }
            }
            tfliteClassifier.close()

            val result = if (isBypassed) {
                baseResult.copy(
                    aiExplanation = "Quality checks bypassed by user. ${baseResult.aiExplanation}"
                )
            } else {
                baseResult
            }

            binding?.icStep4?.text = "✓"
            binding?.tvStep4?.setTextColor(Color.parseColor("#10B981"))

            delay(300)
            val bundle = bundleOf(
                "imageUri" to imageUriStr,
                "analysisResult" to result,
                "activationMatrix" to activationMatrix,
            )

            // If diagnosis is uncertain/low confidence, navigate to ScanLowConfidenceFragment (which offers "Enroll as New Pathogen ➔")
            if (isLowConfidenceRoute && result.isLowConfidence) {
                findNavController().navigate(R.id.action_scanAnalyzingFragment_to_scanLowConfidenceFragment, bundle)
            } else {
                findNavController().navigate(R.id.action_scanAnalyzingFragment_to_scanResultFragment, bundle)
            }
        }
    }

    private fun loadBitmapFromUri(uriStr: String?): Bitmap {
        if (uriStr.isNullOrBlank()) {
            return createFallbackBitmap()
        }
        return try {
            val uri = Uri.parse(uriStr)
            val decoded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().contentResolver, uri)) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            }
            if (decoded.config == Bitmap.Config.HARDWARE) {
                decoded.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                decoded
            }
        } catch (e: Exception) {
            createFallbackBitmap()
        }
    }

    private fun createFallbackBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_1)
        drawable?.setBounds(0, 0, canvas.width, canvas.height)
        drawable?.draw(canvas)
        return bitmap
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

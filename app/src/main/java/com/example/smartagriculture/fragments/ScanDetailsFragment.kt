package com.example.smartagriculture.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.smartagriculture.R
import com.example.smartagriculture.database.AppDatabase
import com.example.smartagriculture.databinding.DialogRemoveHistoryBinding
import com.example.smartagriculture.databinding.FragmentScanDetailsBinding
import com.example.smartagriculture.model.ChatMessage
import com.example.smartagriculture.model.DiseaseAnalysisResult
import com.example.smartagriculture.model.ScanHistoryItem
import com.example.smartagriculture.utils.ImageStorageManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanDetailsFragment : Fragment(R.layout.fragment_scan_details) {

    private var binding: FragmentScanDetailsBinding? = null
    private var scanItem: ScanHistoryItem? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScanDetailsBinding.bind(view)

        @Suppress("DEPRECATION")
        scanItem = arguments?.getSerializable("scanItem") as? ScanHistoryItem

        val item = scanItem ?: return

        // Timestamp
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        binding?.tvScanTimestamp?.text = "Scanned on ${sdf.format(Date(item.timestamp))}"

        // Leaf Image
        if (item.imagePath.isNotBlank()) {
            binding?.ivDetailLeaf?.let {
                val model = ImageStorageManager.getImageModel(item.imagePath) ?: item.imagePath
                Glide.with(this)
                    .load(model)
                    .placeholder(R.drawable.rounded_button)
                    .error(R.drawable.rounded_button)
                    .into(it)
            }
        }

        // Details
        binding?.tvCropName?.text = item.cropName
        binding?.tvDiseaseName?.text = item.diseaseName
        binding?.tvScientificName?.text = item.scientificName
        binding?.tvConfidenceValue?.text = "${item.confidence}%"
        binding?.pbConfidence?.progress = item.confidence
        binding?.tvAiExplanation?.text = item.aiExplanation

        // Status badge
        val status = item.status.ifBlank {
            if (item.isLowConfidence) "Uncertain"
            else if (item.diseaseName.contains("Healthy", ignoreCase = true)) "Healthy"
            else "Diseased"
        }
        binding?.tvStatusBadge?.text = status
        when (status) {
            "Healthy" -> {
                binding?.tvStatusBadge?.setTextColor(Color.parseColor("#10B981"))
                binding?.tvStatusBadge?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#3310B981"))
            }
            "Uncertain" -> {
                binding?.tvStatusBadge?.setTextColor(Color.parseColor("#F59E0B"))
                binding?.tvStatusBadge?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#33F59E0B"))
            }
            "New Finding" -> {
                binding?.tvStatusBadge?.setTextColor(Color.parseColor("#A78BFA"))
                binding?.tvStatusBadge?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#338B5CF6"))
            }
            else -> {
                binding?.tvStatusBadge?.setTextColor(Color.parseColor("#EF4444"))
                binding?.tvStatusBadge?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#33EF4444"))
            }
        }

        // Setup Chat Consultation Card
        setupChatCard(item)

        binding?.btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        val analysisResult = DiseaseAnalysisResult(
            cropName = item.cropName,
            diseaseName = item.diseaseName,
            scientificName = item.scientificName,
            confidence = item.confidence,
            isLowConfidence = item.isLowConfidence,
            isNewFinding = item.status.equals("New Finding", ignoreCase = true),
            aiExplanation = item.aiExplanation,
            organicCare = item.organicCare,
            chemicalCare = item.chemicalCare
        )

        binding?.btnViewHeatmap?.setOnClickListener {
            val bundle = bundleOf(
                "imageUri" to item.imagePath,
                "analysisResult" to analysisResult
            )
            findNavController().navigate(R.id.action_scanDetailsFragment_to_scanHeatmapFragment, bundle)
        }

        binding?.btnViewCareGuide?.setOnClickListener {
            val bundle = bundleOf("analysisResult" to analysisResult)
            findNavController().navigate(R.id.action_scanDetailsFragment_to_scanCareGuideFragment, bundle)
        }

        binding?.btnRecapture?.setOnClickListener {
            val bundle = bundleOf(
                "isRecapture" to true,
                "scanItem" to item
            )
            findNavController().navigate(R.id.action_scanDetailsFragment_to_recaptureCameraFragment, bundle)
        }

        binding?.btnRemoveFromHistory?.setOnClickListener {
            showRemoveDialog(item)
        }
    }

    private fun setupChatCard(item: ScanHistoryItem) {
        val messages = parseChatHistory(item.chatHistoryJson)
        if (messages.isNotEmpty()) {
            binding?.tvChatSummary?.text = "${messages.size} consultation messages saved • Tap to revisit"
            binding?.btnOpenChatHistory?.text = "💬 Revisit Chat"
            val lastMsg = messages.lastOrNull()
            if (lastMsg != null) {
                val sender = if (lastMsg.isUser) "You" else "Doctor"
                binding?.tvChatLastMessage?.text = "$sender: ${lastMsg.message.take(120)}"
                binding?.tvChatLastMessage?.visibility = View.VISIBLE
            } else {
                binding?.tvChatLastMessage?.visibility = View.GONE
            }
        } else {
            binding?.tvChatSummary?.text = "No consultation saved yet. Ask about treatments, dosage, and care."
            binding?.btnOpenChatHistory?.text = "💬 Ask Doctor"
            binding?.tvChatLastMessage?.visibility = View.GONE
        }

        binding?.btnOpenChatHistory?.setOnClickListener {
            val bottomSheet = AskCropDoctorBottomSheet.newInstance(
                diseaseName = item.diseaseName,
                scientificName = item.scientificName,
                confidence = item.confidence,
                organicCare = item.organicCare,
                chemicalCare = item.chemicalCare,
                scanId = item.id,
                chatHistoryJson = item.chatHistoryJson
            )
            bottomSheet.onChatUpdated = { updatedMessages ->
                val updatedJson = Gson().toJson(updatedMessages)
                val updatedItem = item.copy(chatHistoryJson = updatedJson)
                scanItem = updatedItem
                setupChatCard(updatedItem)
            }
            bottomSheet.show(childFragmentManager, "AskCropDoctorBottomSheet")
        }
    }

    private fun parseChatHistory(json: String?): List<ChatMessage> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<ChatMessage>>() {}.type
            Gson().fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun showRemoveDialog(item: ScanHistoryItem) {
        val dialogView = DialogRemoveHistoryBinding.inflate(layoutInflater)
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView.root)
            .setCancelable(true)
            .create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.btnCloseDialog.setOnClickListener { alertDialog.dismiss() }
        dialogView.btnCancelDelete.setOnClickListener { alertDialog.dismiss() }

        dialogView.btnConfirmDelete.setOnClickListener {
            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(requireContext())
                db.scanHistoryDao().deleteScan(item.id)
                Toast.makeText(requireContext(), "Scan removed from history", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
                findNavController().navigateUp()
            }
        }

        alertDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

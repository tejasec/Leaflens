package com.example.smartagriculture.fragments

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.smartagriculture.R
import com.example.smartagriculture.database.AppDatabase
import com.example.smartagriculture.databinding.FragmentReplaceHistoryBinding
import com.example.smartagriculture.model.ScanHistoryItem
import com.example.smartagriculture.utils.ImageStorageManager
import kotlinx.coroutines.launch

class ReplaceHistoryFragment : Fragment(R.layout.fragment_replace_history) {

    private var binding: FragmentReplaceHistoryBinding? = null
    private var scanItem: ScanHistoryItem? = null
    private var newImageUriStr: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentReplaceHistoryBinding.bind(view)

        newImageUriStr = arguments?.getString("newImageUri")
        @Suppress("DEPRECATION")
        scanItem = arguments?.getSerializable("scanItem") as? ScanHistoryItem

        val item = scanItem

        // Load Old Image
        if (item != null && item.imagePath.isNotBlank()) {
            binding?.ivOldImage?.let {
                val model = ImageStorageManager.getImageModel(item.imagePath) ?: item.imagePath
                Glide.with(this)
                    .load(model)
                    .placeholder(R.drawable.rounded_button)
                    .error(R.drawable.rounded_button)
                    .into(it)
            }
        }

        // Load New Image
        if (!newImageUriStr.isNullOrBlank()) {
            binding?.ivNewImage?.let {
                val model = ImageStorageManager.getImageModel(newImageUriStr) ?: newImageUriStr
                Glide.with(this)
                    .load(model)
                    .placeholder(R.drawable.rounded_button)
                    .error(R.drawable.rounded_button)
                    .into(it)
            }
        }

        binding?.btnBack?.setOnClickListener { findNavController().navigateUp() }
        binding?.btnCancelReplace?.setOnClickListener { findNavController().navigateUp() }

        binding?.btnConfirmReplace?.setOnClickListener {
            if (item != null && !newImageUriStr.isNullOrBlank()) {
                val persistentPath = ImageStorageManager.persistScanImage(requireContext(), newImageUriStr)
                val updatedItem = item.copy(imagePath = persistentPath)
                lifecycleScope.launch {
                    val db = AppDatabase.getDatabase(requireContext())
                    db.scanHistoryDao().updateScan(updatedItem)
                    Toast.makeText(requireContext(), "Scan image replaced successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack(R.id.scanDetailsFragment, false)
                }
            } else {
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

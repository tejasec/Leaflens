package com.example.smartagriculture.fragments

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.smartagriculture.R
import com.example.smartagriculture.databinding.FragmentScanQualityBinding

class ScanQualityFragment : Fragment(R.layout.fragment_scan_quality) {

    private var binding: FragmentScanQualityBinding? = null
    private var imageUriStr: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScanQualityBinding.bind(view)

        imageUriStr = arguments?.getString("imageUri")

        if (!imageUriStr.isNullOrBlank()) {
            binding?.ivCapturedLeaf?.let {
                Glide.with(this)
                    .load(Uri.parse(imageUriStr))
                    .placeholder(R.drawable.bg_1)
                    .into(it)
            }
        } else {
            binding?.ivCapturedLeaf?.setImageResource(R.drawable.bg_1)
        }

        binding?.btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        binding?.btnEditImage?.setOnClickListener {
            findNavController().navigateUp()
        }

        binding?.btnAnalyzeLeaf?.setOnClickListener {
            val bundle = bundleOf("imageUri" to imageUriStr)
            findNavController().navigate(R.id.action_scanQualityFragment_to_scanAnalyzingFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

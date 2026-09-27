package com.example.smartagriculture.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.smartagriculture.R
import com.example.smartagriculture.databinding.FragmentMyCropsBinding
import com.example.smartagriculture.repository.CropRepository

class MyCropsFragment : Fragment(R.layout.fragment_my_crops) {

    private var binding: FragmentMyCropsBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMyCropsBinding.bind(view)

        renderMyCropsList()

        binding?.btnAddCrop?.setOnClickListener {
            findNavController().navigate(R.id.action_myCropsFragment_to_addToMyCropsFragment)
        }

        binding?.btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun renderMyCropsList() {
        binding?.containerMyCrops?.removeAllViews()
        val crops = CropRepository.getMyCrops()

        for (item in crops) {
            val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_crop_list, binding?.containerMyCrops, false)
            val tvTitle = itemView.findViewById<TextView>(R.id.tvCropTitle)
            val tvSub = itemView.findViewById<TextView>(R.id.tvCropScientific)
            val ivThumb = itemView.findViewById<ImageView>(R.id.ivCropThumb)

            tvTitle.text = item.cropName
            tvSub.text = "${item.status} • Planted: ${item.plantedDate}"
            ivThumb.setImageResource(item.imageRes)

            itemView.setOnClickListener {
                val bundle = bundleOf("myCrop" to item)
                findNavController().navigate(R.id.action_myCropsFragment_to_cropProgressFragment, bundle)
            }

            binding?.containerMyCrops?.addView(itemView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

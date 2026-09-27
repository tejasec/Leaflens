package com.example.smartagriculture.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartagriculture.R
import com.example.smartagriculture.adapter.CropListAdapter
import com.example.smartagriculture.databinding.FragmentAllCropsBinding
import com.example.smartagriculture.model.CropDetailItem
import com.example.smartagriculture.repository.CropRepository

class AllCropsFragment : Fragment(R.layout.fragment_all_crops) {

    private var binding: FragmentAllCropsBinding? = null
    private var adapter: CropListAdapter? = null
    private var selectedCategory: String = "All"
    private var searchQuery: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAllCropsBinding.bind(view)

        adapter = CropListAdapter(CropRepository.allCropsList) { cropItem ->
            val bundle = bundleOf("cropItem" to cropItem)
            findNavController().navigate(R.id.action_allCropsFragment_to_cropDetailsFragment, bundle)
        }

        binding?.rvAllCrops?.layoutManager = LinearLayoutManager(requireContext())
        binding?.rvAllCrops?.adapter = adapter

        setupPillListeners()

        binding?.etSearchCrops?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                filterCrops()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding?.btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupPillListeners() {
        binding?.pillAll?.setOnClickListener { setPill("All", binding?.pillAll) }
        binding?.pillCereals?.setOnClickListener { setPill("Cereals", binding?.pillCereals) }
        binding?.pillVegetables?.setOnClickListener { setPill("Vegetables", binding?.pillVegetables) }
        binding?.pillFruits?.setOnClickListener { setPill("Fruits", binding?.pillFruits) }
    }

    private fun setPill(category: String, selectedView: TextView?) {
        selectedCategory = category

        val unselectedBg = ColorStateList.valueOf(Color.parseColor("#1C241E"))

        binding?.pillAll?.backgroundTintList = unselectedBg
        binding?.pillAll?.setTextColor(Color.parseColor("#9CA3AF"))

        binding?.pillCereals?.backgroundTintList = unselectedBg
        binding?.pillCereals?.setTextColor(Color.parseColor("#9CA3AF"))

        binding?.pillVegetables?.backgroundTintList = unselectedBg
        binding?.pillVegetables?.setTextColor(Color.parseColor("#9CA3AF"))

        binding?.pillFruits?.backgroundTintList = unselectedBg
        binding?.pillFruits?.setTextColor(Color.parseColor("#9CA3AF"))

        selectedView?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#10B981"))
        selectedView?.setTextColor(Color.WHITE)

        filterCrops()
    }

    private fun filterCrops() {
        val filtered = CropRepository.allCropsList.filter { crop ->
            val matchesQuery = searchQuery.isEmpty() ||
                    crop.name.contains(searchQuery, ignoreCase = true) ||
                    crop.scientificName.contains(searchQuery, ignoreCase = true)

            val matchesCategory = when (selectedCategory) {
                "All" -> true
                else -> crop.category.equals(selectedCategory, ignoreCase = true)
            }

            matchesQuery && matchesCategory
        }
        adapter?.updateData(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

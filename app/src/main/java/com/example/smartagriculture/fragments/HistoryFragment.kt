package com.example.smartagriculture.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartagriculture.R
import com.example.smartagriculture.adapter.ScanHistoryAdapter
import com.example.smartagriculture.database.AppDatabase
import com.example.smartagriculture.databinding.FragmentHistoryBinding
import com.example.smartagriculture.model.ScanHistoryItem
import kotlinx.coroutines.launch

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private var binding: FragmentHistoryBinding? = null
    private var adapter: ScanHistoryAdapter? = null
    private var allScans: List<ScanHistoryItem> = emptyList()
    private var selectedFilter: String = "All" // "All", "Healthy", "Diseased", "Uncertain"
    private var searchQuery: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHistoryBinding.bind(view)

        adapter = ScanHistoryAdapter(emptyList()) { item ->
            val bundle = bundleOf("scanItem" to item)
            findNavController().navigate(R.id.action_historyFragment_to_scanDetailsFragment, bundle)
        }

        binding?.rvHistory?.layoutManager = LinearLayoutManager(requireContext())
        binding?.rvHistory?.adapter = adapter

        // Setup filter chips
        setupFilterChips()

        // Setup search
        binding?.etSearchHistory?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding?.btnHelp?.setOnClickListener {
            Toast.makeText(requireContext(), "Your saved scan history & disease diagnoses.", Toast.LENGTH_SHORT).show()
        }

        binding?.btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            db.scanHistoryDao().getAllScans().collect { scans ->
                allScans = scans
                applyFilters()
            }
        }
    }

    private fun setupFilterChips() {
        binding?.chipAll?.setOnClickListener { setFilter("All", binding?.chipAll) }
        binding?.chipHealthy?.setOnClickListener { setFilter("Healthy", binding?.chipHealthy) }
        binding?.chipDiseased?.setOnClickListener { setFilter("Diseased", binding?.chipDiseased) }
        binding?.chipUncertain?.setOnClickListener { setFilter("Uncertain", binding?.chipUncertain) }
    }

    private fun setFilter(filter: String, selectedChip: TextView?) {
        selectedFilter = filter

        // Reset chip styles
        val unselectedBg = ColorStateList.valueOf(Color.parseColor("#1C241E"))

        binding?.chipAll?.backgroundTintList = unselectedBg
        binding?.chipAll?.setTextColor(Color.parseColor("#9CA3AF"))

        binding?.chipHealthy?.backgroundTintList = unselectedBg
        binding?.chipHealthy?.setTextColor(Color.parseColor("#10B981"))

        binding?.chipDiseased?.backgroundTintList = unselectedBg
        binding?.chipDiseased?.setTextColor(Color.parseColor("#EF4444"))

        binding?.chipUncertain?.backgroundTintList = unselectedBg
        binding?.chipUncertain?.setTextColor(Color.parseColor("#F59E0B"))

        // Active selected style
        when (filter) {
            "All" -> {
                selectedChip?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#10B981"))
                selectedChip?.setTextColor(Color.WHITE)
            }
            "Healthy" -> {
                selectedChip?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#10B981"))
                selectedChip?.setTextColor(Color.WHITE)
            }
            "Diseased" -> {
                selectedChip?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EF4444"))
                selectedChip?.setTextColor(Color.WHITE)
            }
            "Uncertain" -> {
                selectedChip?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F59E0B"))
                selectedChip?.setTextColor(Color.WHITE)
            }
        }

        applyFilters()
    }

    private fun applyFilters() {
        val filtered = allScans.filter { item ->
            val matchesQuery = searchQuery.isEmpty() ||
                    item.diseaseName.contains(searchQuery, ignoreCase = true) ||
                    item.cropName.contains(searchQuery, ignoreCase = true)

            val itemStatus = item.status.ifBlank {
                if (item.isLowConfidence) "Uncertain"
                else if (item.diseaseName.contains("Healthy", ignoreCase = true)) "Healthy"
                else "Diseased"
            }

            val matchesFilter = when (selectedFilter) {
                "Healthy" -> itemStatus.equals("Healthy", ignoreCase = true)
                "Diseased" -> itemStatus.equals("Diseased", ignoreCase = true)
                "Uncertain" -> itemStatus.equals("Uncertain", ignoreCase = true)
                else -> true
            }

            matchesQuery && matchesFilter
        }

        if (filtered.isEmpty()) {
            binding?.tvEmptyState?.visibility = View.VISIBLE
            binding?.rvHistory?.visibility = View.GONE
        } else {
            binding?.tvEmptyState?.visibility = View.GONE
            binding?.rvHistory?.visibility = View.VISIBLE
            adapter?.updateData(filtered)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

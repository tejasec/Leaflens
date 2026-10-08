package com.example.smartagriculture.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.smartagriculture.R
import com.example.smartagriculture.adapter.DashboardAdapter
import com.example.smartagriculture.databinding.FragmentHomeBinding
import com.example.smartagriculture.model.DashboardItem

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var binding: FragmentHomeBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        val items = listOf(
            DashboardItem("Benchmark", R.drawable.ic_dash_benchmark, R.id.action_homeFragment_to_benchmarkGalleryFragment, "#3B82F6"),
            DashboardItem("Enroll Pathogen", R.drawable.ic_dash_enroll, R.id.action_homeFragment_to_enrollPathogenFragment, "#10B981"),
            DashboardItem("Schemes", R.drawable.ic_dash_schemes, R.id.action_homeFragment_to_schemesFragment, "#F59E0B"),
            DashboardItem("Soil Info", R.drawable.ic_dash_soil, R.id.action_homeFragment_to_soilFragment, "#10B981"),
            DashboardItem("Calendar", R.drawable.ic_dash_calendar, R.id.action_homeFragment_to_calendarFragment, "#EF4444"),
            DashboardItem("Fertilizer", R.drawable.ic_dash_fertilizer, R.id.action_homeFragment_to_fertilizerFragment, "#10B981"),
            DashboardItem("Pest Info", R.drawable.ic_dash_pest, R.id.action_homeFragment_to_pestFragment, "#F59E0B"),
            DashboardItem("Helpline", R.drawable.ic_dash_helpline, R.id.action_homeFragment_to_helplineFragment, "#06B6D4"),
            DashboardItem("Profile", R.drawable.ic_dash_profile, R.id.action_homeFragment_to_profileFragment, "#8B5CF6"),
            DashboardItem("Settings", R.drawable.ic_dash_settings, R.id.action_homeFragment_to_settingsFragment, "#3B82F6"),
        )

        binding?.rvDashboard?.layoutManager = GridLayoutManager(requireContext(), 3)
        binding?.rvDashboard?.adapter = DashboardAdapter(items) { item ->
            findNavController().navigate(item.navActionId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
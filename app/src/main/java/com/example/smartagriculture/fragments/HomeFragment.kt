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
            DashboardItem("Schemes", android.R.drawable.ic_menu_info_details, R.id.action_homeFragment_to_schemesFragment),
            DashboardItem("Soil Info", android.R.drawable.ic_menu_gallery, R.id.action_homeFragment_to_soilFragment),
            DashboardItem("Calendar", android.R.drawable.ic_menu_my_calendar, R.id.action_homeFragment_to_calendarFragment),
            DashboardItem("Fertilizer", android.R.drawable.ic_menu_add, R.id.action_homeFragment_to_fertilizerFragment),
            DashboardItem("Pest Info", android.R.drawable.ic_menu_report_image, R.id.action_homeFragment_to_pestFragment),
            DashboardItem("Helpline", android.R.drawable.ic_menu_call, R.id.action_homeFragment_to_helplineFragment),
            DashboardItem("Profile", android.R.drawable.ic_menu_my_calendar, R.id.action_homeFragment_to_profileFragment),
            DashboardItem("Settings", android.R.drawable.ic_menu_preferences, R.id.action_homeFragment_to_settingsFragment)
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
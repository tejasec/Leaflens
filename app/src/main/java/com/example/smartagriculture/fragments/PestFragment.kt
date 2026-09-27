package com.example.smartagriculture.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartagriculture.R
import com.example.smartagriculture.adapter.PestAdapter
import com.example.smartagriculture.model.Pest

class PestFragment : Fragment(R.layout.fragment_pest) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvPest = view.findViewById<RecyclerView>(R.id.rvPest)
        rvPest.layoutManager = LinearLayoutManager(requireContext())

        val pestList = listOf(
            Pest("Aphids", "Wheat, Cotton, Maize", "Apply Neem oil extract or insecticidal soap. Encourage natural predators like ladybugs."),
            Pest("Fall Armyworm", "Maize", "Use pheromone traps. Apply bio-pesticides or recommended chemical insecticides at early stage."),
            Pest("Stem Borer", "Rice, Sugarcane", "Remove and destroy dead hearts. Apply appropriate granular insecticides in the soil."),
            Pest("Whitefly", "Cotton, Vegetables", "Use yellow sticky traps. Spray appropriate systemic insecticides."),
            Pest("Locusts", "All crops", "Organize community-level trenching. Apply fast-acting chemical sprays recommended by local authorities.")
        )

        rvPest.adapter = PestAdapter(pestList)
    }
}
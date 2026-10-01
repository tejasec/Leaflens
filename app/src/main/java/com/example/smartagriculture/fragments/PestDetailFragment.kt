package com.example.smartagriculture.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.smartagriculture.R
import com.example.smartagriculture.model.Pest

class PestDetailFragment : Fragment(R.layout.fragment_pest_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        @Suppress("DEPRECATION")
        val pest = arguments?.getSerializable("pest") as? Pest

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val tvName = view.findViewById<TextView>(R.id.tvPestDetailName)
        val tvAffectedCrops = view.findViewById<TextView>(R.id.tvPestAffectedCrops)
        val tvSymptoms = view.findViewById<TextView>(R.id.tvPestSymptoms)
        val tvManagement = view.findViewById<TextView>(R.id.tvPestManagement)

        btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        pest?.let {
            tvName.text = it.pestName
            tvAffectedCrops.text = "Affected Crops: ${it.affectedCrops}"
            tvSymptoms.text = "Yellowing leaves, sap sucking damage, vector transmission of plant viruses."
            tvManagement.text = it.solution
        }
    }
}

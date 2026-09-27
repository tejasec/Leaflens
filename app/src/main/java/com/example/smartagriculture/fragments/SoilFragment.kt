package com.example.smartagriculture.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartagriculture.R
import com.example.smartagriculture.adapter.SoilAdapter
import com.example.smartagriculture.model.Soil

class SoilFragment : Fragment(R.layout.fragment_soil) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvSoil = view.findViewById<RecyclerView>(R.id.rvSoil)
        rvSoil.layoutManager = LinearLayoutManager(requireContext())

        val soilList = listOf(
            Soil("Alluvial Soil", "Highly fertile, rich in potash and lime but poor in nitrogen.", "Rice, Wheat, Sugarcane, Cotton"),
            Soil("Black Soil", "High moisture retention, rich in iron, lime, calcium, and magnesium.", "Cotton, Sugarcane, Millets, Tobacco"),
            Soil("Red Soil", "Rich in iron content, porous, lacks nitrogen and humus.", "Wheat, Cotton, Pulses, Tobacco, Oilseeds"),
            Soil("Laterite Soil", "Rich in iron and aluminum, poor in organic matter.", "Tea, Coffee, Rubber, Coconut, Cashew"),
            Soil("Arid/Desert Soil", "Sandy, low organic matter, highly alkaline.", "Millets, Barley, Maize (with irrigation)")
        )

        rvSoil.adapter = SoilAdapter(soilList)
    }
}
package com.example.smartagriculture.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartagriculture.R
import com.example.smartagriculture.adapter.SchemesAdapter
import com.example.smartagriculture.model.Scheme

class SchemesFragment : Fragment(R.layout.fragment_schemes) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val rvSchemes = view.findViewById<RecyclerView>(R.id.rvSchemes)
        rvSchemes.layoutManager = LinearLayoutManager(requireContext())
        
        val realSchemes = listOf(
            Scheme(
                "PM-Kisan Samman Nidhi", 
                "Direct income support of ₹6,000 per year to farmer families.", 
                "Small and marginal farmers",
                "Pradhan Mantri Kisan Samman Nidhi (PM-KISAN) is a Central Sector scheme with 100% funding from Government of India. Under the Scheme an income support of Rs.6000/- per year is provided to all farmer families across the country in three equal installments of Rs.2000/- each every four months.",
                "https://pmkisan.gov.in/"
            ),
            Scheme(
                "Pradhan Mantri Fasal Bima Yojana", 
                "Provides comprehensive crop insurance cover against non-preventable natural risks.", 
                "All farmers growing notified crops",
                "PMFBY aims to provide insurance coverage and financial support to the farmers in the event of failure of any of the notified crop as a result of natural calamities, pests & diseases. It helps stabilize the income of farmers to ensure their continuance in farming.",
                "https://pmfby.gov.in/"
            ),
            Scheme(
                "Paramparagat Krishi Vikas Yojana", 
                "Promotes organic farming through a cluster approach and PGS certification.", 
                "All farmers interested in organic farming",
                "PKVY is an elaborated component of Soil Health Management (SHM) of major project National Mission of Sustainable Agriculture (NMSA). Under PKVY Organic farming is promoted through adoption of organic village by cluster approach and PGS certification.",
                "https://pgsindia-ncof.gov.in/pkvy/index.aspx"
            ),
            Scheme(
                "Soil Health Card Scheme", 
                "Issues soil health cards to farmers to help them use fertilizers optimally.", 
                "All farmers",
                "The scheme aims at promoting soil test based and balanced use of fertilizers to enable farmers to realize higher yields at lower cost. The Soil Health Card contains the status of his soil with respect to 12 parameters.",
                "https://soilhealth.dac.gov.in/"
            ),
            Scheme(
                "Kisan Credit Card (KCC)", 
                "Provides timely credit support for cultivation and other needs.", 
                "Farmers, tenant farmers, sharecroppers",
                "The KCC scheme was introduced to ensure that farmers have easy and timely access to credit for their agricultural operations and other needs. It offers flexible repayment options and lower interest rates compared to regular loans.",
                "https://www.rbi.org.in/"
            )
        )
        
        rvSchemes.adapter = SchemesAdapter(realSchemes) { scheme ->
            val bundle = Bundle().apply {
                putSerializable("scheme", scheme)
            }
            findNavController().navigate(R.id.action_schemesFragment_to_schemeDetailFragment, bundle)
        }
    }
}
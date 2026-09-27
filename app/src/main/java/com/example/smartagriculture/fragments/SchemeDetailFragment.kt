package com.example.smartagriculture.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.smartagriculture.R
import com.example.smartagriculture.model.Scheme

class SchemeDetailFragment : Fragment(R.layout.fragment_scheme_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val scheme = arguments?.getSerializable("scheme") as? Scheme
        
        scheme?.let {
            view.findViewById<TextView>(R.id.tvDetailTitle).text = it.title
            view.findViewById<TextView>(R.id.tvDetailEligibility).text = "Eligibility: ${it.eligibility}"
            view.findViewById<TextView>(R.id.tvDetailFullDesc).text = it.fullDetails
            view.findViewById<TextView>(R.id.tvDetailLink).text = "Apply/More Info: ${it.link}"
        }
    }
}

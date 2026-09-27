package com.example.smartagriculture.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.smartagriculture.AppConfig
import com.example.smartagriculture.R

class MoreFragment : Fragment(R.layout.fragment_more) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.cardProfileSettings).setOnClickListener {
            findNavController().navigate(R.id.action_moreFragment_to_profileFragment)
        }

        view.findViewById<View>(R.id.cardHelpSupport).setOnClickListener {
            findNavController().navigate(R.id.action_moreFragment_to_helpSupportFragment)
        }

        view.findViewById<View>(R.id.cardAboutUs).setOnClickListener {
            findNavController().navigate(R.id.action_moreFragment_to_aboutFragment)
        }

        val btnLogout = view.findViewById<View>(R.id.btnLogout)
        if (!AppConfig.IS_AUTH_ENABLED) {
            btnLogout.visibility = View.GONE
        } else {
            btnLogout.visibility = View.VISIBLE
            btnLogout.setOnClickListener {
                // Clear SharedPreferences
                val prefs = requireContext().getSharedPreferences("smart_agri_prefs", Context.MODE_PRIVATE)
                prefs.edit().clear().apply()

                Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
                
                // Navigate to login
                findNavController().navigate(R.id.action_moreFragment_to_loginFragment)
            }
        }
    }
}

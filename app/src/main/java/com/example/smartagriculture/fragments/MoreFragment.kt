package com.example.smartagriculture.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.smartagriculture.R
import com.google.firebase.auth.FirebaseAuth

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
        btnLogout.visibility = View.VISIBLE
        btnLogout.setOnClickListener {
            // 1. Sign out of Firebase Auth to invalidate token
            try {
                FirebaseAuth.getInstance().signOut()
            } catch (e: Exception) {
                android.util.Log.e("MoreFragment", "Failed to sign out of Firebase", e)
            }

            // 2. Clear user session while preserving settings like selected language
            val prefs = requireContext().getSharedPreferences("smart_agri_prefs", Context.MODE_PRIVATE)
            val savedLanguage = prefs.getString("selected_language", null)

            prefs.edit().apply {
                remove("user_name")
                remove("user_email")
                remove("user_location")
                remove("user_crop")
                remove("user_password")
                putBoolean("is_logged_in", false)
                apply()
            }

            if (savedLanguage != null) {
                prefs.edit().putString("selected_language", savedLanguage).apply()
            }

            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            
            // 3. Clear backstack and route to Login
            findNavController().navigate(R.id.action_moreFragment_to_loginFragment)
        }
    }
}

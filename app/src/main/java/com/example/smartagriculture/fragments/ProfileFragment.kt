package com.example.smartagriculture.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.smartagriculture.R
import com.google.android.material.textfield.TextInputEditText

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val btnSave = view.findViewById<Button>(R.id.btnSaveProfile)
        val tvForgotPassword = view.findViewById<TextView>(R.id.tvForgotPassword)
        
        val etName = view.findViewById<TextInputEditText>(R.id.etProfileName)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etProfileEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etProfilePassword)
        
        val prefs = requireContext().getSharedPreferences("smart_agri_prefs", android.content.Context.MODE_PRIVATE)
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        
        // Load details from SharedPreferences or Firebase
        val savedName = prefs.getString("user_name", "")
        val savedEmail = currentUser?.email ?: prefs.getString("user_email", "")
        val savedPassword = prefs.getString("user_password", "")
        
        etName.setText(savedName)
        etEmail.setText(savedEmail)
        etPassword.setText(savedPassword)
        
        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            
            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                prefs.edit().apply {
                    putString("user_name", name)
                    putString("user_email", email)
                    putString("user_password", password)
                    apply()
                }
                Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
        
        tvForgotPassword.setOnClickListener {
            Toast.makeText(requireContext(), "Password reset link sent to your email.", Toast.LENGTH_SHORT).show()
        }
    }
}
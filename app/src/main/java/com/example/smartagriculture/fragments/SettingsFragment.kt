package com.example.smartagriculture.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.smartagriculture.R
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val switchNotifications = view.findViewById<SwitchMaterial>(R.id.switchNotifications)
        val btnResetData = view.findViewById<Button>(R.id.btnResetData)

        val prefs = requireContext().getSharedPreferences("smart_agri_prefs", Context.MODE_PRIVATE)

        switchNotifications?.isChecked = prefs.getBoolean("notifications_enabled", true)
        switchNotifications?.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            val msg = if (isChecked) "Push notifications enabled" else "Notifications muted"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        btnResetData?.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Reset Local Data?")
                .setMessage("This will permanently clear all saved scan history and local user preferences. Continue?")
                .setPositiveButton("Reset") { dialog, _ ->
                    prefs.edit().clear().apply()
                    Toast.makeText(requireContext(), "Local app data reset successfully.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}

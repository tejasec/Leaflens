package com.example.smartagriculture.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.smartagriculture.R
import com.example.smartagriculture.utils.ThemeManager
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val switchNotifications = view.findViewById<SwitchMaterial>(R.id.switchNotifications)
        val switchLightMode = view.findViewById<SwitchMaterial>(R.id.switchLightMode)
        val btnResetData = view.findViewById<Button>(R.id.btnResetData)

        val prefs = requireContext().getSharedPreferences(ThemeManager.PREFS_NAME, Context.MODE_PRIVATE)

        // Notifications Toggle
        switchNotifications?.isChecked = prefs.getBoolean("notifications_enabled", true)
        switchNotifications?.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            val msg = if (isChecked) "Push notifications enabled" else "Notifications muted"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        // Light Mode Toggle (Default is false: Dark/Green mode)
        val isCurrentlyLight = ThemeManager.isLightMode(requireContext())
        switchLightMode?.isChecked = isCurrentlyLight
        switchLightMode?.setOnCheckedChangeListener { _, isChecked ->
            if (ThemeManager.isLightMode(requireContext()) != isChecked) {
                ThemeManager.setLightMode(requireContext(), isChecked)
                val msg = if (isChecked) "Light mode enabled" else "Dark/green theme restored"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                activity?.recreate()
            }
        }

        // Reset Local App Data
        btnResetData?.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Reset Local Data?")
                .setMessage("This will permanently clear all saved scan history and local user preferences. Continue?")
                .setPositiveButton("Reset") { dialog, _ ->
                    val wasLight = ThemeManager.isLightMode(requireContext())
                    prefs.edit().clear().apply()
                    Toast.makeText(requireContext(), "Local app data reset successfully.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    if (wasLight) {
                        activity?.recreate()
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}

package com.example.smartagriculture.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartagriculture.R
import com.example.smartagriculture.adapter.CalendarAdapter
import com.example.smartagriculture.viewmodel.CalendarViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class CalendarFragment : Fragment(R.layout.fragment_calendar) {

    private val viewModel: CalendarViewModel by viewModels()

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(requireContext(), "Reminder notifications enabled.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkNotificationPermission()

        val rvCalendar = view.findViewById<RecyclerView>(R.id.rvCalendar)
        rvCalendar.layoutManager = LinearLayoutManager(requireContext())

        val adapter = CalendarAdapter(emptyList()) { activity ->
            viewModel.deleteActivity(activity)
            Toast.makeText(requireContext(), "Activity reminder removed.", Toast.LENGTH_SHORT).show()
        }
        rvCalendar.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.activities.collect { activities ->
                if (activities.isEmpty()) {
                    seedDefaultActivities()
                } else {
                    adapter.updateList(activities)
                }
            }
        }

        val btnAdd = view.findViewById<Button>(R.id.btnAddActivity)
        btnAdd?.setOnClickListener {
            showAddActivityDialog()
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun seedDefaultActivities() {
        val now = System.currentTimeMillis()
        viewModel.addActivity("Rice (Kharif)", "First Top Dressing (Urea)", "FERTILIZER", now + TimeUnit.DAYS.toMillis(1))
        viewModel.addActivity("Wheat (Rabi)", "Crown Root Irrigation", "WATERING", now + TimeUnit.DAYS.toMillis(3))
        viewModel.addActivity("Cotton", "Bollworm Pest Inspection & Neem Spray", "SPRAY", now + TimeUnit.DAYS.toMillis(5))
        viewModel.addActivity("Maize", "Zinc Sulphate Foliar Application", "FERTILIZER", now + TimeUnit.DAYS.toMillis(7))
        viewModel.addActivity("Sugarcane", "Harvest & Trash Mulching", "HARVEST", now + TimeUnit.DAYS.toMillis(14))
    }

    private fun showAddActivityDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_activity, null)
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etCrop = dialogView.findViewById<EditText>(R.id.etCrop)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val spinnerType = dialogView.findViewById<Spinner>(R.id.spinnerType)
        val spinnerDelay = dialogView.findViewById<Spinner>(R.id.spinnerDelay)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSchedule = dialogView.findViewById<Button>(R.id.btnSchedule)

        val types = listOf("FERTILIZER", "WATERING", "SPRAY", "HARVEST", "GENERAL")
        val typeAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner_selected, types).apply {
            setDropDownViewResource(R.layout.item_spinner_dropdown)
        }
        spinnerType.adapter = typeAdapter

        val delayOptions = listOf(
            "Schedule in 1 Minute (Test Alert)",
            "Schedule in 1 Day",
            "Schedule in 3 Days",
            "Schedule in 7 Days",
            "Schedule in 14 Days"
        )
        val delayAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner_selected, delayOptions).apply {
            setDropDownViewResource(R.layout.item_spinner_dropdown)
        }
        spinnerDelay.adapter = delayAdapter

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        btnSchedule.setOnClickListener {
            val crop = etCrop.text.toString().trim().ifBlank { "Crop" }
            val title = etTitle.text.toString().trim().ifBlank { "Farm Activity" }
            val type = types.getOrElse(spinnerType.selectedItemPosition) { "GENERAL" }

            val delayMillis = when (spinnerDelay.selectedItemPosition) {
                0 -> TimeUnit.MINUTES.toMillis(1)
                1 -> TimeUnit.DAYS.toMillis(1)
                2 -> TimeUnit.DAYS.toMillis(3)
                3 -> TimeUnit.DAYS.toMillis(7)
                4 -> TimeUnit.DAYS.toMillis(14)
                else -> TimeUnit.DAYS.toMillis(1)
            }
            val scheduledDate = System.currentTimeMillis() + delayMillis

            viewModel.addActivity(crop, title, type, scheduledDate)
            Toast.makeText(requireContext(), "Reminder scheduled successfully!", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
}
package com.example.smartagriculture.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
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
import com.example.smartagriculture.database.CropActivityEntity
import com.example.smartagriculture.network.RetrofitClient
import com.example.smartagriculture.network.WeatherApiService
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
        fetchWeatherBarData(view)

        val rvCalendar = view.findViewById<RecyclerView>(R.id.rvCalendar)
        rvCalendar?.layoutManager = LinearLayoutManager(requireContext())

        val adapter = CalendarAdapter(
            activityList = emptyList(),
            onEditClick = { activity ->
                showEditActivityDialog(activity)
            },
            onDeleteClick = { activity ->
                viewModel.deleteActivity(activity)
                Toast.makeText(requireContext(), "Activity reminder removed.", Toast.LENGTH_SHORT).show()
            }
        )
        rvCalendar?.adapter = adapter

        val chipAll = view.findViewById<TextView>(R.id.chipAllActivities)
        val chipDaily = view.findViewById<TextView>(R.id.chipDailyRoutines)

        chipAll?.setOnClickListener {
            viewModel.setFilterDailyOnly(false)
            chipAll.setBackgroundResource(R.drawable.bg_chip_selected)
            chipAll.setTextColor(Color.WHITE)
            chipDaily?.setBackgroundResource(R.drawable.bg_chip_unselected)
            chipDaily?.setTextColor(Color.parseColor("#9CA3AF"))
        }

        chipDaily?.setOnClickListener {
            viewModel.setFilterDailyOnly(true)
            chipDaily.setBackgroundResource(R.drawable.bg_chip_selected)
            chipDaily.setTextColor(Color.WHITE)
            chipAll?.setBackgroundResource(R.drawable.bg_chip_unselected)
            chipAll?.setTextColor(Color.parseColor("#9CA3AF"))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filteredActivities.collect { activities ->
                if (activities.isEmpty() && !viewModel.filterDailyOnly.value) {
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

    private fun fetchWeatherBarData(view: View) {
        val tvWeatherIcon = view.findViewById<TextView>(R.id.tvWeatherIcon)
        val tvWeatherTemp = view.findViewById<TextView>(R.id.tvWeatherTemp)
        val tvWeatherRainProb = view.findViewById<TextView>(R.id.tvWeatherRainProb)
        val tvWeatherHumidity = view.findViewById<TextView>(R.id.tvWeatherHumidity)
        val tvSprayBadge = view.findViewById<TextView>(R.id.tvSprayBadge)
        val tvWeatherAdvisory = view.findViewById<TextView>(R.id.tvWeatherAdvisory)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val weatherApi = RetrofitClient.weatherRetrofit
                    .create(WeatherApiService::class.java)
                val response = weatherApi.getCurrentWeather(19.0760, 72.8777)

                val temp = response.current_weather?.temperature ?: 28.0
                val weatherCode = response.current_weather?.weathercode ?: 0
                val rainProb = response.hourly?.precipitation_probability?.take(6)?.maxOrNull() ?: 10
                val humidity = response.hourly?.relativehumidity_2m?.take(6)?.average()?.toInt() ?: 65

                val isRainWarning = rainProb >= 40
                val isHighHumidity = humidity >= 85

                tvWeatherTemp?.text = "${temp.toInt()}°C"
                tvWeatherRainProb?.text = "💧 $rainProb% Rain"
                tvWeatherHumidity?.text = "RH: $humidity%"

                val (icon, advisory) = when {
                    isRainWarning -> "⛈️" to "Rain predicted ($rainProb% chance). Delay foliar spraying."
                    isHighHumidity -> "🌧️" to "High humidity ($humidity%). Fungal spore risk elevated."
                    weatherCode in 1..3 -> "⛅" to "Partly cloudy. Optimal conditions for field tasks."
                    weatherCode >= 51 -> "🌧️" to "Precipitation active. Postpone spraying."
                    else -> "☀️" to "Optimal weather for spraying and crop inspection."
                }

                tvWeatherIcon?.text = icon
                tvWeatherAdvisory?.text = advisory

                if (isRainWarning || weatherCode >= 51) {
                    tvSprayBadge?.text = "⚠️ FOLIAR SPRAY WARNING"
                    tvSprayBadge?.setTextColor(Color.parseColor("#856404"))
                    tvSprayBadge?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFF3CD"))
                } else {
                    tvSprayBadge?.text = "✓ SPRAYING PERMITTED"
                    tvSprayBadge?.setTextColor(Color.parseColor("#10B981"))
                    tvSprayBadge?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#3310B981"))
                }
            } catch (_: Exception) {
                // Keep default fallback values on UI
            }
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
        viewModel.addActivity("Rice (Kharif)", "First Top Dressing (Urea)", "FERTILIZER", now + TimeUnit.DAYS.toMillis(1), isDaily = false)
        viewModel.addActivity("Tomato", "Morning Field Inspection & Scouting", "SPRAY", now + TimeUnit.HOURS.toMillis(2), isDaily = true)
        viewModel.addActivity("Wheat (Rabi)", "Crown Root Irrigation", "WATERING", now + TimeUnit.DAYS.toMillis(3), isDaily = false)
        viewModel.addActivity("Vegetables", "Drip Irrigation Moisture Check", "WATERING", now + TimeUnit.HOURS.toMillis(4), isDaily = true)
        viewModel.addActivity("Cotton", "Bollworm Pest Inspection & Neem Spray", "SPRAY", now + TimeUnit.DAYS.toMillis(5), isDaily = false)
        viewModel.addActivity("Maize", "Zinc Sulphate Foliar Application", "FERTILIZER", now + TimeUnit.DAYS.toMillis(7), isDaily = false)
        viewModel.addActivity("Sugarcane", "Harvest & Trash Mulching", "HARVEST", now + TimeUnit.DAYS.toMillis(14), isDaily = false)
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
        val cbIsDaily = dialogView.findViewById<CheckBox>(R.id.cbIsDaily)
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
            val isDaily = cbIsDaily?.isChecked ?: false

            val delayMillis = when (spinnerDelay.selectedItemPosition) {
                0 -> TimeUnit.MINUTES.toMillis(1)
                1 -> TimeUnit.DAYS.toMillis(1)
                2 -> TimeUnit.DAYS.toMillis(3)
                3 -> TimeUnit.DAYS.toMillis(7)
                4 -> TimeUnit.DAYS.toMillis(14)
                else -> TimeUnit.DAYS.toMillis(1)
            }
            val scheduledDate = System.currentTimeMillis() + delayMillis

            viewModel.addActivity(crop, title, type, scheduledDate, isDaily = isDaily)
            Toast.makeText(requireContext(), "Reminder scheduled successfully!", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun showEditActivityDialog(activity: CropActivityEntity) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_activity, null)
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val etCrop = dialogView.findViewById<EditText>(R.id.etCrop)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val spinnerType = dialogView.findViewById<Spinner>(R.id.spinnerType)
        val spinnerDelay = dialogView.findViewById<Spinner>(R.id.spinnerDelay)
        val cbIsDaily = dialogView.findViewById<CheckBox>(R.id.cbIsDaily)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSchedule = dialogView.findViewById<Button>(R.id.btnSchedule)

        tvDialogTitle?.text = "✏️ Edit Crop Activity Reminder"
        etCrop?.setText(activity.cropName)
        etTitle?.setText(activity.activityTitle)
        cbIsDaily?.isChecked = activity.isDaily
        btnSchedule?.text = "Update & Save"

        val types = listOf("FERTILIZER", "WATERING", "SPRAY", "HARVEST", "GENERAL")
        val typeAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner_selected, types).apply {
            setDropDownViewResource(R.layout.item_spinner_dropdown)
        }
        spinnerType.adapter = typeAdapter
        val typeIndex = types.indexOf(activity.activityType.uppercase()).takeIf { it >= 0 } ?: 0
        spinnerType.setSelection(typeIndex)

        val delayOptions = listOf(
            "Keep Current Scheduled Time",
            "Reschedule in 1 Minute (Test Alert)",
            "Reschedule in 1 Day",
            "Reschedule in 3 Days",
            "Reschedule in 7 Days",
            "Reschedule in 14 Days"
        )
        val delayAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner_selected, delayOptions).apply {
            setDropDownViewResource(R.layout.item_spinner_dropdown)
        }
        spinnerDelay.adapter = delayAdapter

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        btnSchedule.setOnClickListener {
            val crop = etCrop.text.toString().trim().ifBlank { activity.cropName }
            val title = etTitle.text.toString().trim().ifBlank { activity.activityTitle }
            val type = types.getOrElse(spinnerType.selectedItemPosition) { activity.activityType }
            val isDaily = cbIsDaily?.isChecked ?: false

            val scheduledDate = when (spinnerDelay.selectedItemPosition) {
                0 -> activity.scheduledDate
                1 -> System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1)
                2 -> System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)
                3 -> System.currentTimeMillis() + TimeUnit.DAYS.toMillis(3)
                4 -> System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)
                5 -> System.currentTimeMillis() + TimeUnit.DAYS.toMillis(14)
                else -> activity.scheduledDate
            }

            viewModel.updateActivity(
                activity = activity,
                newCropName = crop,
                newTitle = title,
                newType = type,
                newScheduledDate = scheduledDate,
                newIsDaily = isDaily
            )
            Toast.makeText(requireContext(), "Reminder updated successfully!", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
}
package com.example.smartagriculture.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartagriculture.R
import com.example.smartagriculture.adapter.CalendarAdapter
import com.example.smartagriculture.model.CropCalendar

class CalendarFragment : Fragment(R.layout.fragment_calendar) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvCalendar = view.findViewById<RecyclerView>(R.id.rvCalendar)
        rvCalendar.layoutManager = LinearLayoutManager(requireContext())

        val calendarList = mutableListOf(
            CropCalendar("Rice (Kharif) - Sowing & Irrigation", "June - July", "November - December", "Needs well-distributed rainfall or irrigation. Sown at the onset of monsoon."),
            CropCalendar("Wheat (Rabi) - Top Dressing", "October - November", "March - April", "Requires cool weather during growth and warm weather during ripening."),
            CropCalendar("Maize - Fertilizer Application", "June - July", "September - October", "Versatile crop, requires moderate rainfall and well-drained soil."),
            CropCalendar("Cotton - Pest Inspection", "April - May", "October - December", "Requires a long frost-free period and plenty of sunshine."),
            CropCalendar("Sugarcane - Harvest Window", "Jan - March", "Dec - March", "Long duration crop requiring hot and humid climate.")
        )

        val adapter = CalendarAdapter(calendarList)
        rvCalendar.adapter = adapter

        val btnAdd = view.findViewById<Button>(R.id.btnAddActivity)
        btnAdd?.setOnClickListener {
            showAddActivityDialog(calendarList, adapter)
        }
    }

    private fun showAddActivityDialog(list: MutableList<CropCalendar>, adapter: CalendarAdapter) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("➕ Add Farming Reminder")

        val inputLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val etTitle = EditText(requireContext()).apply {
            hint = "Crop & Activity Name (e.g. Tomato Spraying)"
        }
        val etSowing = EditText(requireContext()).apply {
            hint = "Start Window (e.g. May - June)"
        }
        val etHarvest = EditText(requireContext()).apply {
            hint = "End Window (e.g. Aug - Sept)"
        }
        val etDetails = EditText(requireContext()).apply {
            hint = "Notes / Care Instructions"
        }

        inputLayout.addView(etTitle)
        inputLayout.addView(etSowing)
        inputLayout.addView(etHarvest)
        inputLayout.addView(etDetails)

        builder.setView(inputLayout)

        builder.setPositiveButton("Save") { dialog, _ ->
            val title = etTitle.text.toString().ifBlank { "New Activity" }
            val sowing = etSowing.text.toString().ifBlank { "Immediate" }
            val harvest = etHarvest.text.toString().ifBlank { "Upcoming" }
            val details = etDetails.text.toString().ifBlank { "Farming activity scheduled." }

            list.add(0, CropCalendar(title, sowing, harvest, details))
            adapter.notifyItemInserted(0)
            Toast.makeText(requireContext(), "Reminder saved!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }
}
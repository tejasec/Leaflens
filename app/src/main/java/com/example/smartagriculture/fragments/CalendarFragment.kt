package com.example.smartagriculture.fragments

import android.os.Bundle
import android.view.View
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

        val calendarList = listOf(
            CropCalendar("Rice (Kharif)", "June - July", "November - December", "Needs well-distributed rainfall or irrigation. Sown at the onset of monsoon."),
            CropCalendar("Wheat (Rabi)", "October - November", "March - April", "Requires cool weather during growth and warm weather during ripening."),
            CropCalendar("Maize", "June - July", "September - October", "Versatile crop, requires moderate rainfall and well-drained soil."),
            CropCalendar("Cotton", "April - May", "October - December", "Requires a long frost-free period and plenty of sunshine."),
            CropCalendar("Sugarcane", "Jan - March", "Dec - March", "Long duration crop requiring hot and humid climate.")
        )

        rvCalendar.adapter = CalendarAdapter(calendarList)
    }
}
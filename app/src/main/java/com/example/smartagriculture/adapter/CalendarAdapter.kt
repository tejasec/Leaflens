package com.example.smartagriculture.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartagriculture.R
import com.example.smartagriculture.model.CropCalendar

class CalendarAdapter(private val calendarList: List<CropCalendar>) :
    RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCropName: TextView = itemView.findViewById(R.id.tvCropName)
        val tvSowing: TextView = itemView.findViewById(R.id.tvSowing)
        val tvHarvest: TextView = itemView.findViewById(R.id.tvHarvest)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val calendar = calendarList[position]
        holder.tvCropName.text = calendar.cropName
        holder.tvSowing.text = calendar.sowingSeason
        holder.tvHarvest.text = calendar.harvestSeason
        holder.tvDescription.text = calendar.description
    }

    override fun getItemCount(): Int {
        return calendarList.size
    }
}

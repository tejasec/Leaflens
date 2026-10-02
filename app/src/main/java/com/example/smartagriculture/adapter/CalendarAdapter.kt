package com.example.smartagriculture.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartagriculture.R
import com.example.smartagriculture.database.CropActivityEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CalendarAdapter(
    private var activityList: List<CropActivityEntity>,
    private val onDeleteClick: ((CropActivityEntity) -> Unit)? = null
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    fun updateList(newList: List<CropActivityEntity>) {
        this.activityList = newList
        notifyDataSetChanged()
    }

    class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvActivityType: TextView = itemView.findViewById(R.id.tvActivityType)
        val tvCropName: TextView = itemView.findViewById(R.id.tvCropName)
        val tvSowing: TextView = itemView.findViewById(R.id.tvSowing)
        val tvHarvest: TextView = itemView.findViewById(R.id.tvHarvest)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val btnDeleteActivity: ImageView = itemView.findViewById(R.id.btnDeleteActivity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val activity = activityList[position]
        holder.tvCropName.text = "${activity.cropName} - ${activity.activityTitle}"

        val emoji = when (activity.activityType.uppercase()) {
            "FERTILIZER" -> "🌱"
            "WATERING" -> "💧"
            "SPRAY" -> "🛡️"
            "HARVEST" -> "🌾"
            else -> "📅"
        }
        holder.tvActivityType.text = "$emoji ${activity.activityType.uppercase()}"

        val dateStr = dateFormat.format(Date(activity.scheduledDate))
        val timeStr = timeFormat.format(Date(activity.scheduledDate))
        holder.tvSowing.text = dateStr
        holder.tvHarvest.text = timeStr
        holder.tvDescription.text = if (activity.isCompleted) "Status: Completed ✓" else "Status: Scheduled Reminder Active 🔔"

        holder.btnDeleteActivity.visibility = View.VISIBLE
        holder.btnDeleteActivity.setOnClickListener {
            onDeleteClick?.invoke(activity)
        }
    }

    override fun getItemCount(): Int = activityList.size
}

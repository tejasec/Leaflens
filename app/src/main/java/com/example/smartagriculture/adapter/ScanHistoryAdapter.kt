package com.example.smartagriculture.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smartagriculture.R
import com.example.smartagriculture.model.ScanHistoryItem
import com.example.smartagriculture.utils.ImageStorageManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanHistoryAdapter(
    private var items: List<ScanHistoryItem>,
    private val onItemClick: (ScanHistoryItem) -> Unit
) : RecyclerView.Adapter<ScanHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumb: ImageView = view.findViewById(R.id.ivItemThumb)
        val tvTitle: TextView = view.findViewById(R.id.tvItemTitle)
        val tvDate: TextView = view.findViewById(R.id.tvItemDate)
        val tvBadge: TextView = view.findViewById(R.id.tvItemBadge)
        val tvChatIndicator: TextView = view.findViewById(R.id.tvChatIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scan_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Crop + Disease Title
        val fullTitle = if (item.cropName.isNotBlank() && !item.diseaseName.startsWith(item.cropName, ignoreCase = true)) {
            "${item.cropName} ${item.diseaseName}"
        } else {
            item.diseaseName
        }
        holder.tvTitle.text = fullTitle

        // Timestamp
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        holder.tvDate.text = sdf.format(Date(item.timestamp))

        // Status badge
        val status = item.status.ifBlank {
            if (item.isLowConfidence) "Uncertain"
            else if (item.diseaseName.contains("Healthy", ignoreCase = true)) "Healthy"
            else "Diseased"
        }

        holder.tvBadge.text = status
        when (status) {
            "Healthy" -> {
                holder.tvBadge.setTextColor(Color.parseColor("#10B981"))
                holder.tvBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#3310B981"))
            }
            "Uncertain" -> {
                holder.tvBadge.setTextColor(Color.parseColor("#F59E0B"))
                holder.tvBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#33F59E0B"))
            }
            "New Finding" -> {
                holder.tvBadge.setTextColor(Color.parseColor("#A78BFA"))
                holder.tvBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#338B5CF6"))
            }
            else -> { // Diseased
                holder.tvBadge.setTextColor(Color.parseColor("#EF4444"))
                holder.tvBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#33EF4444"))
            }
        }

        if (!item.chatHistoryJson.isNullOrBlank()) {
            holder.tvChatIndicator.visibility = View.VISIBLE
        } else {
            holder.tvChatIndicator.visibility = View.GONE
        }

        val imageModel = ImageStorageManager.getImageModel(item.imagePath)
        if (imageModel != null) {
            Glide.with(holder.itemView.context)
                .load(imageModel)
                .placeholder(R.drawable.rounded_button)
                .error(R.drawable.rounded_button)
                .into(holder.ivThumb)
        } else {
            holder.ivThumb.setImageResource(R.drawable.rounded_button)
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ScanHistoryItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}

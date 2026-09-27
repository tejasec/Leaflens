package com.example.smartagriculture.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartagriculture.R
import com.example.smartagriculture.model.Crop

class CropAdapter(
    private val crops: List<Crop>,
    private val onCropClick: (Crop) -> Unit
) : RecyclerView.Adapter<CropAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvCropName)
        val season: TextView = view.findViewById(R.id.tvCropSeason)
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_crop, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val crop = crops[position]
        holder.name.text = crop.name
        holder.season.text = "Season: ${crop.season}"
        holder.itemView.setOnClickListener { onCropClick(crop) }
    }

    override fun getItemCount(): Int = crops.size
}
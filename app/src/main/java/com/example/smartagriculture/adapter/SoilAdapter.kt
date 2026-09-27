package com.example.smartagriculture.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartagriculture.R
import com.example.smartagriculture.model.Soil

class SoilAdapter(private val soilList: List<Soil>) :
    RecyclerView.Adapter<SoilAdapter.SoilViewHolder>() {

    class SoilViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSoilType: TextView = itemView.findViewById(R.id.tvSoilType)
        val tvProperties: TextView = itemView.findViewById(R.id.tvProperties)
        val tvSuitableCrops: TextView = itemView.findViewById(R.id.tvSuitableCrops)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoilViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_soil, parent, false)
        return SoilViewHolder(view)
    }

    override fun onBindViewHolder(holder: SoilViewHolder, position: Int) {
        val soil = soilList[position]
        holder.tvSoilType.text = soil.soilType
        holder.tvProperties.text = soil.properties
        holder.tvSuitableCrops.text = soil.suitableCrops
    }

    override fun getItemCount(): Int {
        return soilList.size
    }
}

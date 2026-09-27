package com.example.smartagriculture.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartagriculture.R
import com.example.smartagriculture.model.CropDiseaseItem

class CropDiseaseAdapter(
    private val items: List<CropDiseaseItem>,
    private val onItemClick: (CropDiseaseItem) -> Unit
) : RecyclerView.Adapter<CropDiseaseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumb: ImageView = view.findViewById(R.id.ivDiseaseThumb)
        val tvTitle: TextView = view.findViewById(R.id.tvDiseaseTitle)
        val tvScientific: TextView = view.findViewById(R.id.tvDiseaseScientific)
        val tvShortDesc: TextView = view.findViewById(R.id.tvDiseaseShortDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_crop_disease, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.diseaseName
        holder.tvScientific.text = item.scientificName
        holder.tvShortDesc.text = item.description

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size
}

package com.example.smartagriculture.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartagriculture.R
import com.example.smartagriculture.model.CropDetailItem

class CropListAdapter(
    private var items: List<CropDetailItem>,
    private val onItemClick: (CropDetailItem) -> Unit
) : RecyclerView.Adapter<CropListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumb: ImageView = view.findViewById(R.id.ivCropThumb)
        val tvTitle: TextView = view.findViewById(R.id.tvCropTitle)
        val tvScientific: TextView = view.findViewById(R.id.tvCropScientific)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_crop_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.name
        holder.tvScientific.text = item.scientificName
        holder.ivThumb.setImageResource(item.imageRes)

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<CropDetailItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}

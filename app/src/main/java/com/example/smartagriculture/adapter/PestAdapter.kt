package com.example.smartagriculture.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartagriculture.R
import com.example.smartagriculture.model.Pest

class PestAdapter(
    private var pestList: List<Pest>,
    private val onItemClick: ((Pest) -> Unit)? = null
) : RecyclerView.Adapter<PestAdapter.PestViewHolder>() {

    class PestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPestName: TextView = itemView.findViewById(R.id.tvPestName)
        val tvAffectedCrops: TextView = itemView.findViewById(R.id.tvAffectedCrops)
        val tvSolution: TextView = itemView.findViewById(R.id.tvSolution)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pest, parent, false)
        return PestViewHolder(view)
    }

    override fun onBindViewHolder(holder: PestViewHolder, position: Int) {
        val pest = pestList[position]
        holder.tvPestName.text = pest.pestName
        holder.tvAffectedCrops.text = "Affected Crops: ${pest.affectedCrops}"
        holder.tvSolution.text = pest.solution

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(pest)
        }
    }

    override fun getItemCount(): Int {
        return pestList.size
    }

    fun updateData(newList: List<Pest>) {
        this.pestList = newList
        notifyDataSetChanged()
    }
}

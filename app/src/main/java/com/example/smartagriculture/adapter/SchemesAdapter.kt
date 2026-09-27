package com.example.smartagriculture.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartagriculture.R
import com.example.smartagriculture.model.Scheme

class SchemesAdapter(
    private val schemes: List<Scheme>,
    private val onItemClick: (Scheme) -> Unit
) : RecyclerView.Adapter<SchemesAdapter.SchemeViewHolder>() {

    class SchemeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvSchemeTitle)
        val tvDesc: TextView = view.findViewById(R.id.tvSchemeDesc)
        val tvEligibility: TextView = view.findViewById(R.id.tvSchemeEligibility)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchemeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_scheme, parent, false)
        return SchemeViewHolder(view)
    }

    override fun onBindViewHolder(holder: SchemeViewHolder, position: Int) {
        val scheme = schemes[position]
        holder.tvTitle.text = scheme.title
        holder.tvDesc.text = scheme.description
        holder.tvEligibility.text = "Eligibility: ${scheme.eligibility}"
        holder.itemView.setOnClickListener { onItemClick(scheme) }
    }

    override fun getItemCount(): Int = schemes.size
}

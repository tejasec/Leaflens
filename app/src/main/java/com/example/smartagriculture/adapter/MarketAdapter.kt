package com.example.smartagriculture.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartagriculture.R
import com.example.smartagriculture.model.MarketPrice

class MarketAdapter(
    private var prices: List<MarketPrice>,
    private val onItemClick: (MarketPrice) -> Unit
) : RecyclerView.Adapter<MarketAdapter.MarketViewHolder>() {

    class MarketViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCommodity: TextView = view.findViewById(R.id.tv_commodity)
        val tvMarket: TextView = view.findViewById(R.id.tv_market)
        val tvPrice: TextView = view.findViewById(R.id.tv_price)
        val tvDate: TextView = view.findViewById(R.id.tv_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_market, parent, false)
        return MarketViewHolder(view)
    }

    override fun onBindViewHolder(holder: MarketViewHolder, position: Int) {
        val item = prices[position]
        holder.tvCommodity.text = item.commodity
        holder.tvMarket.text = item.market
        holder.tvPrice.text = "₹ ${item.price} / quintal"
        holder.tvDate.text = item.date
        
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = prices.size

    fun updateData(newPrices: List<MarketPrice>) {
        prices = newPrices
        notifyDataSetChanged()
    }
}

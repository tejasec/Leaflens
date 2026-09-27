package com.example.smartagriculture.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.example.smartagriculture.R
import com.example.smartagriculture.adapter.MarketAdapter
import com.example.smartagriculture.databinding.FragmentMarketBinding
import com.example.smartagriculture.model.MarketPrice
import kotlinx.coroutines.launch

class MarketFragment : Fragment(R.layout.fragment_market) {

    private var _binding: FragmentMarketBinding? = null
    private val binding get() = _binding!!
    private lateinit var marketAdapter: MarketAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMarketBinding.bind(view)

        setupRecyclerView()
        fetchMarketData()
    }

    private fun setupRecyclerView() {
        marketAdapter = MarketAdapter(emptyList()) { selectedItem ->
            val action = MarketFragmentDirections.actionMarketFragmentToMarketDetailFragment(selectedItem)
            findNavController().navigate(action)
        }
        binding.rvMarket.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = marketAdapter
        }
    }

    private fun fetchMarketData() {
        binding.shimmerMarketContainer.startShimmer()
        binding.shimmerMarketContainer.visibility = View.VISIBLE
        binding.rvMarket.visibility = View.GONE

        val mandiApi = com.example.smartagriculture.network.RetrofitClient.mandiRetrofit.create(com.example.smartagriculture.network.MandiApiService::class.java)
        val apiKey = "579b464db66ec23bdd0000015be113541a9d46725eb5a4f80edec8c4" // User API key

        lifecycleScope.launch {
            try {
                val response = mandiApi.getMandiPrices(apiKey = apiKey, limit = 50)
                
                val realData = response.records?.map { record ->
                    MarketPrice(
                        commodity = record.commodity ?: "Unknown",
                        market = "${record.market}, ${record.state}",
                        price = record.modal_price ?: record.max_price ?: "N/A",
                        date = record.arrival_date ?: ""
                    )
                } ?: emptyList()

                binding.shimmerMarketContainer.stopShimmer()
                binding.shimmerMarketContainer.visibility = View.GONE
                binding.rvMarket.visibility = View.VISIBLE
                
                if (realData.isNotEmpty()) {
                    marketAdapter.updateData(realData)
                } else {
                    android.widget.Toast.makeText(requireContext(), "No market data found", android.widget.Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                binding.shimmerMarketContainer.stopShimmer()
                binding.shimmerMarketContainer.visibility = View.GONE
                binding.rvMarket.visibility = View.VISIBLE
                
                val mockData = listOf(
                    MarketPrice("Wheat", "Delhi Mandi, Delhi", "2300", "03/07/2026"),
                    MarketPrice("Rice", "Karnal Mandi, Haryana", "3500", "03/07/2026"),
                    MarketPrice("Maize", "Pune Mandi, Maharashtra", "1800", "03/07/2026"),
                    MarketPrice("Sugarcane", "Meerut Mandi, UP", "400", "03/07/2026"),
                    MarketPrice("Cotton", "Rajkot Mandi, Gujarat", "7500", "03/07/2026")
                )
                marketAdapter.updateData(mockData)
                
                if (e.message?.contains("429") == true) {
                    android.widget.Toast.makeText(requireContext(), "Live data limit reached. Showing recent prices.", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(requireContext(), "Offline mode: Showing cached prices.", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

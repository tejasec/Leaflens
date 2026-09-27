package com.example.smartagriculture.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.smartagriculture.R
import com.example.smartagriculture.databinding.FragmentWeatherBinding
import com.example.smartagriculture.network.RetrofitClient
import com.example.smartagriculture.network.WeatherApiService
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

class WeatherFragment : Fragment(R.layout.fragment_weather) {

    private var _binding: FragmentWeatherBinding? = null
    private val binding get() = _binding!!

    // IMPORTANT: Replace with a valid API key for real usage, using a placeholder for now
    private val weatherApiKey = "YOUR_OPENWEATHERMAP_API_KEY"

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            fetchLocationAndWeather()
        } else {
            Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWeatherBinding.bind(view)

        binding.btnUseLocation.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fetchLocationAndWeather()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun fetchLocationAndWeather() {
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.shimmerViewContainer.startShimmer()
        binding.weatherContentLayout.visibility = View.GONE
        binding.btnUseLocation.visibility = View.GONE

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    fetchWeather(location.latitude, location.longitude)
                } else {
                    showError("Could not get location. Try again.")
                }
            }.addOnFailureListener {
                showError("Failed to get location.")
            }
        } catch (e: SecurityException) {
            showError("Location permission required.")
        }
    }

    private fun fetchWeather(lat: Double, lon: Double) {
        val weatherApi = RetrofitClient.weatherRetrofit.create(WeatherApiService::class.java)

        lifecycleScope.launch {
            try {
                val response = weatherApi.getCurrentWeather(lat, lon)
                
                binding.tvCityName.text = "Current Location"
                binding.tvTemperature.text = "${response.current_weather?.temperature ?: "--"}°C"
                
                // Open-Meteo uses WMO weather codes. We map a few basic ones here, or just show the code.
                val weatherDesc = when (response.current_weather?.weathercode) {
                    0 -> "Clear sky"
                    1, 2, 3 -> "Mainly clear, partly cloudy, and overcast"
                    45, 48 -> "Fog"
                    51, 53, 55 -> "Drizzle"
                    61, 63, 65 -> "Rain"
                    71, 73, 75 -> "Snow"
                    95 -> "Thunderstorm"
                    else -> "Unknown (${response.current_weather?.weathercode})"
                }
                
                binding.tvDescription.text = weatherDesc
                binding.tvHumidity.text = "Windspeed: ${response.current_weather?.windspeed ?: "--"} km/h"

                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visibility = View.GONE
                binding.weatherContentLayout.visibility = View.VISIBLE
            } catch (e: Exception) {
                showError("Failed to fetch weather data: ${e.message}")
            }
        }
    }

    private fun showError(message: String) {
        binding.shimmerViewContainer.stopShimmer()
        binding.shimmerViewContainer.visibility = View.GONE
        binding.btnUseLocation.visibility = View.VISIBLE
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

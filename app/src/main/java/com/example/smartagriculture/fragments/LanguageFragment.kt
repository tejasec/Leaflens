package com.example.smartagriculture.fragments

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.smartagriculture.AppConfig
import com.example.smartagriculture.R
import com.example.smartagriculture.databinding.FragmentLanguageBinding

class LanguageFragment : Fragment(R.layout.fragment_language) {

    private var binding: FragmentLanguageBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentLanguageBinding.bind(view)

        val languages = resources.getStringArray(R.array.language_array)

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            languages
        )
        binding?.spinnerLanguage?.adapter = adapter

        // Pre-select currently saved language
        val prefs = requireContext().getSharedPreferences("smart_agri_prefs", 0)
        val currentLang = prefs.getString("selected_language", "en") ?: "en"
        val initialIndex = when (currentLang) {
            "hi" -> 1
            "mr" -> 2
            "gu" -> 3
            "te" -> 4
            "ta" -> 5
            else -> 0
        }
        binding?.spinnerLanguage?.setSelection(initialIndex)

        binding?.btnContinue?.setOnClickListener {
            val selectedLanguage = binding?.spinnerLanguage?.selectedItem.toString()
            val languageCode = when {
                selectedLanguage.contains("हिंदी") || selectedLanguage.contains("Hindi", ignoreCase = true) -> "hi"
                selectedLanguage.contains("मराठी") || selectedLanguage.contains("Marathi", ignoreCase = true) -> "mr"
                selectedLanguage.contains("ગુજરાતી") || selectedLanguage.contains("Gujarati", ignoreCase = true) -> "gu"
                selectedLanguage.contains("తెలుగు") || selectedLanguage.contains("Telugu", ignoreCase = true) -> "te"
                selectedLanguage.contains("தமிழ்") || selectedLanguage.contains("Tamil", ignoreCase = true) -> "ta"
                else -> "en"
            }

            prefs.edit().putString("selected_language", languageCode).apply()

            // Apply locale immediately using AppCompatDelegate
            val appLocale = LocaleListCompat.forLanguageTags(languageCode)
            AppCompatDelegate.setApplicationLocales(appLocale)

            if (AppConfig.IS_AUTH_ENABLED) {
                findNavController().navigate(R.id.action_languageFragment_to_loginFragment)
            } else {
                findNavController().navigate(R.id.homeFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
package com.example.smartagriculture.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.smartagriculture.AppConfig
import com.example.smartagriculture.R

class SplashFragment : Fragment(R.layout.fragment_splash) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivLogo = view.findViewById<View>(R.id.ivLogo)
        val tvAppName = view.findViewById<View>(R.id.tvAppName)

        // Initial state
        ivLogo.alpha = 0f
        ivLogo.scaleX = 0.3f
        ivLogo.scaleY = 0.3f
        
        tvAppName.alpha = 0f
        tvAppName.translationY = 100f

        // Animations
        ivLogo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(1000)
            .setInterpolator(android.view.animation.OvershootInterpolator())
            .start()

        tvAppName.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(1000)
            .setStartDelay(400)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        view.postDelayed({
            val prefs = requireContext().getSharedPreferences("smart_agri_prefs", 0)
            val selectedLanguage = prefs.getString("selected_language", null)

            if (selectedLanguage != null) {
                if (AppConfig.IS_AUTH_ENABLED) {
                    findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
                } else {
                    findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                }
            } else {
                findNavController().navigate(R.id.action_splashFragment_to_languageFragment)
            }
        }, 2200)
    }
}
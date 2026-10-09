package com.example.smartagriculture.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.smartagriculture.R
import com.example.smartagriculture.databinding.ActivityMainBinding
import com.example.smartagriculture.ml.MobileNetV2Verifier

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Read and apply saved theme preference before super.onCreate
        com.example.smartagriculture.utils.ThemeManager.applyTheme(this)

        // Read and apply saved language preference before super.onCreate
        val prefs = getSharedPreferences("smart_agri_prefs", 0)
        val savedLang = prefs.getString("selected_language", null)
        if (savedLang != null) {
            val currentLocales = AppCompatDelegate.getApplicationLocales()
            if (currentLocales.isEmpty) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(savedLang))
            }
        }

        super.onCreate(savedInstanceState)
        hideSystemStatusBar()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verify MobileNetV2 Edge Model Implementation & Bounds safely
        try {
            MobileNetV2Verifier.verifyImplementation(this)
        } catch (t: Throwable) {
            android.util.Log.e("MainActivity", "Model verification skipped or failed on startup", t)
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        // Show/hide bottom navigation depending on the destination fragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splashFragment,
                R.id.languageFragment,
                R.id.loginFragment,
                R.id.registerFragment -> {
                    binding.bottomNav.visibility = View.GONE
                }
                else -> {
                    binding.bottomNav.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemStatusBar()
        }
    }

    private fun hideSystemStatusBar() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.statusBars())
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false
    }
}
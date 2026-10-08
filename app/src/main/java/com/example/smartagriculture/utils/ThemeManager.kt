package com.example.smartagriculture.utils

import android.app.Activity
import android.content.Context
import com.example.smartagriculture.R

/**
 * Manages runtime theme selection and persistence between the signature Dark/Green mode and Light mode.
 */
object ThemeManager {
    const val PREFS_NAME = "smart_agri_prefs"
    const val KEY_LIGHT_MODE = "light_mode_enabled"

    /**
     * Checks if Light Mode is active.
     * Defaults to false, ensuring the signature Dark/Green mode is preserved as the default.
     */
    fun isLightMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_LIGHT_MODE, false)
    }

    /**
     * Persists the Light Mode state to local SharedPreferences.
     */
    fun setLightMode(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_LIGHT_MODE, enabled).apply()
    }

    /**
     * Applies the appropriate theme to the given Activity.
     * Must be called in Activity.onCreate() before super.onCreate() or setContentView().
     */
    fun applyTheme(activity: Activity) {
        if (isLightMode(activity)) {
            activity.setTheme(R.style.Theme_SmartAgriculture_Light)
        } else {
            activity.setTheme(R.style.Theme_SmartAgriculture)
        }
    }
}

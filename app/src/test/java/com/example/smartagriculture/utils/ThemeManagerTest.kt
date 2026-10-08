package com.example.smartagriculture.utils

import android.content.Context
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ThemeManagerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        val prefs = context.getSharedPreferences(ThemeManager.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
    }

    @Test
    fun testDefaultMode_isDarkGreenMode() {
        // By default, light mode must be disabled so the signature dark/green theme is untouched
        val isLight = ThemeManager.isLightMode(context)
        assertFalse("Default theme must be Dark/Green (isLightMode = false)", isLight)
    }

    @Test
    fun testSetLightMode_persistsEnabled() {
        ThemeManager.setLightMode(context, true)
        val isLight = ThemeManager.isLightMode(context)
        assertTrue("Enabling light mode must persist true", isLight)
    }

    @Test
    fun testSetLightMode_revertsToDarkGreen() {
        ThemeManager.setLightMode(context, true)
        assertTrue(ThemeManager.isLightMode(context))

        ThemeManager.setLightMode(context, false)
        assertFalse("Disabling light mode must revert to Dark/Green (false)", ThemeManager.isLightMode(context))
    }

    @Test
    fun testResetPreferences_restoresDefaultDark() {
        ThemeManager.setLightMode(context, true)
        assertTrue(ThemeManager.isLightMode(context))

        val prefs = context.getSharedPreferences(ThemeManager.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().commit()

        assertFalse("Clearing preferences must restore default Dark/Green mode", ThemeManager.isLightMode(context))
    }
}

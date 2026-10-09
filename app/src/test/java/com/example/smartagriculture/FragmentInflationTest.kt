package com.example.smartagriculture

import android.view.LayoutInflater
import androidx.appcompat.view.ContextThemeWrapper
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FragmentInflationTest {

    // --- Default Theme (Dark / Forest Green) ---

    @Test
    fun testSettingsLayoutInflation_DefaultTheme() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_settings, null, false)
        assertNotNull(view)
    }

    @Test
    fun testCalendarLayoutInflation_DefaultTheme() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_calendar, null, false)
        assertNotNull(view)
    }

    @Test
    fun testCalendarItemLayoutInflation_DefaultTheme() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_calendar, null, false)
        assertNotNull(view)
    }

    @Test
    fun testSchemesLayoutInflation_DefaultTheme() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_schemes, null, false)
        assertNotNull(view)
    }

    @Test
    fun testSchemeItemLayoutInflation_DefaultTheme() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_scheme, null, false)
        assertNotNull(view)
    }

    // --- Night Mode (System Dark Theme Qualifier) ---

    @Test
    @Config(qualifiers = "night")
    fun testSettingsLayoutInflation_NightMode() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_settings, null, false)
        assertNotNull(view)
    }

    @Test
    @Config(qualifiers = "night")
    fun testCalendarLayoutInflation_NightMode() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_calendar, null, false)
        assertNotNull(view)
    }

    @Test
    @Config(qualifiers = "night")
    fun testCalendarItemLayoutInflation_NightMode() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_calendar, null, false)
        assertNotNull(view)
    }

    @Test
    @Config(qualifiers = "night")
    fun testSchemesLayoutInflation_NightMode() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_schemes, null, false)
        assertNotNull(view)
    }

    @Test
    @Config(qualifiers = "night")
    fun testSchemeItemLayoutInflation_NightMode() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_scheme, null, false)
        assertNotNull(view)
    }

    // --- Light Theme (Clean Agricultural Daylight Mode) ---

    @Test
    fun testSettingsLayoutInflation_LightTheme() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture_Light)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_settings, null, false)
        assertNotNull(view)
    }

    @Test
    fun testCalendarLayoutInflation_LightTheme() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture_Light)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_calendar, null, false)
        assertNotNull(view)
    }

    @Test
    fun testCalendarItemLayoutInflation_LightTheme() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture_Light)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_calendar, null, false)
        assertNotNull(view)
    }

    @Test
    fun testSchemesLayoutInflation_LightTheme() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture_Light)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_schemes, null, false)
        assertNotNull(view)
    }

    @Test
    fun testSchemeItemLayoutInflation_LightTheme() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture_Light)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_scheme, null, false)
        assertNotNull(view)
    }

    // --- History & Scan Details Layout Inflation Tests ---

    @Test
    fun testHistoryLayoutInflation_DefaultTheme() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_history, null, false)
        assertNotNull(view)
    }

    @Test
    fun testScanDetailsLayoutInflation_DefaultTheme() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_scan_details, null, false)
        assertNotNull(view)
    }

    @Test
    fun testScanHistoryItemLayoutInflation_DefaultTheme() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_scan_history, null, false)
        assertNotNull(view)
    }

    @Test
    @Config(qualifiers = "night")
    fun testHistoryLayoutInflation_NightMode() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_history, null, false)
        assertNotNull(view)
    }

    @Test
    @Config(qualifiers = "night")
    fun testScanDetailsLayoutInflation_NightMode() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_scan_details, null, false)
        assertNotNull(view)
    }

    @Test
    fun testHistoryLayoutInflation_LightTheme() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture_Light)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_history, null, false)
        assertNotNull(view)
    }

    @Test
    fun testScanDetailsLayoutInflation_LightTheme() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture_Light)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_scan_details, null, false)
        assertNotNull(view)
    }

    // --- Scan Screen & Pathogen Enrollment Inflation Tests ---

    @Test
    fun testScanLayoutInflation_DefaultTheme() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_scan, null, false)
        assertNotNull(view)
    }

    @Test
    fun testEnrollPathogenLayoutInflation_DefaultTheme() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_enroll_pathogen, null, false)
        assertNotNull(view)
    }

    @Test
    @Config(qualifiers = "night")
    fun testScanLayoutInflation_NightMode() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_scan, null, false)
        assertNotNull(view)
    }

    @Test
    @Config(qualifiers = "night")
    fun testEnrollPathogenLayoutInflation_NightMode() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_enroll_pathogen, null, false)
        assertNotNull(view)
    }

    @Test
    fun testScanLayoutInflation_LightTheme() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture_Light)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_scan, null, false)
        assertNotNull(view)
    }

    @Test
    fun testEnrollPathogenLayoutInflation_LightTheme() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture_Light)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_enroll_pathogen, null, false)
        assertNotNull(view)
    }

    // --- Scan Result & Language Screen Inflation Tests ---

    @Test
    fun testScanResultLayoutInflation_DefaultTheme() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_scan_result, null, false)
        assertNotNull(view)
    }

    @Test
    fun testLanguageLayoutInflation_DefaultTheme() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_language, null, false)
        assertNotNull(view)
    }

    @Test
    @Config(qualifiers = "night")
    fun testScanResultLayoutInflation_NightMode() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_scan_result, null, false)
        assertNotNull(view)
    }

    @Test
    fun testScanResultLayoutInflation_LightTheme() {
        val app = RuntimeEnvironment.getApplication()
        val context = ContextThemeWrapper(app, R.style.Theme_SmartAgriculture_Light)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_scan_result, null, false)
        assertNotNull(view)
    }
}

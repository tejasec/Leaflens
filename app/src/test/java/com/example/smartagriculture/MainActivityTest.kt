package com.example.smartagriculture

import androidx.recyclerview.widget.RecyclerView
import com.example.smartagriculture.activities.MainActivity
import com.example.smartagriculture.adapter.DashboardAdapter
import com.example.smartagriculture.fragments.HomeFragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MainActivityTest {

    @Test
    fun testMainActivityLaunchAndViews() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        assertNotNull(activity)
        assertNotNull(activity.findViewById(R.id.bottomNav))
        assertNotNull(activity.findViewById(R.id.nav_host_fragment_content_main))
    }

    @Test
    fun testHomeFragmentDashboardExcludesBenchmark() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val fragment = HomeFragment()
        activity.supportFragmentManager.beginTransaction()
            .add(fragment, "home")
            .commitNow()

        val rvDashboard = fragment.view?.findViewById<RecyclerView>(R.id.rvDashboard)
        assertNotNull(rvDashboard)
        val adapter = rvDashboard?.adapter as? DashboardAdapter
        assertNotNull(adapter)
        assertEquals(9, adapter?.itemCount)
    }
}

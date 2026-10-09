package com.example.smartagriculture

import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.smartagriculture.database.AppDatabase
import com.example.smartagriculture.database.CropActivityEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class CalendarFeatureTest {

    @Test
    fun testCropActivityEntity_defaultIsDailyIsFalse() {
        val activity = CropActivityEntity(
            id = 1L,
            cropName = "Rice",
            activityTitle = "First Top Dressing",
            scheduledDate = 1700000000000L,
            activityType = "FERTILIZER"
        )

        assertFalse("Default isDaily should be false", activity.isDaily)
        assertFalse("Default isCompleted should be false", activity.isCompleted)
        assertEquals("Rice", activity.cropName)
        assertEquals("First Top Dressing", activity.activityTitle)
        assertEquals("FERTILIZER", activity.activityType)
    }

    @Test
    fun testCropActivityEntity_withIsDailyTrue() {
        val dailyRoutine = CropActivityEntity(
            id = 2L,
            cropName = "Tomato",
            activityTitle = "Morning Field Inspection & Scouting",
            scheduledDate = 1700000000000L,
            isCompleted = false,
            activityType = "SPRAY",
            isDaily = true
        )

        assertTrue("isDaily should be true for recurring routine", dailyRoutine.isDaily)
        assertEquals("SPRAY", dailyRoutine.activityType)
    }

    @Test
    fun testCropActivityEntity_editingReminderUpdatesFields() {
        val original = CropActivityEntity(
            id = 10L,
            cropName = "Wheat",
            activityTitle = "Crown Root Irrigation",
            scheduledDate = 1700000000000L,
            isCompleted = false,
            activityType = "WATERING",
            isDaily = false
        )

        val updated = original.copy(
            cropName = "Wheat (Rabi)",
            activityTitle = "Secondary Node Irrigation",
            activityType = "WATERING",
            scheduledDate = 1700086400000L,
            isDaily = true
        )

        assertEquals(10L, updated.id)
        assertEquals("Wheat (Rabi)", updated.cropName)
        assertEquals("Secondary Node Irrigation", updated.activityTitle)
        assertEquals(1700086400000L, updated.scheduledDate)
        assertTrue(updated.isDaily)
    }

    @Test
    fun testFilterActivities_dailyOnlyVsAll() {
        val activities = listOf(
            CropActivityEntity(1L, "Rice", "Top Dressing", 1000L, false, "FERTILIZER", isDaily = false),
            CropActivityEntity(2L, "Tomato", "Scouting", 2000L, false, "SPRAY", isDaily = true),
            CropActivityEntity(3L, "Wheat", "Irrigation", 3000L, false, "WATERING", isDaily = false),
            CropActivityEntity(4L, "Vegetables", "Drip Check", 4000L, false, "WATERING", isDaily = true)
        )

        assertEquals(4, activities.size)

        val dailyOnly = activities.filter { it.isDaily }
        assertEquals(2, dailyOnly.size)
        assertTrue(dailyOnly.all { it.isDaily })
        assertEquals("Tomato", dailyOnly[0].cropName)
        assertEquals("Vegetables", dailyOnly[1].cropName)
    }

    @Test
    fun testRoomMigration_MIGRATION_6_7_altersCropActivitiesTable() {
        val mockDb = mock(SupportSQLiteDatabase::class.java)
        AppDatabase.MIGRATION_6_7.migrate(mockDb)

        verify(mockDb).execSQL("ALTER TABLE crop_activities ADD COLUMN isDaily INTEGER NOT NULL DEFAULT 0")
    }
}

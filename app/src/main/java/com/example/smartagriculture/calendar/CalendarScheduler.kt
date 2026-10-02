package com.example.smartagriculture.calendar

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

/**
 * Helper to schedule and cancel WorkManager reminder jobs for Crop Activities.
 */
class CalendarScheduler(private val context: Context) {

    fun scheduleReminder(
        activityId: Long,
        cropName: String,
        activityTitle: String,
        activityType: String,
        scheduledTimeMillis: Long
    ) {
        val delayMillis = scheduledTimeMillis - System.currentTimeMillis()
        val effectiveDelay = if (delayMillis > 0) delayMillis else 0L

        val data = workDataOf(
            "ID" to activityId,
            "TITLE" to "$cropName: $activityTitle",
            "MESSAGE" to "Scheduled farm activity ($activityType) is due now.",
            "TYPE" to activityType
        )

        val workRequest = OneTimeWorkRequestBuilder<CalendarReminderWorker>()
            .setInitialDelay(effectiveDelay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "crop_reminder_$activityId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelReminder(activityId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork("crop_reminder_$activityId")
    }
}

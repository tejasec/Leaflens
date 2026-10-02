package com.example.smartagriculture.calendar

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * WorkManager Worker to trigger system notifications for scheduled crop activities.
 */
class CalendarReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString("TITLE") ?: "Crop Activity Reminder"
        val message = inputData.getString("MESSAGE") ?: "You have a scheduled crop activity due today."
        val activityType = inputData.getString("TYPE") ?: "GENERAL"
        val activityId = inputData.getLong("ID", System.currentTimeMillis())

        NotificationHelper.showActivityNotification(
            context = applicationContext,
            notificationId = activityId.toInt(),
            title = title,
            message = message,
            activityType = activityType
        )

        return Result.success()
    }
}

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
        val isDaily = inputData.getBoolean("IS_DAILY", false)
        val cropName = inputData.getString("CROP_NAME") ?: "Crop"
        val activityTitle = inputData.getString("ACTIVITY_TITLE") ?: "Daily Task"

        NotificationHelper.showActivityNotification(
            context = applicationContext,
            notificationId = activityId.toInt(),
            title = title,
            message = message,
            activityType = activityType
        )

        // If daily routine, automatically schedule next occurrence for +24 hours
        if (isDaily) {
            val scheduler = CalendarScheduler(applicationContext)
            val nextTime = System.currentTimeMillis() + java.util.concurrent.TimeUnit.DAYS.toMillis(1)
            scheduler.scheduleReminder(
                activityId = activityId,
                cropName = cropName,
                activityTitle = activityTitle,
                activityType = activityType,
                scheduledTimeMillis = nextTime,
                isDaily = true
            )
        }

        return Result.success()
    }
}

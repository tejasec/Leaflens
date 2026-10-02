package com.example.smartagriculture.calendar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.smartagriculture.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver triggered on system reboot (BOOT_COMPLETED).
 * Reschedules all upcoming pending reminders into WorkManager.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context.applicationContext)
                    val scheduler = CalendarScheduler(context.applicationContext)
                    val upcoming = db.cropActivityDao().getUpcomingActivities(System.currentTimeMillis())

                    for (activity in upcoming) {
                        scheduler.scheduleReminder(
                            activityId = activity.id,
                            cropName = activity.cropName,
                            activityTitle = activity.activityTitle,
                            activityType = activity.activityType,
                            scheduledTimeMillis = activity.scheduledDate
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}

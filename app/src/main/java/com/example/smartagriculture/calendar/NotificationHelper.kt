package com.example.smartagriculture.calendar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.smartagriculture.R
import com.example.smartagriculture.activities.MainActivity

object NotificationHelper {

    const val CHANNEL_ID = "leaflens_crop_reminders"
    private const val CHANNEL_NAME = "Crop Activity Reminders"
    private const val CHANNEL_DESCRIPTION = "Alerts and reminders for scheduled farm and crop activities"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showActivityNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        activityType: String
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val emoji = when (activityType.uppercase()) {
            "FERTILIZER" -> "🌱"
            "WATERING" -> "💧"
            "SPRAY" -> "🛡️"
            "HARVEST" -> "🌾"
            else -> "📅"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("$emoji $title")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Handled if POST_NOTIFICATIONS is denied by user
        }
    }
}

package com.example

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("iptv_manager_prefs", Context.MODE_PRIVATE)
            val enabled = prefs.getBoolean("daily_notification_enabled", true)
            val hour = prefs.getInt("daily_notification_hour", 9)
            val minute = prefs.getInt("daily_notification_minute", 0)
            if (enabled) {
                scheduleDailyNotification(context, hour, minute, true)
            }
            return
        }

        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val prefs = context.getSharedPreferences("iptv_manager_prefs", Context.MODE_PRIVATE)
                val enabled = prefs.getBoolean("daily_notification_enabled", true)
                val hour = prefs.getInt("daily_notification_hour", 9)
                val minute = prefs.getInt("daily_notification_minute", 0)

                if (enabled) {
                    val db = AppDatabase.getDatabase(context)
                    val subscriptions = db.clientSubscriptionDao().getAllSubscriptions().first()
                    
                    val toContactCount = subscriptions.count {
                        !it.isContacted && !it.noPhoneFound &&
                        !it.status.equals("Expired", ignoreCase = true) &&
                        !it.remainingTimeRaw.equals("Expired", ignoreCase = true) &&
                        it.getRemainingDays() <= 10
                    }

                    if (toContactCount > 0) {
                        showSummaryNotification(context, toContactCount)
                    }
                    
                    // Reschedule for tomorrow
                    rescheduleForTomorrow(context, hour, minute)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showSummaryNotification(context: Context, count: Int) {
        val channelId = "iptv_daily_summary_channel"
        val channelName = "Résumé des relances"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notification quotidienne résumant les clients à relancer"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            8888,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Relances clients aujourd'hui")
            .setContentText("Vous avez $count client${if (count > 1) "s" else ""} à relancer aujourd'hui.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            notificationManager.notify(7777, builder.build())
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    private fun rescheduleForTomorrow(context: Context, hour: Int, minute: Int) {
        scheduleDailyNotification(context, hour, minute, true, forceTomorrow = true)
    }

    companion object {
        fun scheduleDailyNotification(
            context: Context,
            hour: Int,
            minute: Int,
            enabled: Boolean,
            forceTomorrow: Boolean = false
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                9999,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (!enabled) {
                alarmManager.cancel(pendingIntent)
                return
            }

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                
                if (forceTomorrow || timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    }
}

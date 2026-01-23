package com.melikenurozun.webtoapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.melikenurozun.webtoapp.MainActivity
import com.melikenurozun.webtoapp.R
import com.melikenurozun.webtoapp.data.local.DataStoreManager
import kotlinx.coroutines.flow.first

class NotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val dataStoreManager = DataStoreManager(context)
        val notificationsEnabled = dataStoreManager.notificationsEnabled.first()

        if (notificationsEnabled) {
            showNotification()
        }

        return Result.success()
    }

    private fun showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily News"
            val descriptionText = "Daily news reminder"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("DAILY_NEWS", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "DAILY_NEWS")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Varsayılan ikon, özel ikon varsa onu kullan
            .setContentTitle("BBC News")
            .setContentText("Check out today's headline news!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(1001, builder.build())
        } catch (e: SecurityException) {
            // İzin verilmemişse hata verebilir
        }
    }
}

package com.monuk7735.nope.remote.utils


import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import com.monuk7735.nope.remote.R
import com.monuk7735.nope.remote.SettingsActivity

object NotificationHelper {

    const val CHANNEL_ID_DOWNLOAD = "RepoDownloadChannel"
    const val NOTIFICATION_ID_DOWNLOAD = 12345

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Repository Download"
            val descriptionText = "Notifications for repository download progress"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID_DOWNLOAD, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createDownloadNotification(
        context: Context,
        content: String,
        progress: Int,
        indeterminate: Boolean
    ): Notification {
        createNotificationChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_DOWNLOAD)
            .setContentTitle("Repository Download")
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (indeterminate) {
            builder.setProgress(100, 0, true)
        } else {
            builder.setProgress(100, progress, false)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.foregroundServiceBehavior = Notification.FOREGROUND_SERVICE_IMMEDIATE
        }

        return builder.build()
    }

    fun updateDownloadNotification(
        context: Context,
        content: String,
        progress: Int,
        indeterminate: Boolean
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = createDownloadNotification(context, content, progress, indeterminate)
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_DOWNLOAD, notification)
    }
}

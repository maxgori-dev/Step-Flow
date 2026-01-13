package com.example.step_flow

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlin.random.Random

object NotificationHelper {

    private const val CHANNEL_ID = "run_finish_channel"
    private const val CHANNEL_NAME = "Run Completed"
    private const val CHANNEL_DESC = "Notifications shown after you finish a run"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESC
        }

        nm.createNotificationChannel(channel)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showRunFinished(context: Context) {
        ensureChannel(context)

        val messages = listOf(
            "Well done! Run completed 💪",
            "Great job! Keep it up 👟",
            "Finish! You crushed it 🔥",
            "Workout saved. Proud of you 💯",
            "Awesome! One more step forward ✅"
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("StepFlow")
            .setContentText(messages[Random.nextInt(messages.size)])
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(1001, notification)
    }
}

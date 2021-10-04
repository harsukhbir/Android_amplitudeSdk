package com.vybesxapp.base

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.clevertap.android.sdk.CleverTapAPI
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vybesxapp.MyApplication
import com.vybesxapp.R
import com.vybesxapp.ui.feed.notification_details.NotificationDetailsActivity
import com.vybesxapp.utils.Analytics
import kotlin.random.Random


class FirebaseService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Analytics().amplitudeAnalytics(getString(R.string.event_receive_notification))
        if (MyApplication.getCleverTapInstance() != null) Analytics().pushEvent(getString(R.string.event_receive_notification))
        else CleverTapAPI.getDefaultInstance(applicationContext)?.pushEvent(getString(R.string.event_receive_notification))

        val extras = Bundle()
        for ((key, value) in message.data.entries) {
            extras.putString(key, value)
        }

        val info = CleverTapAPI.getNotificationInfo(extras)

        if (info.fromCleverTap) {
            CleverTapAPI.createNotification(applicationContext, extras)
        } else {
            // not from CleverTap handle yourself or pass to another provider
            val intent = Intent(this, NotificationDetailsActivity::class.java)
            intent.putExtra(NotificationDetailsActivity.NOTIFICATION_ID,
                message.data["notification_id"])
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            Log.i(TAG, "onMessageReceived: " + message.data["notification_id"])

//        val pendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
//            addNextIntentWithParentStack(intent)
//            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
//        }
            val pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = getString(R.string.default_notification_channel_id)

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId,
                    "General",
                    NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "News, flash sale and other events"
                    enableLights(true)
                    lightColor = Color.GREEN
                }
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(message.data["title"])
                .setContentText(message.data["message"])
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .build()
            notificationManager.notify(Random.nextInt(), notification)
        }

        Log.i(TAG, "onMessageReceived: " + message.data)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    companion object {
        private const val TAG = "FirebaseMessageService"
    }
}
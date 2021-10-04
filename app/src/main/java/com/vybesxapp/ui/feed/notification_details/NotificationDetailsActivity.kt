package com.vybesxapp.ui.feed.notification_details

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.vybesxapp.R
import com.vybesxapp.base.BaseActivity
import com.vybesxapp.service.ApiClient
import com.vybesxapp.utils.Analytics
import es.dmoral.markdownview.MarkdownView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class NotificationDetailsActivity : BaseActivity() {

    private lateinit var mNotificationContent: MarkdownView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_details)
        Analytics().amplitudeAnalytics("Push Notifications Open")
        Analytics().pushEvent("Push Notifications Open")

        mNotificationContent = findViewById(R.id.notification_content)

        val notificationId = intent.getStringExtra(NOTIFICATION_ID)
        notificationId?.let { retrieveNotificationDetails(it) }
    }

    private fun retrieveNotificationDetails(notificationId: String) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notification = mApiClient.retrieveNotificationDetails(notificationId)
                mNotificationContent.loadFromText(notification.data.content)
                Analytics().amplitudeAnalytics("Feed Detail Screen")
                Analytics().pushEvent("Feed Detail Screen")
            } catch (e: HttpException) {
                Log.e(TAG, "retrieveNotificationDetails: ", e)
            } catch (e: Exception) {

            }
        }

    companion object {
        const val NOTIFICATION_ID = "notification_id"
        const val TAG = "NotificationDetails"
    }
}
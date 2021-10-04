package com.vybesxapp.ui.feed.list_notification

import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import coil.load
import com.vybesxapp.R
import com.vybesxapp.base.BaseViewHolder
import com.vybesxapp.service.domain_model.Notification
import com.vybesxapp.ui.feed.notification_details.NotificationDetailsActivity
import com.vybesxapp.utils.Analytics

class NotificationViewHolder(itemView: View) : BaseViewHolder<Notification>(itemView) {
    override fun onBind(item: Notification) {
        val titleTextView = itemView.findViewById<TextView>(R.id.title)
        val bodyTextView = itemView.findViewById<TextView>(R.id.body)
        val notificationImageView = itemView.findViewById<ImageView>(R.id.notification_image)

        titleTextView.text = item.title
        bodyTextView.text = item.body

        // extract image url from markdown
        val regex = "(?:!\\[(.*?)\\]\\((.*?)\\))".toRegex()
        val extractedImage = regex.find(item.data.content)?.groupValues?.get(2)
        if (extractedImage != null) {
            notificationImageView.visibility = View.VISIBLE
            notificationImageView.load(extractedImage) {
                crossfade(true)
            }
        } else {
            notificationImageView.visibility = View.GONE
        }

        itemView.setOnClickListener {
            Analytics().amplitudeAnalytics("Click Pinned Feed")
            Analytics().pushEvent("Click Pinned Feed")
            val intent = Intent(itemView.context, NotificationDetailsActivity::class.java)
            intent.putExtra(NotificationDetailsActivity.NOTIFICATION_ID, item.id)
            itemView.context.startActivity(intent)
        }
    }
}
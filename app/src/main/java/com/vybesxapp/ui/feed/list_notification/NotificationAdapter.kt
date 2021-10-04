package com.vybesxapp.ui.feed.list_notification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vybesxapp.R
import com.vybesxapp.service.domain_model.Notification


class NotificationAdapter(var notificationList: List<Notification>) :
    RecyclerView.Adapter<NotificationViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.onBind(notificationList[position])
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    fun refreshNotifications(newList: List<Notification>) {
        notificationList = newList
        notifyDataSetChanged()
    }
}
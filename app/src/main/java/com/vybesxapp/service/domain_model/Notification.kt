package com.vybesxapp.service.domain_model;

data class Notification(
    val id: String,
    val title: String,
    val body: String,
    val data: NotificationData,
    val createdAt: Long,
) {
    data class NotificationData(
        val notificationId: String,
        val type: String,
        val content: String,
    )
}


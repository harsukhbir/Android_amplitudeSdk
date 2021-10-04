package com.vybesxapp.service.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path

interface NotificationService {
    @GET("/notifications/")
    suspend fun getNotifications(): ListNotificationResponse

    @GET("/notifications/pinned")
    suspend fun getPinnedNotifications(): ListNotificationResponse

    @GET("/notifications/{id}")
    suspend fun getNotificationDetails(@Path("id") id: String): NotificationResponse
}

data class NetworkNotification(
    @SerializedName("_id")
    val id: String,
    val title: String,
    val body: String,
    val data: Data,
    @SerializedName("created_at")
    val createdAt: Long,
)

data class Data(
    @SerializedName("notification_id")
    val notificationId: String,
    val type: String,
    val content: String,
)

data class NotificationResponse(
    val data: NetworkNotification,
)

data class ListNotificationResponse(
    val data: List<NetworkNotification>,
)
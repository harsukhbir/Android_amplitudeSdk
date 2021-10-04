package com.vybesxapp.service.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserService {
    @GET("/me")
    suspend fun getUserProfile(): UserProfileResponse

    @POST("/me/register_device_token")
    suspend fun registerDeviceToken(@Body request: RegisterDeviceTokenRequest)

    @PUT("me/update-profile")
    suspend fun updateUserProfile(@Body request: UpdateUserRequest)
}

data class RegisterDeviceTokenRequest(
    @SerializedName("device_token")
    val deviceToken: String,
)

data class UserProfileResponse(
    @SerializedName("_id")
    val id: String,
    val name: String?,
    val username: String?,
    val email: String,
    @SerializedName("phone_number")
    val phoneNumber: String,
    @SerializedName("profile_pic_url")
    val profileImage: String?,
    val slug: String?,
)

data class UpdateUserRequest(
    val name: String?,
    val username: String?,
    @SerializedName("phone_number")
    val phoneNumber: String?,
)
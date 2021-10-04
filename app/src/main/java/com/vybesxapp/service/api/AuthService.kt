package com.vybesxapp.service.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthService {
    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("/auth/v2/register_seller")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @GET("/auth/v1/captcha")
    suspend fun retrieveCaptcha(): GetCaptchaResponse

    @POST("/auth/verify_signup_code")
    suspend fun verifyCode(@Body request: VerificationCodeRequest): VerificationCodeResponse
}

data class LoginRequest(
    val email: String,
    val password: String,
)

data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String,
    val user: User,
) {
    data class User(
        @SerializedName("_id")
        val id: String,
        val email: String,
        @SerializedName("phone_number")
        val phoneNumber: String,
        @SerializedName("profile_pic_url")
        val profilePic: String,
        val slug: String,
        @SerializedName("created_at")
        val createdAt: Long,
    )
}

data class RegisterRequest(
    val username: String,
    val name: String,
    val email: String,
    @SerializedName("phone_number")
    val phoneNumber: String,
    val password: String,
    val captcha: String,
    val token: String,
)

data class RegisterResponse(
    val data: RegisteredUserData,
) {
    data class RegisteredUserData(
        @SerializedName("_id")
        val userId: String,
        val email: String,
        var username: String,
    )
}

data class GetCaptchaResponse(
    val data: Captcha,
) {
    data class Captcha(
        val token: String, // jwt token generated based on captcha
        val captcha: String, // base64 image of captcha
    )
}

data class VerificationCodeRequest(
    @SerializedName("_id")
    val userId: String,
    val code: String,
)

data class VerificationCodeResponse(
    val user: User,
    @SerializedName("access_token")
    val accessToken: String,
) {
    data class User(
        @SerializedName("_id")
        val id: String,
        val username: String,
        val name: String,
        val email: String,
        @SerializedName("phone_number")
        val phoneNumber: String,
        @SerializedName("created_at")
        val createdAt: Long,
    )
}
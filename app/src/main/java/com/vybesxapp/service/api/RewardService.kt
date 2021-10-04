package com.vybesxapp.service.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET

interface RewardService {
    @GET("/reward/point_balance")
    suspend fun retrieveCurrentPointBalance(): PointBalanceResponse
}

data class PointBalanceResponse(
    val data: PointBalance,
) {
    data class PointBalance(
        @SerializedName("point_balance")
        val pointBalance: Int,
    )
}
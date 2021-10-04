package com.vybesxapp.service.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET

interface SellerService {
    @GET("/me")
    suspend fun retrieveSellerInfo(): SellerInfoResponse

    @GET("/analytics/total_sale")
    suspend fun retrieveTotalSale(): TotalSaleResponse
}

data class SellerInfoResponse(
    @SerializedName("_id")
    val id: String?,
    val name: String?,
    val email: String?,
    @SerializedName("phone_number")
    val phoneNumber: String?,
    val slug: String?,
)

data class TotalSaleResponse(
    val data: TotalSale,
) {
    data class TotalSale(
        @SerializedName("total_sale")
        val totalSale: Int,
    )
}
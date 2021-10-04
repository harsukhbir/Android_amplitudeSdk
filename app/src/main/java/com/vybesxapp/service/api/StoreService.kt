package com.vybesxapp.service.api

import com.google.gson.annotations.SerializedName
import com.vybesxapp.service.dto.StoreResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface StoreService {
    @GET("/store")
    suspend fun retrieveStoreData(): StoreResponse

    @POST("/store/add_products")
    suspend fun addProductsToStore(@Body request: AddProductsRequest)

    @GET("/offers/products/{id}")
    suspend fun retrieveOfferDetails(@Path("id") id: String): OfferDetailsResponse

    @POST("/store/update_slug")
    suspend fun updateStoreSlug(@Body request: UpdateStoreSlugRequest)
}

data class AddProductsRequest(
    @SerializedName("product_ids")
    val productIds: Array<String>,
)

data class OfferDetailsResponse(
    val data: Offer,
) {
    data class Offer(
        @SerializedName("_id")
        val id: String,
        val code: String,
        @SerializedName("created_at")
        val createdAt: Long,
    )
}

data class UpdateStoreSlugRequest(
    val slug: String
)
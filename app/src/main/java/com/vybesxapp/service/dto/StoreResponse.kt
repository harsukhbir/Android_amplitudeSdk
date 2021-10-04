package com.vybesxapp.service.dto

import com.google.gson.annotations.SerializedName

data class StoreResponse(
    @SerializedName("data")
    val data: StoreData
)

data class StoreData(
    @SerializedName("offers")
    val offers: List<OfferItem>,
    val store: Store
) {
    data class OfferItem(
        @SerializedName("_id")
        var id: String,
        val code: String,
        var product: Product,
        @SerializedName("created_at")
        var createdAt: Long
    )
    data class Product(
        @SerializedName("_id")
        val id:String,
        val brand: String,
        val description: String,
        @SerializedName("image_url")
        val imageUrl: String,
        val name: String,
        var pricing: Pricing,
    )
    data class Pricing(
        val retail: Int,
        val sale: Int
    )

    data class Store(
        @SerializedName("shop_link")
        val shopLink: String,
        val slug: String
    )
}




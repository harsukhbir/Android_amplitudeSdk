package com.vybesxapp.service.dto

import com.google.gson.annotations.SerializedName

data class ProductDetailsResponse(
    @SerializedName("_id")
    val id: String,
    val brand: String,
    val description: String,
    @SerializedName("image_url")
    val imageUrl: String,
    val name: String,
    var pricing: Pricing,
) {
    data class Pricing(
        val retail: Int,
        val sale: Int,
    )
}
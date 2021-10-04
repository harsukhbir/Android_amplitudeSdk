package com.vybesxapp.service.dto

import com.google.gson.annotations.SerializedName

data class ProductRecommendResponse (
    @SerializedName("_id")
    val id:String,
    val brand: String,
    val description: String,
    @SerializedName("image_url")
    val imageUrl: String,
    val name: String,
    var pricing: StoreData.Pricing
)
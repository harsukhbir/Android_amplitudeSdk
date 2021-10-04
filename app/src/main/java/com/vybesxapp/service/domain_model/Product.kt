package com.vybesxapp.service.domain_model

data class Product(
    val id: String,
    val brand: String?,
    val description: String?,
    val name: String,
    val imageUrl: String,
    val pricing: Pricing
) {
    data class Pricing(
        val retailPrice: Int,
        val salePrice: Int
    )
}
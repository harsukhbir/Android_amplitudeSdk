package com.vybesxapp.service.api

import com.vybesxapp.service.dto.ProductDetailsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface CatalogService {
    @GET("/catalog/products/{id}")
    suspend fun retrieveProductDetails(@Path("id") id: String): ProductDetailsResponse
}
package com.vybesxapp.service.api

import com.vybesxapp.service.dto.ProductRecommendResponse
import retrofit2.Call
import retrofit2.http.GET

interface RecommendProductService {
    @GET("/recommender")
    suspend fun recommendProducts(): List<ProductRecommendResponse>
}
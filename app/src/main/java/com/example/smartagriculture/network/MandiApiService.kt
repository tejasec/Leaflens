package com.example.smartagriculture.network

import retrofit2.http.GET
import retrofit2.http.Query

interface MandiApiService {
    @GET("resource/9ef84268-d588-465a-a308-a864a43d0070")
    suspend fun getMandiPrices(
        @Query("api-key") apiKey: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 20,
        @Query("filters[commodity]") commodity: String? = null
    ): MandiResponse
}

data class MandiResponse(
    val records: List<MandiRecord>?
)

data class MandiRecord(
    val state: String?,
    val district: String?,
    val market: String?,
    val commodity: String?,
    val variety: String?,
    val arrival_date: String?,
    val min_price: String?,
    val max_price: String?,
    val modal_price: String?
)

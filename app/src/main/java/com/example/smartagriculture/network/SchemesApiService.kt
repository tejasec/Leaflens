package com.example.smartagriculture.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET

data class GovtSchemeResponse(
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("eligibility") val eligibility: String? = null,
    @SerializedName("details") val details: String? = null,
    @SerializedName("subsidyAmount") val subsidyAmount: String? = null,
    @SerializedName("applicationUrl") val applicationUrl: String? = null
)

interface SchemesApiService {
    @GET("api/v1/schemes/online")
    suspend fun fetchOnlineSchemes(): Response<List<GovtSchemeResponse>>
}

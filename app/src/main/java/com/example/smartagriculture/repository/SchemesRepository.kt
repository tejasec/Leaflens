package com.example.smartagriculture.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.smartagriculture.database.AppDatabase
import com.example.smartagriculture.database.SchemeEntity
import com.example.smartagriculture.model.Scheme
import com.example.smartagriculture.network.GovtSchemeResponse
import com.example.smartagriculture.network.SchemesApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class SchemesRepository(private val context: Context) {

    private val schemeDao = AppDatabase.getDatabase(context.applicationContext).schemeDao()

    private val okHttpClient: OkHttpClient by lazy {
        val cacheSize = 10L * 1024L * 1024L // 10 MB Cache
        val cacheDir = File(context.cacheDir, "schemes_http_cache")
        val cache = Cache(cacheDir, cacheSize)

        val offlineInterceptor = Interceptor { chain ->
            var request = chain.request()
            if (!isNetworkAvailable(context)) {
                // 7 days stale cache allowed if offline
                val maxStale = 60 * 60 * 24 * 7
                request = request.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
                    .build()
            }
            chain.proceed(request)
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(offlineInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private val apiService: SchemesApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/") // Local FastAPI emulator base URL / production backend
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SchemesApiService::class.java)
    }

    suspend fun getSchemes(): List<Scheme> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.fetchOnlineSchemes()
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val networkSchemes = response.body()!!
                val entities = networkSchemes.mapIndexed { index, resp ->
                    SchemeEntity(
                        id = resp.id ?: "scheme_$index",
                        title = resp.title ?: "Government Scheme",
                        description = resp.description ?: "Agricultural welfare scheme details.",
                        eligibility = resp.eligibility ?: "All Eligible Farmers",
                        details = resp.details ?: (resp.description ?: "Agricultural welfare scheme details."),
                        applicationUrl = resp.applicationUrl ?: "",
                        subsidyAmount = resp.subsidyAmount ?: ""
                    )
                }
                schemeDao.clearAll()
                schemeDao.insertAll(entities)
                return@withContext entities.map { it.toDomainScheme() }
            }
        } catch (_: Throwable) {
            // Network request failed or parse error occurred, fallback to local Room cache
        }

        // 1. Fallback to cached schemes in Room SQLite database
        val cachedEntities = schemeDao.getSchemesList()
        if (cachedEntities.isNotEmpty()) {
            return@withContext cachedEntities.map { it.toDomainScheme() }
        }

        // 2. Fallback to hardcoded verified agricultural schemes
        return@withContext getOfflineFallbackSchemes()
    }

    private fun SchemeEntity.toDomainScheme(): Scheme {
        return Scheme(
            title = this.title,
            description = this.description,
            eligibility = this.eligibility,
            fullDetails = this.details,
            link = this.applicationUrl
        )
    }

    private fun getOfflineFallbackSchemes(): List<Scheme> {
        return listOf(
            Scheme(
                "PM-Kisan Samman Nidhi",
                "Direct income support of ₹6,000 per year to farmer families.",
                "Small and marginal farmers",
                "Pradhan Mantri Kisan Samman Nidhi (PM-KISAN) is a Central Sector scheme with 100% funding from Government of India. Under the Scheme an income support of Rs.6000/- per year is provided to all farmer families across the country in three equal installments of Rs.2000/- each every four months.",
                "https://pmkisan.gov.in/"
            ),
            Scheme(
                "Pradhan Mantri Fasal Bima Yojana",
                "Provides comprehensive crop insurance cover against non-preventable natural risks.",
                "All farmers growing notified crops",
                "PMFBY aims to provide insurance coverage and financial support to the farmers in the event of failure of any of the notified crop as a result of natural calamities, pests & diseases. It helps stabilize the income of farmers to ensure their continuance in farming.",
                "https://pmfby.gov.in/"
            ),
            Scheme(
                "Paramparagat Krishi Vikas Yojana",
                "Promotes organic farming through a cluster approach and PGS certification.",
                "All farmers interested in organic farming",
                "PKVY is an elaborated component of Soil Health Management (SHM) of major project National Mission of Sustainable Agriculture (NMSA). Under PKVY Organic farming is promoted through adoption of organic village by cluster approach and PGS certification.",
                "https://pgsindia-ncof.gov.in/pkvy/index.aspx"
            ),
            Scheme(
                "Soil Health Card Scheme",
                "Issues soil health cards to farmers to help them use fertilizers optimally.",
                "All farmers",
                "The scheme aims at promoting soil test based and balanced use of fertilizers to enable farmers to realize higher yields at lower cost. The Soil Health Card contains the status of his soil with respect to 12 parameters.",
                "https://soilhealth.dac.gov.in/"
            ),
            Scheme(
                "Kisan Credit Card (KCC)",
                "Provides timely credit support for cultivation and other needs.",
                "Farmers, tenant farmers, sharecroppers",
                "The KCC scheme was introduced to ensure that farmers have easy and timely access to credit for their agricultural operations and other needs. It offers flexible repayment options and lower interest rates compared to regular loans.",
                "https://www.rbi.org.in/"
            )
        )
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

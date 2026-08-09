package com.example.data.api

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

data class CreateCodeRequest(
    val title: String,
    val content: String,
    val isDynamic: Boolean,
    val style: String,
    val colorHex: String?,
    val eyeColorHex: String?,
    val innerEyeColorHex: String?
)

data class CreateCodeResponse(
    val code: String,       // Hashed or shortened path code (e.g. "x7p3a")
    val shortUrl: String,   // Fully qualified dynamic redirection URL (e.g. "https://sayanthrock.github.io/Rock-QR-Code/redirect?code=x7p3a")
    val success: Boolean
)

data class ScanEvent(
    val timestamp: Long,
    val visitorId: String,  // Hashed anonymous identifier
    val os: String,         // Operating System (Android, iOS, Windows, etc.)
    val browser: String,    // Browser (Chrome, Safari, Firefox, etc.)
    val location: String    // Vercel Edge Geo location (e.g. "San Jose, US")
)

data class AnalyticsResponse(
    val code: String,
    val scanCount: Int,
    val scans: List<ScanEvent>,
    val success: Boolean
)

interface QrBackendApi {
    @POST("api/codes")
    suspend fun createDynamicCode(
        @Body request: CreateCodeRequest,
        @Header("X-Device-ID") deviceId: String
    ): CreateCodeResponse

    @GET("api/analytics/{code}")
    suspend fun getCodeAnalytics(
        @Path("code") code: String,
        @Header("X-Device-ID") deviceId: String
    ): AnalyticsResponse

    @DELETE("api/codes/{code}")
    suspend fun deleteDynamicCode(
        @Path("code") code: String,
        @Header("X-Device-ID") deviceId: String
    ): retrofit2.Response<Unit>
}

object QrBackendClient {
    // Developers can customize this Base URL to match their deployed Vercel endpoint
    private const val DEFAULT_BASE_URL = "https://sayanth-rock-qr-backend.vercel.app/"

    val api: QrBackendApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(DEFAULT_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(QrBackendApi::class.java)
    }
}

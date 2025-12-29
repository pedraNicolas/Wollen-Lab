package com.pedra.wollenlab.network.api

import com.pedra.wollenlab.network.dtos.GeminiRequest
import com.pedra.wollenlab.network.dtos.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApi {
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}


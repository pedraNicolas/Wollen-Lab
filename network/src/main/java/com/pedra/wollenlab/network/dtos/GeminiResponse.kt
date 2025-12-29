package com.pedra.wollenlab.network.dtos

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: ResponseContent,
    val finishReason: String?,
    val index: Int,
    val safetyRatings: List<SafetyRating>?
)

@JsonClass(generateAdapter = true)
data class ResponseContent(
    val parts: List<Part>,
    val role: String? = null
)

@JsonClass(generateAdapter = true)
data class SafetyRating(
    val category: String,
    val probability: String
)


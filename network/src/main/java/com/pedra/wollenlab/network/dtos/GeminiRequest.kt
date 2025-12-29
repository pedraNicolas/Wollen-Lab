package com.pedra.wollenlab.network.dtos

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<Content>
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>,
    val role: String? = null
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)


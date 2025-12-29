package com.pedra.wollenlab.domain.model

data class Conversation(
    val id: String,
    val createdAt: Long,
    val updatedAt: Long,
    val title: String?
)
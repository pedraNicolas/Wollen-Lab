package com.pedra.wollenlab.model

data class Message(
    val id: String,
    val content: String,
    val role: MessageRole,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}


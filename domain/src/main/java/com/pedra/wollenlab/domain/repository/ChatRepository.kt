package com.pedra.wollenlab.domain.repository

import com.pedra.wollenlab.model.Message

interface ChatRepository {
    suspend fun sendMessage(messages: List<Message>): Result<String>
}


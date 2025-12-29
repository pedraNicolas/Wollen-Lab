package com.pedra.wollenlab.domain.usecase

import com.pedra.wollenlab.domain.repository.ChatRepository
import com.pedra.wollenlab.model.Message
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(messages: List<Message>): Result<String> {
        return chatRepository.sendMessage(messages)
    }
}


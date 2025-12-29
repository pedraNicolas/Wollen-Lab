package com.pedra.wollenlab.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedra.wollenlab.domain.repository.ConversationRepository
import com.pedra.wollenlab.domain.usecase.SendChatMessageUseCase
import com.pedra.wollenlab.model.Message
import com.pedra.wollenlab.model.MessageRole
import com.pedra.wollenlab.presentation.state.ChatUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val conversationRepository: ConversationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentConversationId: String? = null
    private var currentSendJob: Job? = null

    fun loadConversation(conversationId: String) {
        // Cancelar cualquier request en vuelo
        currentSendJob?.cancel()
        currentSendJob = null

        currentConversationId = conversationId
        viewModelScope.launch {
            val messages = conversationRepository.getMessagesByConversationIdSync(conversationId)
            _uiState.update { it.copy(messages = messages) }
        }
    }

    fun createNewConversation() {
        // Cancelar cualquier request en vuelo
        currentSendJob?.cancel()
        currentSendJob = null

        // Solo limpiar la pantalla, la conversación se creará al enviar el primer mensaje
        currentConversationId = null
        _uiState.update { it.copy(messages = emptyList(), inputText = "") }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        if (!_uiState.value.isInputEnabled) return

        // Cancelar cualquier request previo en vuelo
        currentSendJob?.cancel()

        val userMessage = Message(
            id = UUID.randomUUID().toString(),
            content = text.trim(),
            role = MessageRole.USER
        )

        _uiState.update { currentState ->
            currentState.copy(
                messages = currentState.messages + userMessage,
                isLoading = true,
                inputText = ""
            )
        }

        currentSendJob = viewModelScope.launch {
            sendChatMessageUseCase(currentConversationId, userMessage)
                .onSuccess { result ->
                    // Actualizar currentConversationId si se creó una nueva conversación
                    if (currentConversationId == null) {
                        currentConversationId = result.conversationId
                    }
                    
                    // Verificar que seguimos en la misma conversación antes de actualizar
                    if (currentConversationId == result.conversationId) {
                        _uiState.update { currentState ->
                            currentState.copy(
                                messages = currentState.messages + result.assistantMessage,
                                isLoading = false
                            )
                        }
                    }
                    currentSendJob = null
                }
                .onFailure { error ->
                    // Verificar que seguimos en la misma conversación antes de actualizar
                    if (currentConversationId != null) {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                error = error.message
                            )
                        }
                    } else {
                        // Si no había conversación, limpiar el estado
                        _uiState.update { currentState ->
                            currentState.copy(
                                messages = currentState.messages.dropLast(1), // Remover el mensaje del usuario que falló
                                isLoading = false,
                                error = error.message
                            )
                        }
                    }
                    currentSendJob = null
                }
        }
    }

    fun updateInputText(text: String) {
        // No permitir escribir mientras está cargando o creando conversación
        if (!_uiState.value.isInputEnabled) return
        _uiState.update { it.copy(inputText = text) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}


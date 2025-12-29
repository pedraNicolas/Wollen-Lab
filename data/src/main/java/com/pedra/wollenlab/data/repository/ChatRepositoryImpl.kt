package com.pedra.wollenlab.data.repository

import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.pedra.wollenlab.domain.repository.ChatRepository
import com.pedra.wollenlab.model.Message
import com.pedra.wollenlab.model.MessageRole
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

class ChatRepositoryImpl @Inject constructor(
    @Named("GeminiApiKey") private val apiKey: String
) : ChatRepository {

    private val generativeModel: GenerativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = apiKey
        )
    }

    private var chat: Chat? = null

    override suspend fun sendMessage(messages: List<Message>): Result<String> {
        return try {
            if (apiKey.isEmpty()) {
                throw Exception("API Key is empty. Please check local.properties file.")
            }

            // Obtener el último mensaje (del usuario)
            val lastMessage = messages.lastOrNull()
                ?: throw Exception("No hay mensajes para enviar")

            if (lastMessage.role != MessageRole.USER) {
                throw Exception("El último mensaje debe ser del usuario")
            }

            // Si es el primer mensaje o necesitamos reconstruir el chat con historial
            if (chat == null || messages.size > 1) {
                // Construir historial desde los mensajes anteriores (excepto el último)
                // Filtrar mensajes SYSTEM ya que solo se usan como contexto
                val history = if (messages.size > 1) {
                    messages.dropLast(1)
                        .filter { it.role != MessageRole.SYSTEM }
                        .map { message ->
                            content(role = when (message.role) {
                                MessageRole.USER -> "user"
                                MessageRole.ASSISTANT -> "model"
                                MessageRole.SYSTEM -> "user" // No debería llegar aquí por el filter
                            }) {
                                text(message.content)
                            }
                        }
                } else {
                    emptyList()
                }
                chat = generativeModel.startChat(history = history)
            }

            // Enviar el mensaje usando el chat
            val response = chat?.sendMessage(lastMessage.content)
                ?: throw Exception("Chat no inicializado")

            val responseText = response.text ?: throw Exception("No se recibió respuesta del modelo")

            Result.success(responseText)
        } catch (e: IOException) {
            val errorMsg = "Error de red: ${e.message}. Verifica tu conexión a internet."
            Result.failure(Exception(errorMsg))
        } catch (e: Exception) {
            val errorMsg = when {
                e.message?.contains("API_KEY", ignoreCase = true) == true -> 
                    "API Key inválida o no autorizada. Verifica tu API key en local.properties"
                e.message?.contains("quota", ignoreCase = true) == true || 
                e.message?.contains("limit", ignoreCase = true) == true -> 
                    "Límite de solicitudes excedido. Intenta más tarde."
                else -> "Error: ${e.message ?: "Error desconocido"}"
            }
            Result.failure(Exception(errorMsg))
        }
    }
}


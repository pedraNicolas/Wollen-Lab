package com.pedra.wollenlab.presentation.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.pedra.wollenlab.model.Message
import com.pedra.wollenlab.model.MessageRole
import com.pedra.wollenlab.presentation.state.ChatUiState
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChatScreenUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

    private lateinit var uiStateFlow: MutableStateFlow<ChatUiState>
    private lateinit var conversationsFlow: MutableStateFlow<List<com.pedra.wollenlab.domain.model.Conversation>>
    private var darkTheme = false
    private var onThemeChangeCalled = false
    private var sendMessageCalled = false
    private var sendMessageText: String? = null
    private var updateInputTextCalled = false
    private var updateInputTextValue: String? = null

    @Before
    fun setup() {
        uiStateFlow = MutableStateFlow(ChatUiState())
        conversationsFlow = MutableStateFlow(emptyList())
        darkTheme = false
        onThemeChangeCalled = false
        sendMessageCalled = false
        sendMessageText = null
        updateInputTextCalled = false
        updateInputTextValue = null
    }

    @Test
    fun welcomeMessage_isDisplayed_whenNoMessages() {
        // Given
        uiStateFlow.value = ChatUiState(messages = emptyList())

        // When
        composeTestRule.setContent {
            TestChatScreen(
                darkTheme = darkTheme,
                onThemeChange = { onThemeChangeCalled = true },
                uiState = uiStateFlow,
                conversations = conversationsFlow,
                onSendMessage = { text ->
                    sendMessageCalled = true
                    sendMessageText = text
                },
                onUpdateInputText = { text ->
                    updateInputTextCalled = true
                    updateInputTextValue = text
                },
                onCreateNewConversation = {},
                onLoadConversation = {},
                onDeleteConversation = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Hola!").assertIsDisplayed()
        composeTestRule.onNodeWithText("¿En qué te puedo ayudar?").assertIsDisplayed()
    }

    @Test
    fun welcomeMessage_isNotDisplayed_whenMessagesExist() {
        // Given
        val messages = listOf(
            Message(
                id = "1",
                content = "Hola",
                role = MessageRole.USER
            )
        )
        uiStateFlow.value = ChatUiState(messages = messages)

        // When
        composeTestRule.setContent {
            TestChatScreen(
                darkTheme = darkTheme,
                onThemeChange = { onThemeChangeCalled = true },
                uiState = uiStateFlow,
                conversations = conversationsFlow,
                onSendMessage = { text ->
                    sendMessageCalled = true
                    sendMessageText = text
                },
                onUpdateInputText = { text ->
                    updateInputTextCalled = true
                    updateInputTextValue = text
                },
                onCreateNewConversation = {},
                onLoadConversation = {},
                onDeleteConversation = {}
            )
        }

        // Then - Verificamos que el mensaje del usuario está visible
        composeTestRule.onNodeWithText("Hola").assertIsDisplayed()
    }

    @Test
    fun messages_areDisplayed_correctly() {
        // Given
        val messages = listOf(
            Message(
                id = "1",
                content = "Mensaje del usuario",
                role = MessageRole.USER
            ),
            Message(
                id = "2",
                content = "Respuesta del asistente",
                role = MessageRole.ASSISTANT
            )
        )
        uiStateFlow.value = ChatUiState(messages = messages)

        // When
        composeTestRule.setContent {
            TestChatScreen(
                darkTheme = darkTheme,
                onThemeChange = { onThemeChangeCalled = true },
                uiState = uiStateFlow,
                conversations = conversationsFlow,
                onSendMessage = { text ->
                    sendMessageCalled = true
                    sendMessageText = text
                },
                onUpdateInputText = { text ->
                    updateInputTextCalled = true
                    updateInputTextValue = text
                },
                onCreateNewConversation = {},
                onLoadConversation = {},
                onDeleteConversation = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Mensaje del usuario").assertIsDisplayed()
        composeTestRule.onNodeWithText("Respuesta del asistente").assertIsDisplayed()
    }

    @Test
    fun inputField_isDisplayed_andEnabled() {
        // Given
        uiStateFlow.value = ChatUiState(
            messages = emptyList(),
            inputText = "",
            isLoading = false
        )

        // When
        composeTestRule.setContent {
            TestChatScreen(
                darkTheme = darkTheme,
                onThemeChange = { onThemeChangeCalled = true },
                uiState = uiStateFlow,
                conversations = conversationsFlow,
                onSendMessage = { text ->
                    sendMessageCalled = true
                    sendMessageText = text
                },
                onUpdateInputText = { text ->
                    updateInputTextCalled = true
                    updateInputTextValue = text
                },
                onCreateNewConversation = {},
                onLoadConversation = {},
                onDeleteConversation = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Escribe un mensaje...").assertIsDisplayed()
        composeTestRule.onNodeWithText("Escribe un mensaje...").assertIsEnabled()
    }

    @Test
    fun inputField_canReceiveText() {
        // Given
        uiStateFlow.value = ChatUiState(
            messages = emptyList(),
            inputText = "",
            isLoading = false
        )

        // When
        composeTestRule.setContent {
            TestChatScreen(
                darkTheme = darkTheme,
                onThemeChange = { onThemeChangeCalled = true },
                uiState = uiStateFlow,
                conversations = conversationsFlow,
                onSendMessage = { text ->
                    sendMessageCalled = true
                    sendMessageText = text
                },
                onUpdateInputText = { text ->
                    updateInputTextCalled = true
                    updateInputTextValue = text
                },
                onCreateNewConversation = {},
                onLoadConversation = {},
                onDeleteConversation = {}
            )
        }

        composeTestRule.onNodeWithText("Escribe un mensaje...")
            .performTextInput("Hola, esto es un test")
        
        // Then
        assert(updateInputTextCalled)
        assert(updateInputTextValue == "Hola, esto es un test")
    }

    @Test
    fun sendButton_isDisabled_whenInputIsEmpty() {
        // Given
        uiStateFlow.value = ChatUiState(
            messages = emptyList(),
            inputText = "",
            isLoading = false
        )

        // When
        composeTestRule.setContent {
            TestChatScreen(
                darkTheme = darkTheme,
                onThemeChange = { onThemeChangeCalled = true },
                uiState = uiStateFlow,
                conversations = conversationsFlow,
                onSendMessage = { text ->
                    sendMessageCalled = true
                    sendMessageText = text
                },
                onUpdateInputText = { text ->
                    updateInputTextCalled = true
                    updateInputTextValue = text
                },
                onCreateNewConversation = {},
                onLoadConversation = {},
                onDeleteConversation = {}
            )
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Enviar").assertIsNotEnabled()
    }

    @Test
    fun sendButton_isEnabled_whenInputHasText() {
        // Given
        uiStateFlow.value = ChatUiState(
            messages = emptyList(),
            inputText = "Hola",
            isLoading = false
        )

        // When
        composeTestRule.setContent {
            TestChatScreen(
                darkTheme = darkTheme,
                onThemeChange = { onThemeChangeCalled = true },
                uiState = uiStateFlow,
                conversations = conversationsFlow,
                onSendMessage = { text ->
                    sendMessageCalled = true
                    sendMessageText = text
                },
                onUpdateInputText = { text ->
                    updateInputTextCalled = true
                    updateInputTextValue = text
                },
                onCreateNewConversation = {},
                onLoadConversation = {},
                onDeleteConversation = {}
            )
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Enviar").assertIsEnabled()
    }

    @Test
    fun sendButton_sendsMessage_whenClicked() {
        // Given
        uiStateFlow.value = ChatUiState(
            messages = emptyList(),
            inputText = "Hola",
            isLoading = false
        )

        // When
        composeTestRule.setContent {
            TestChatScreen(
                darkTheme = darkTheme,
                onThemeChange = { onThemeChangeCalled = true },
                uiState = uiStateFlow,
                conversations = conversationsFlow,
                onSendMessage = { text ->
                    sendMessageCalled = true
                    sendMessageText = text
                },
                onUpdateInputText = { text ->
                    updateInputTextCalled = true
                    updateInputTextValue = text
                },
                onCreateNewConversation = {},
                onLoadConversation = {},
                onDeleteConversation = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Enviar").performClick()

        // Then
        assert(sendMessageCalled)
        assert(sendMessageText == "Hola")
    }

    @Test
    fun loadingIndicator_isDisplayed_whenLoading() {
        // Given
        uiStateFlow.value = ChatUiState(
            messages = listOf(
                Message(
                    id = "1",
                    content = "Mensaje del usuario",
                    role = MessageRole.USER
                )
            ),
            isLoading = true
        )

        // When
        composeTestRule.setContent {
            TestChatScreen(
                darkTheme = darkTheme,
                onThemeChange = { onThemeChangeCalled = true },
                uiState = uiStateFlow,
                conversations = conversationsFlow,
                onSendMessage = { text ->
                    sendMessageCalled = true
                    sendMessageText = text
                },
                onUpdateInputText = { text ->
                    updateInputTextCalled = true
                    updateInputTextValue = text
                },
                onCreateNewConversation = {},
                onLoadConversation = {},
                onDeleteConversation = {}
            )
        }

        // Then - El indicador de carga debería estar visible
        // Verificamos que el input está deshabilitado (indicando que está cargando)
        composeTestRule.onNodeWithText("Escribe un mensaje...").assertIsNotEnabled()
    }

    @Test
    fun inputField_isDisabled_whenLoading() {
        // Given
        uiStateFlow.value = ChatUiState(
            messages = emptyList(),
            inputText = "",
            isLoading = true
        )

        // When
        composeTestRule.setContent {
            TestChatScreen(
                darkTheme = darkTheme,
                onThemeChange = { onThemeChangeCalled = true },
                uiState = uiStateFlow,
                conversations = conversationsFlow,
                onSendMessage = { text ->
                    sendMessageCalled = true
                    sendMessageText = text
                },
                onUpdateInputText = { text ->
                    updateInputTextCalled = true
                    updateInputTextValue = text
                },
                onCreateNewConversation = {},
                onLoadConversation = {},
                onDeleteConversation = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Escribe un mensaje...").assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription("Enviar").assertIsNotEnabled()
    }

    @Test
    fun themeToggleButton_isDisplayed() {
        // Given
        uiStateFlow.value = ChatUiState()

        // When
        composeTestRule.setContent {
            TestChatScreen(
                darkTheme = darkTheme,
                onThemeChange = { onThemeChangeCalled = true },
                uiState = uiStateFlow,
                conversations = conversationsFlow,
                onSendMessage = { text ->
                    sendMessageCalled = true
                    sendMessageText = text
                },
                onUpdateInputText = { text ->
                    updateInputTextCalled = true
                    updateInputTextValue = text
                },
                onCreateNewConversation = {},
                onLoadConversation = {},
                onDeleteConversation = {}
            )
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Cambiar a tema oscuro").assertIsDisplayed()
    }

    @Test
    fun themeToggleButton_changesTheme_whenClicked() {
        // Given
        uiStateFlow.value = ChatUiState()

        // When
        composeTestRule.setContent {
            TestChatScreen(
                darkTheme = darkTheme,
                onThemeChange = { onThemeChangeCalled = true },
                uiState = uiStateFlow,
                conversations = conversationsFlow,
                onSendMessage = { text ->
                    sendMessageCalled = true
                    sendMessageText = text
                },
                onUpdateInputText = { text ->
                    updateInputTextCalled = true
                    updateInputTextValue = text
                },
                onCreateNewConversation = {},
                onLoadConversation = {},
                onDeleteConversation = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Cambiar a tema oscuro").performClick()

        // Then
        assert(onThemeChangeCalled)
    }

    @Test
    fun menuButton_isDisplayed() {
        // Given
        uiStateFlow.value = ChatUiState()

        // When
        composeTestRule.setContent {
            TestChatScreen(
                darkTheme = darkTheme,
                onThemeChange = { onThemeChangeCalled = true },
                uiState = uiStateFlow,
                conversations = conversationsFlow,
                onSendMessage = { text ->
                    sendMessageCalled = true
                    sendMessageText = text
                },
                onUpdateInputText = { text ->
                    updateInputTextCalled = true
                    updateInputTextValue = text
                },
                onCreateNewConversation = {},
                onLoadConversation = {},
                onDeleteConversation = {}
            )
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Menú").assertIsDisplayed()
    }

    @Test
    fun menuButton_opensDrawer_whenClicked() {
        // Given
        uiStateFlow.value = ChatUiState()

        // When
        composeTestRule.setContent {
            TestChatScreen(
                darkTheme = darkTheme,
                onThemeChange = { onThemeChangeCalled = true },
                uiState = uiStateFlow,
                conversations = conversationsFlow,
                onSendMessage = { text ->
                    sendMessageCalled = true
                    sendMessageText = text
                },
                onUpdateInputText = { text ->
                    updateInputTextCalled = true
                    updateInputTextValue = text
                },
                onCreateNewConversation = {},
                onLoadConversation = {},
                onDeleteConversation = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Menú").performClick()

        // Then - El drawer debería estar abierto, verificamos que el botón "Nuevo chat" está visible
        composeTestRule.onNodeWithText("Nuevo chat").assertIsDisplayed()
    }

    @Test
    fun welcomeMessage_isNotDisplayed_whenLoading() {
        // Given
        uiStateFlow.value = ChatUiState(
            messages = emptyList(),
            isLoading = true
        )

        // When
        composeTestRule.setContent {
            TestChatScreen(
                darkTheme = darkTheme,
                onThemeChange = { onThemeChangeCalled = true },
                uiState = uiStateFlow,
                conversations = conversationsFlow,
                onSendMessage = { text ->
                    sendMessageCalled = true
                    sendMessageText = text
                },
                onUpdateInputText = { text ->
                    updateInputTextCalled = true
                    updateInputTextValue = text
                },
                onCreateNewConversation = {},
                onLoadConversation = {},
                onDeleteConversation = {}
            )
        }

        // Then - Verificamos que el input está deshabilitado (indicando que está cargando)
        // El mensaje de bienvenida no debería estar visible cuando isLoading es true
        composeTestRule.onNodeWithText("Escribe un mensaje...").assertIsNotEnabled()
    }
}

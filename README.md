# Wollen Lab - Chat con IA

## 📱 Descripción

Aplicación Android desarrollada con Jetpack Compose que permite chatear con una IA usando la API de Google Gemini. La app ofrece una experiencia similar a ChatGPT con modo claro y oscuro, historial de conversaciones persistente y gestión inteligente del contexto.

## 🎯 Características principales

- **Chat con IA**: Conversaciones fluidas usando Google Gemini API
- **Historial persistente**: Guardado automático de conversaciones y mensajes con Room
- **Menú lateral**: Navegación entre conversaciones guardadas
- **Modo claro/oscuro**: Tema adaptativo con paleta en blanco y negro
- **Gestión de contexto**: Resumen automático para optimizar tokens al reingresar conversaciones
- **Títulos automáticos**: Generación de títulos basados en el primer mensaje
- **UI moderna**: Interfaz similar a ChatGPT con diseño limpio y minimalista

<img width="388" height="801" alt="image" src="https://github.com/user-attachments/assets/b75f95a8-e0ae-4ffc-a592-acdbfc8e8af8" />

<img width="394" height="800" alt="image" src="https://github.com/user-attachments/assets/2849b29f-e0f0-4225-b130-cd265eb56104" />

<img width="386" height="803" alt="image" src="https://github.com/user-attachments/assets/514f83a6-495b-4785-9fac-24844063f576" />

<img width="390" height="802" alt="image" src="https://github.com/user-attachments/assets/d19150dc-e267-4a7c-ab45-eda83a5f3b0a" />

<img width="388" height="803" alt="image" src="https://github.com/user-attachments/assets/fa6d8df4-308a-4f2a-b032-70a4f8f0c90d" />

<img width="392" height="805" alt="image" src="https://github.com/user-attachments/assets/c93f3a95-b97b-47a0-9c65-3f1abb6ceeff" />



## 🏗️ Arquitectura

### Clean Architecture + MVVM
La aplicación sigue los principios de Clean Architecture con el patrón MVVM (Model-View-ViewModel) en la capa de presentación:

- **app**: Presentación y navegación (MVVM)
- **domain**: Lógica de negocio y casos de uso
- **data**: Repositorios, fuentes de datos y persistencia local
- **network**: Integración con Google AI SDK
- **model**: Modelos de datos compartidos

### Estructura de módulos

```
app/
├── presentation/
│   ├── screen/        # Pantallas Compose
│   ├── viewmodel/     # ViewModels
│   └── state/         # Estados de UI
domain/
├── model/             # Modelos de dominio
├── repository/        # Interfaces de repositorios
└── usecase/           # Casos de uso
data/
├── repository/        # Implementaciones de repositorios
├── local/             # Room database, DAOs, entities
└── mapper/            # Mappers entre capas
model/
└── Message.kt         # Modelo compartido de mensajes
```

### Tecnologías utilizadas

- **Jetpack Compose**: UI declarativa
- **MVVM**: Patrón arquitectónico de presentación
- **Hilt**: Inyección de dependencias
- **Room**: Persistencia local de conversaciones y mensajes
- **Google AI SDK**: Integración con Gemini API
- **Coroutines & Flow**: Programación asíncrona y streams reactivos
- **Lifecycle-aware**: `collectAsStateWithLifecycle` para observación eficiente

## 💬 Funcionalidades del chat

### Gestión de conversaciones

- **Creación automática**: Las conversaciones se crean al enviar el primer mensaje
- **Persistencia**: Todas las conversaciones y mensajes se guardan localmente
- **Navegación**: Menú lateral para acceder al historial completo
- **Eliminación**: Opción para borrar conversaciones individuales

### Optimización de contexto

Para ahorrar tokens al reingresar a una conversación, la app implementa un sistema de resumen:

- **Umbral**: Si hay más de `SUMMARY_THRESHOLD` mensajes, se genera un resumen
- **Estrategia**: Primeros 3 mensajes del usuario + últimos 2 mensajes (usuario + asistente)
- **Prefijo**: El resumen se envía con el prefijo "Contexto previo:" para mantener el contexto

### Títulos automáticos

- Los títulos se generan automáticamente desde el primer mensaje del usuario
- Si el mensaje es muy largo, se trunca a `MAX_TITLE_LENGTH` caracteres
- Si no hay título, se muestra "Nueva conversación"

## 🎨 UI/UX

### Temas

- **Modo claro**: Fondo blanco, texto negro
- **Modo oscuro**: Fondo negro, texto blanco
- **Toggle**: Botón en el header para cambiar entre temas

### Componentes principales

- **TopAppBar**: Header fijo con logo y toggle de tema
- **ModalNavigationDrawer**: Menú lateral para historial de conversaciones
- **LazyColumn**: Lista de mensajes con scroll automático
- **ChatInput**: Campo de entrada con botón de envío
- **WelcomeMessage**: Mensaje de bienvenida cuando el chat está vacío

### Experiencia de usuario

- **Mensaje de bienvenida**: "Hola! ¿En qué te puedo ayudar?" cuando no hay mensajes
- **Estados de carga**: Indicadores visuales durante el envío de mensajes
- **Manejo de errores**: Snackbars para mostrar errores de forma no intrusiva
- **Bloqueo de input**: El input se deshabilita mientras se procesa un mensaje
- **Cancelación de requests**: Al cambiar de conversación, se cancelan requests pendientes

## 🔧 Configuración

### API Key de Gemini

Para usar la aplicación, necesitas configurar tu API Key de Google Gemini:

1. Obtén tu API Key en [Google AI Studio](https://aistudio.google.com/app/apikey)
2. Agrega la key en `local.properties`:
   ```properties
   GEMINI_API_KEY=tu_api_key_aqui
   ```
3. La key se inyecta automáticamente mediante Hilt desde `BuildConfig`

### BuildConfig

El proyecto usa `BuildConfig` para acceder a la API Key de forma segura. Asegúrate de tener configurado:

```kotlin
buildConfigField("String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY")}\"")
```

## 📊 Modelo de datos

### Conversation
```kotlin
data class Conversation(
    val id: String,
    val createdAt: Long,
    val updatedAt: Long,
    val title: String?
)
```

### Message
```kotlin
data class Message(
    val id: String,
    val conversationId: String,
    val role: MessageRole, // USER, ASSISTANT, SYSTEM
    val content: String,
    val createdAt: Long
)
```

### Room Database

- **ConversationEntity**: Tabla de conversaciones
- **MessageEntity**: Tabla de mensajes con foreign key a conversaciones
- **MessageRoleConverter**: Type converter para el enum `MessageRole`

## 🚀 Casos de uso

### SendChatMessageUseCase

Encapsula toda la lógica de negocio para enviar mensajes:

- Crea conversaciones si no existen
- Guarda mensajes en la base de datos
- Genera resúmenes cuando es necesario
- Actualiza títulos automáticamente
- Prepara el contexto para la API

### ConversationRepository

Gestiona las operaciones de persistencia:

- CRUD de conversaciones
- CRUD de mensajes
- Generación de resúmenes
- Consultas optimizadas

## 🧪 Testing

La aplicación está preparada para testing con:

- **Tests unitarios**: Casos de uso y repositorios
- **Tests de UI**: Pantallas y componentes Compose
- **Tests de integración**: Flujos completos de conversación

## 📱 Splash Screen

La app incluye un splash screen personalizado:

- **Icono**: Logo de Wollen Lab en blanco
- **Fondo**: Negro sólido
- **Duración**: 300ms
- **Transición**: Suave hacia el tema principal

## 🎯 Icono de la app

El icono adaptativo usa el mismo diseño del splash screen:

- **Foreground**: Logo de Wollen Lab con padding negro
- **Background**: Fondo negro sólido
- **Adaptativo**: Se adapta a diferentes formas de iconos del sistema


- **Android**: pedranicolas@gmail.com

## 📄 Licencia

Este proyecto no está destinado para uso comercial.

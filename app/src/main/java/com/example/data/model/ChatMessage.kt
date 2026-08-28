package com.example.data.model

import java.util.UUID

enum class MessageSender {
    USER,
    MAYA
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isVoice: Boolean = false,
    val isProcessing: Boolean = false
)

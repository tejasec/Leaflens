package com.example.smartagriculture.model

/**
 * Chat message model for Ask Crop Doctor AI Chatbot.
 */
data class ChatMessage(
    val message: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

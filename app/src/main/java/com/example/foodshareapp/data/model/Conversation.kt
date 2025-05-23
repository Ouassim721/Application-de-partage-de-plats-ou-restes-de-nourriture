package com.example.foodshareapp.data.model

data class Conversation(
    val id: String,
    val username: String,
    val avatarUrl: String?,
    val lastMessage: String,
    val timestamp: Long,
    val isUnread: Boolean,
    val status: ConversationStatus
)

enum class ConversationStatus {
    NEW, CONFIRMED, COMPLETED
}

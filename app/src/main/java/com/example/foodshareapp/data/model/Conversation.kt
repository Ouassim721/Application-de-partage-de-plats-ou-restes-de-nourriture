package com.example.foodshareapp.data.model

data class Conversation(
    val id: String = "",
    val username: String = "",
    val avatarUrl: String? = "",
    val lastMessage: String = "",
    val timestamp: Long = 0L,
    val isUnread: Boolean = true,
    val status: ConversationStatus = ConversationStatus.NEW
)

enum class ConversationStatus {
    NEW, CONFIRMED, COMPLETED
}

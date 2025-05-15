package com.example.foodshareapp.data.model

data class Conversation(
    val user1: String = "",
    val user2: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis()
)

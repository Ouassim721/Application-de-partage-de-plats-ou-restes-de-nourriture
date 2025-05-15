package com.example.foodshareapp.data.model

data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

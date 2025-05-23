package com.example.foodshareapp.data.model

data class Message(
    val id: String,
    val content: String,
    val timestamp: Long,
    val senderId: String
)
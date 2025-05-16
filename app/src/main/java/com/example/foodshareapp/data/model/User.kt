package com.example.foodshareapp.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val city: String = "",
    val profileImageUrl: String = "",
    val role: String = "user"
)

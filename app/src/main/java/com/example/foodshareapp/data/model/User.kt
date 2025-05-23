// User.kt
package com.example.foodshareapp.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val city: String = "",
    val profileImageUrl: String = "",
    val bio: String = "", // Added for user's bio
    val role: String = "user",
    val dishesOfferedCount: Int = 0, // Added for statistics
    val dishesReceivedCount: Int = 0 // Added for statistics
)
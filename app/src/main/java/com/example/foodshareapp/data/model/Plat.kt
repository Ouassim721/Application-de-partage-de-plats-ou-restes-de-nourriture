package com.example.foodshareapp.data.model

data class Plat(
    val titre: String = "",
    val description: String = "",
    val portions: Int = 1,
    val expiration: String = "",
    val localisation: String = "",
    val imageUrl: String = "",
    val reserve : Boolean = false,
    val datePublication: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    val userId: String = ""
)



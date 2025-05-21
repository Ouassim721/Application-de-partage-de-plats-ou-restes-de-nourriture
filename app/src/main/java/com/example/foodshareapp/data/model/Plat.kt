package com.example.foodshareapp.data.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class Plat(
    val titre: String = "",
    val description: String = "",
    val ingredients: String = "",
    val portions: Int = 1,
    val expiration: String = "",
    val localisation: String = "",
    val imageUrl: String = "",
    val reserve: Boolean = false,
    val datePublication: Timestamp = Timestamp.now(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val userId: String = "",
    val statut: String = "",
    val typePlat: List<String> = listOf()
) : Parcelable, Serializable
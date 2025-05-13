package com.example.foodshareapp.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ComposeView
import com.example.foodshareapp.R
import com.example.foodshareapp.data.model.Dish
import com.example.foodshareapp.ui.components.DishList
import com.example.foodshareapp.ui.components.FloatingAddButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import setupBottomNavigation

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val composeView = findViewById<ComposeView>(R.id.compose_view)
        composeView.setContent {
            DishList(getDishes())
            FloatingAddButton()
        }
    }

    private fun getDishes(): List<Dish> {
        return listOf(
            Dish(
                imageUrl = "https://url.to/pates.jpg",
                title = "Pâtes aux légumes",
                chef = "Thomas",
                distance = "500m",
                urgent = false
            ),
            Dish(
                imageUrl = "https://url.to/quiche.jpg",
                title = "Quiche aux légumes",
                chef = "Sophie",
                distance = "1.2km",
                urgent = true
            )
        )
    }
}

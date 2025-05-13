package com.example.foodshareapp.activities

import android.os.Bundle
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.ComposeView
import androidx.appcompat.app.AppCompatActivity
import com.example.foodshareapp.R
import com.example.foodshareapp.ui.theme.FoodShareAppTheme
import com.example.foodshareapp.ui.components.IngredientsList

class MealDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_detail)

        val composeView = findViewById<ComposeView>(R.id.ingredients_compose)
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        composeView.setContent {
            FoodShareAppTheme {
                IngredientsList(
                    ingredients = listOf(
                        "Légumes bio (carottes, courgettes, poireaux)",
                        "Œufs fermiers",
                        "Crème fraîche",
                        "Pâte brisée maison"
                    )
                )
            }
        }
    }
}

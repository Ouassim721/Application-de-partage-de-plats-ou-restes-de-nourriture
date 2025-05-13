package com.example.foodshareapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import com.example.foodshareapp.R
import com.example.foodshareapp.ui.components.DishListSection
import com.google.android.material.tabs.TabLayout

class ProfileActivity : AppCompatActivity() {

    private lateinit var composeView: ComposeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_profile)

        composeView = findViewById(R.id.composeView)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.addTab(tabLayout.newTab().setText("Mes Plats"))
        tabLayout.addTab(tabLayout.newTab().setText("Avis Reçus"))

        // Par défaut, afficher les plats partagés
        showDishes()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showDishes()
                    1 -> showReviews()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun showDishes() {
        composeView.setContent {
            DishListSection()
        }
    }

    private fun showReviews() {
        composeView.setContent {
            // TODO: Créer un composant Jetpack Compose pour les avis reçus
        }
    }
}

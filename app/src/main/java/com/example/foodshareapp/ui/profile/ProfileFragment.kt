package com.example.foodshareapp.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.example.foodshareapp.R
import com.example.foodshareapp.ui.components.DishListSection
import com.example.foodshareapp.ui.components.ProfileHeader
import com.example.foodshareapp.ui.components.ReviewListSection
import com.google.android.material.tabs.TabLayout

class ProfileFragment : Fragment() {

    private lateinit var composeView: ComposeView
    private var selectedTab: TabType = TabType.DISHES

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        composeView = root.findViewById(R.id.composeView)
        val tabLayout = root.findViewById<TabLayout>(R.id.tabLayout)

        tabLayout.addTab(tabLayout.newTab().setText("Mes Plats"))
        tabLayout.addTab(tabLayout.newTab().setText("Avis Reçus"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectedTab = when (tab?.position) {
                    0 -> TabType.DISHES
                    1 -> TabType.REVIEWS
                    else -> TabType.DISHES
                }
                updateContent()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        updateContent()
        return root
    }

    private fun updateContent() {
        composeView.setContent {
            ProfileScreen(selectedTab)
        }
    }
}

@Composable
fun ProfileScreen(selectedTab: TabType) {
    // Utiliser Box comme conteneur principal pour gérer les contraintes correctement
    Box(modifier = Modifier.fillMaxSize()) {
        // Column garantit que le contenu est bien structuré verticalement
        Column(modifier = Modifier.fillMaxSize()) {
            // Définir une hauteur fixe pour ProfileHeader si nécessaire
            ProfileHeader()

            // Contenu basé sur l'onglet sélectionné
            when (selectedTab) {
                TabType.DISHES -> DishListSection()
                TabType.REVIEWS -> ReviewListSection()
            }
        }
    }
}

enum class TabType {
    DISHES,
    REVIEWS
}
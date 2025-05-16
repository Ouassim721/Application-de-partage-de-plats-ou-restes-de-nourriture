package com.example.foodshareapp.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.example.foodshareapp.R
import com.example.foodshareapp.ui.components.DishListSection
import com.example.foodshareapp.ui.components.ProfileHeader
import com.example.foodshareapp.ui.components.ReviewListSection
import com.google.android.material.tabs.TabLayout
import com.example.foodshareapp.activities.LoginActivity

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

    private fun logout(context: Context) {
        val sharedPref = context.getSharedPreferences("UserSession", AppCompatActivity.MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

    @Composable
    fun ProfileScreen(selectedTab: TabType) {
        val context = LocalContext.current
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                ProfileHeader()

                when (selectedTab) {
                    TabType.DISHES -> DishListSection()
                    TabType.REVIEWS -> ReviewListSection()
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { logout(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Se déconnecter")
                }
            }
        }
    }

    enum class TabType {
        DISHES,
        REVIEWS
    }
}

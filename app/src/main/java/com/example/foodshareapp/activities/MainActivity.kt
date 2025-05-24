package com.example.foodshareapp.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.ui.setupWithNavController
import com.example.foodshareapp.R
import com.example.foodshareapp.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import android.os.Build
import android.Manifest
import android.content.Context
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.foodshareapp.notifications.NotificationHelper


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val protectedFragments = setOf(
                R.id.conversationsFragment,
                R.id.chatFragment,
                R.id.profileFragment,
                R.id.addFragment
            )

            if (item.itemId in protectedFragments && !isUserLoggedIn()) {
                startActivity(Intent(this, LoginActivity::class.java))
                return@setOnItemSelectedListener false
            }

            // Safe navigation
            val success = try {
                navController.navigate(item.itemId)
                true
            } catch (e: IllegalArgumentException) {
                false
            }

            success
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    observeNewDishes(this)
                }
            }.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            observeNewDishes(this)
        }


    }

    private fun isUserLoggedIn(): Boolean {
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        return sharedPref.getBoolean("isLoggedIn", false)
    }
    fun observeNewDishes(context: Context) {
        NotificationHelper.createNotificationChannel(context)

        val db = FirebaseFirestore.getInstance()
        db.collection("plats")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null || snapshots.isEmpty) return@addSnapshotListener

                val plat = snapshots.documents.first()
                val titre = plat.getString("titre") ?: "Nouveau plat"
                val description = plat.getString("description") ?: "Un nouveau plat est disponible"

                NotificationHelper.showNewDishNotification(context, titre, description)
            }
    }

}


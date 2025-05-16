package com.example.foodshareapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.setupWithNavController
import com.example.foodshareapp.R
import com.example.foodshareapp.databinding.ActivityMainBinding

import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment


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
                R.id.messagesFragment,
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



    }

    private fun isUserLoggedIn(): Boolean {
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        return sharedPref.getBoolean("isLoggedIn", false)
    }


}


package com.example.foodshareapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.foodshareapp.R
import com.example.foodshareapp.databinding.ActivityMainBinding
import com.example.foodshareapp.ui.fragments.HomeFragment
import com.example.foodshareapp.ui.fragments.MapFragment
import com.example.foodshareapp.ui.fragments.MessagesFragment
import com.example.foodshareapp.ui.fragments.ProfileFragment
import com.example.foodshareapp.ui.fragments.PublishFragment

/*class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                /*R.id.nav_my_dishes -> {
                    startActivity(Intent(this, MyDishesActivity::class.java))
                    true
                }*/
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

}*/
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Par défaut on affiche HomeFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, HomeFragment())
            .commit()

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val fragment: Fragment? = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_carte -> MapFragment()
                R.id.nav_ajouter -> PublishFragment()
                R.id.nav_message -> MessagesFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> null
            }

            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, it)
                    .commit()
            }

            true
        }
    }
}


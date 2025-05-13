package com.example.foodshareapp.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.foodshareapp.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            // Redirection après 2 secondes
            val intent = Intent(this, MainActivity::class.java) // ou MainActivity
            startActivity(intent)
            finish()
        }, 2000)
    }
}

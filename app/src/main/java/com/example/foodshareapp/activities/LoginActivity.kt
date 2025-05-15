package com.example.foodshareapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodshareapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.foodshareapp.ui.fragments.HomeFragment

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val emailInput = findViewById<EditText>(R.id.loginEmail)
        val passwordInput = findViewById<EditText>(R.id.loginPassword)
        val loginBtn = findViewById<Button>(R.id.loginButton)
        val registerRedirect: TextView = findViewById(R.id.registerRedirect)
        registerRedirect.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        loginBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        val userId = auth.currentUser?.uid
                        userId?.let {
                            firestore.collection("users").document(it).get()
                                .addOnSuccessListener { doc ->
                                    val role = doc.getString("role")
                                    val intent = Intent(this, HomeFragment::class.java)
                                    intent.putExtra("role", role)
                                    startActivity(intent)
                                    finish()
                                }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Connexion échouée : ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}

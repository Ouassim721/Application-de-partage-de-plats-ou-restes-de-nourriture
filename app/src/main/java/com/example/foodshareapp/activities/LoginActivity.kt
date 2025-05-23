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
import androidx.core.content.edit
import android.util.Log
import com.example.foodshareapp.data.model.User

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

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val user = auth.currentUser
                    if (user != null) {
                        firestore.collection("users").document(user.uid).get()
                            .addOnSuccessListener { document ->
                                val userData = document.toObject(User::class.java)
                                Log.d("FIREBASE", "User chargé : $userData")

                                val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
                                sharedPref.edit {
                                    putBoolean("isLoggedIn", true)
                                    putString("userRole", userData?.role ?: "user") // Ajout rôle
                                    apply()
                                }

                                // Redirection selon rôle
                                if (userData?.role == "admin") {
                                    startActivity(Intent(this, AdminDashboardActivity::class.java))
                                } else {
                                    startActivity(Intent(this, MainActivity::class.java))
                                }
                                finish()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Connexion échouée : ${it.message}", Toast.LENGTH_SHORT)
                        .show()
                }

        }
    }
}


package com.example.foodshareapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodshareapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val roleInput = findViewById<EditText>(R.id.roleInput) // admin ou user
        val registerBtn = findViewById<Button>(R.id.registerButton)

        registerBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val role = roleInput.text.toString().trim()

            if (email.isNotEmpty() && password.length >= 6 && (role == "admin" || role == "user")) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        val userId = auth.currentUser?.uid
                        val user = hashMapOf("email" to email, "role" to role)
                        userId?.let {
                            firestore.collection("users").document(it).set(user)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Inscription réussie", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    finish()
                                }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Erreur : ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Champs invalides", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

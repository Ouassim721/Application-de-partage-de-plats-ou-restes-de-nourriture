package com.example.foodshareapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodshareapp.R
import com.example.foodshareapp.data.model.User
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

        val nameInput = findViewById<EditText>(R.id.nameInput)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val registerBtn = findViewById<Button>(R.id.registerButton)
        val loginRedirect: TextView = findViewById(R.id.loginRedirect)
        loginRedirect.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        registerBtn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.length >= 6 && name.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        val uid = auth.currentUser?.uid ?: return@addOnSuccessListener
                        val newUser = User(
                            uid = uid,
                            name = name,
                            email = email,
                            city = ""
                        )
                        firestore.collection("users").document(uid)
                            .set(newUser)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Inscription réussie", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
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

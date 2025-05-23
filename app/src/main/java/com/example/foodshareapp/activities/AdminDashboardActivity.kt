package com.example.foodshareapp.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodshareapp.data.model.User
import com.example.foodshareapp.data.model.Plat
import com.example.foodshareapp.databinding.ActivityAdminDashboardBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userAdapter: AdminUserAdapter
    private lateinit var platAdapter: AdminPlatAdapter
    private var usersListener: ListenerRegistration? = null
    private var platsListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        setupToolbar()
        setupRecyclerViews()
        loadStats()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Tableau de bord Admin"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupRecyclerViews() {
        userAdapter = AdminUserAdapter(
            onDeleteUser = { uid -> showDeleteUserConfirmation(uid) }
        )
        platAdapter = AdminPlatAdapter(
            onDeletePlat = { plat -> showDeletePlatConfirmation(plat) }
        )

        with(binding.recyclerViewUsers) {
            layoutManager = LinearLayoutManager(this@AdminDashboardActivity)
            adapter = userAdapter
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }

        with(binding.recyclerViewPlats) {
            layoutManager = LinearLayoutManager(this@AdminDashboardActivity)
            adapter = platAdapter
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    private fun setupListeners() {
        usersListener = firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Erreur de chargement des utilisateurs", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val users = snapshot?.documents?.mapNotNull { it.toObject(User::class.java) } ?: emptyList()
                userAdapter.submitList(users)
                binding.textTotalUsers.text = "Utilisateurs: ${users.size}"
            }

        platsListener = firestore.collection("plats")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Erreur de chargement des plats", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val plats = snapshot?.documents?.mapNotNull { it.toObject(Plat::class.java) } ?: emptyList()
                platAdapter.submitList(plats)
                binding.textTotalPlats.text = "Plats publiés: ${plats.size}"
            }
    }

    private fun loadStats() {
        binding.progressBar.visibility = View.VISIBLE

        firestore.collection("users").get().addOnSuccessListener { users ->
            binding.textTotalUsers.text = "Utilisateurs: ${users.size()}"
        }.addOnFailureListener {
            Toast.makeText(this, "Erreur de chargement des stats utilisateurs", Toast.LENGTH_SHORT).show()
        }

        firestore.collection("plats").get().addOnSuccessListener { plats ->
            binding.textTotalPlats.text = "Plats publiés: ${plats.size()}"
        }.addOnFailureListener {
            Toast.makeText(this, "Erreur de chargement des stats plats", Toast.LENGTH_SHORT).show()
        }.addOnCompleteListener {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun showDeleteUserConfirmation(uid: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Voulez-vous vraiment supprimer cet utilisateur?")
            .setPositiveButton("Supprimer") { _, _ -> deleteUser(uid) }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showDeletePlatConfirmation(plat: Plat) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Voulez-vous vraiment supprimer le plat: ${plat.titre}?")
            .setPositiveButton("Supprimer") { _, _ -> deletePlat(plat.id) }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun deleteUser(uid: String) {
        firestore.collection("users").document(uid).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Utilisateur supprimé", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Échec de la suppression", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deletePlat(id: String) {
        firestore.collection("plats").document(id).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Plat supprimé", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Échec de la suppression", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        usersListener?.remove()
        platsListener?.remove()
    }
}
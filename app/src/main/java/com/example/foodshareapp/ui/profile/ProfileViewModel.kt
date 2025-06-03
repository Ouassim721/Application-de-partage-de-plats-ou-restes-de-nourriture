package com.example.foodshareapp.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodshareapp.data.model.Plat
import com.example.foodshareapp.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _sharedDishes = MutableLiveData<List<Plat>>()
    val sharedDishes: LiveData<List<Plat>> = _sharedDishes

    private val _recoveredDishes = MutableLiveData<List<Plat>>()
    val recoveredDishes: LiveData<List<Plat>> = _recoveredDishes

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isAdmin = MutableLiveData<Boolean>()
    val isAdmin: LiveData<Boolean> = _isAdmin
    init {
        fetchCurrentUser()
    }

    private fun fetchCurrentUser() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            viewModelScope.launch {
                try {
                    val userDoc = firestore.collection("users").document(userId).get().await()
                    if (userDoc.exists()) {
                        val user = userDoc.toObject(User::class.java)?.copy(uid = userId)
                        _currentUser.postValue(user)
                    } else {
                        _errorMessage.postValue("Profil utilisateur introuvable")
                        _currentUser.postValue(null)
                    }
                } catch (e: Exception) {
                    _errorMessage.postValue("Erreur lors du chargement du profil: ${e.message}")
                    _currentUser.postValue(null)
                }
            }
        } else {
            _currentUser.postValue(null) // Aucun utilisateur connecté
        }
    }

    fun fetchUserSharedDishes(userId: String) {
        viewModelScope.launch {
            try {
                val dishesCollection = firestore.collection("plats")
                    .whereEqualTo("userId", userId)
                    .orderBy("datePublication", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val dishes = dishesCollection.documents.mapNotNull { document ->
                    document.toObject(Plat::class.java)
                }

                _sharedDishes.postValue(dishes)

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Erreur lors du chargement des plats partagés: ${e.message}")
                _sharedDishes.postValue(emptyList())
            }
        }
    }

    fun fetchUserRecoveredDishes(userId: String) {
        viewModelScope.launch {
            try {
                val dishesCollection = firestore.collection("plats")
                    .whereEqualTo("recoveredByUserId", userId)
                    .whereEqualTo("statut", "recuperer")
                    .orderBy("datePublication", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val dishes = dishesCollection.documents.mapNotNull { document ->
                    document.toObject(Plat::class.java)
                }

                _recoveredDishes.postValue(dishes)

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Erreur lors du chargement des plats récupérés: ${e.message}")
                _recoveredDishes.postValue(emptyList())
            }
        }
    }

    fun checkIfUserIsAdmin(uid: String) {
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                _isAdmin.value = document.getString("role") == "admin"
            }
            .addOnFailureListener {
                _errorMessage.value = "Erreur lors de la récupération du rôle utilisateur"
            }
    }

    fun refreshUserData() {
        fetchCurrentUser()
    }

    fun logout() {
        auth.signOut()
        _currentUser.postValue(null)
        _sharedDishes.postValue(emptyList())
        _recoveredDishes.postValue(emptyList())
    }

    fun clearErrorMessage() {
        _errorMessage.postValue(null)
    }
}

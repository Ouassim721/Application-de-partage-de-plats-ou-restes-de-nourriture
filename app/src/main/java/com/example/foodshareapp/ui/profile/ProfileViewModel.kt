// com/example/foodshareapp/ui/profile/ProfileViewModel.kt
package com.example.foodshareapp.ui.profile

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
            _currentUser.postValue(null) // No user logged in
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
                    document.toObject(Plat::class.java)?.let { plat ->
                        // Ensure we have the document ID if needed
                        plat.copy()
                    }
                }
                _sharedDishes.postValue(dishes)

                // Update user's shared dishes count
                updateUserDishesCount(userId, dishes.size, isShared = true)

            } catch (e: Exception) {
                _errorMessage.postValue("Erreur lors du chargement des plats partagés: ${e.message}")
                _sharedDishes.postValue(emptyList())
            }
        }
    }

    fun fetchUserRecoveredDishes(userId: String) {
        viewModelScope.launch {
            try {
                // Option 1: Si vous avez un champ "recoveredByUserId" dans Plat
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

                // Update user's recovered dishes count
                updateUserDishesCount(userId, dishes.size, isShared = false)

            } catch (e: Exception) {
                // Si la requête échoue (peut-être que le champ n'existe pas encore)
                _errorMessage.postValue("Erreur lors du chargement des plats récupérés: ${e.message}")
                _recoveredDishes.postValue(emptyList())

                // Alternative: Rechercher dans une collection séparée ou utiliser une autre logique
                fetchRecoveredDishesAlternative(userId)
            }
        }
    }

    private fun fetchRecoveredDishesAlternative(userId: String) {
        viewModelScope.launch {
            try {
                // Alternative: Rechercher tous les plats récupérés et filtrer
                // Cette méthode est moins efficace mais fonctionne si le champ n'existe pas
                val allRecoveredDishes = firestore.collection("plats")
                    .whereEqualTo("statut", "recuperer")
                    .get()
                    .await()

                val userRecoveredDishes = allRecoveredDishes.documents.mapNotNull { document ->
                    document.toObject(Plat::class.java)
                }.filter { plat ->
                    // Ici, vous devrez implémenter votre logique pour déterminer
                    // si ce plat a été récupéré par cet utilisateur
                    // Par exemple, vérifier dans une collection séparée ou un champ spécifique
                    false // Placeholder - à remplacer par votre logique
                }

                _recoveredDishes.postValue(userRecoveredDishes)
            } catch (e: Exception) {

                _errorMessage.postValue("Impossible de charger les plats récupérés")
                _recoveredDishes.postValue(emptyList())
            }
        }
    }

    private fun updateUserDishesCount(userId: String, count: Int, isShared: Boolean) {
        viewModelScope.launch {
            try {
                val field = if (isShared) "dishesOfferedCount" else "dishesReceivedCount"
                firestore.collection("users").document(userId)
                    .update(field, count)
                    .await()
            } catch (e: Exception) {
                // Ignore silently - this is just for statistics
            }
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
package com.example.foodshareapp.ui.conversation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.foodshareapp.data.model.Conversation
import com.example.foodshareapp.data.model.ConversationStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ConversationsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _conversations = MutableLiveData<List<Conversation>>()
    val conversations: LiveData<List<Conversation>> = _conversations

    private var allConversations: List<Conversation> = emptyList()
    private var listener: ListenerRegistration? = null

    init {
        fetchConversationsFromFirebase()
    }

    private fun fetchConversationsFromFirebase() {
        val currentUserId = auth.currentUser?.uid ?: return

        listener = db.collection("conversations")
            .whereArrayContains("participants", currentUserId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    // log error
                    return@addSnapshotListener
                }

                val list = mutableListOf<Conversation>()
                for (doc in snapshots!!) {
                    val conversation = doc.toObject(Conversation::class.java).copy(id = doc.id)
                    list.add(conversation)
                }
                allConversations = list
                _conversations.value = list
            }
    }

    fun filterByStatus(status: ConversationStatus?) {
        _conversations.value = if (status == null) {
            allConversations
        } else {
            allConversations.filter { it.status == status }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}

package com.example.foodshareapp.ui.conversation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.foodshareapp.data.model.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

class ChatViewModel : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>(emptyList())
    val messages: LiveData<List<Message>> = _messages

    private var conversationId: String = ""
    private var messagesListener: ListenerRegistration? = null

    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun setConversationId(id: String) {
        conversationId = id
        listenToMessages(id)
    }

    private fun listenToMessages(conversationId: String) {
        // Supprimer un éventuel listener précédent
        messagesListener?.remove()

        messagesListener = firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Gérer l'erreur (log ou message utilisateur)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val messagesList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Message::class.java)?.copy(id = doc.id)
                    }
                    _messages.postValue(messagesList)
                }
            }
    }

    fun sendMessage(content: String) {
        if (conversationId.isEmpty() || currentUserId.isEmpty()) return

        val newMessage = Message(
            id = "", // L'ID sera généré par Firestore
            content = content,
            timestamp = System.currentTimeMillis(),
            senderId = currentUserId
        )

        firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .add(newMessage)
            .addOnFailureListener {
                // Gérer l'échec (log, snackbar, etc)
            }
    }

    override fun onCleared() {
        super.onCleared()
        messagesListener?.remove()
    }
}

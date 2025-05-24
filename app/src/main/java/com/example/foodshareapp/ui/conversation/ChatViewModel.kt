package com.example.foodshareapp.ui.conversation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.foodshareapp.data.model.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.auth.FirebaseAuth

class ChatViewModel : ViewModel() {
    private val TAG = "ChatViewModel"

    private val _messages = MutableLiveData<List<Message>>(emptyList())
    val messages: LiveData<List<Message>> = _messages

    private var conversationId: String = ""
    private var messagesListener: ListenerRegistration? = null

    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun setConversationId(id: String) {
        Log.d(TAG, "Setting conversation ID: $id")
        conversationId = id
        listenToMessages(id)
    }

    private fun listenToMessages(conversationId: String) {
        Log.d(TAG, "Setting up messages listener for conversation: $conversationId")

        messagesListener?.remove()

        messagesListener = firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to messages: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d(TAG, "Received ${snapshot.documents.size} messages from Firestore")
                    val messagesList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Message::class.java)?.copy(id = doc.id).also {
                            Log.d(TAG, "Message content: ${it?.content}, sender: ${it?.senderId}")
                        }
                    }
                    _messages.postValue(messagesList)
                } else {
                    Log.d(TAG, "Snapshot is null")
                }
            }
    }

    fun sendMessage(content: String) {
        if (conversationId.isEmpty() || currentUserId.isEmpty()) {
            Log.e(TAG, "Cannot send message - conversationId or currentUserId is empty")
            return
        }

        Log.d(TAG, "Preparing to send message: $content")

        val newMessage = Message(
            id = "",
            content = content,
            timestamp = System.currentTimeMillis(),
            senderId = currentUserId
        )

        firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .add(newMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Message sent successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to send message: ${e.message}")
            }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared, removing listener")
        messagesListener?.remove()
    }
}
package com.example.foodshareapp.ui.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodshareapp.data.model.Message
import com.example.foodshareapp.databinding.FragmentChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private var receiverId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)

        messageAdapter = MessageAdapter(messages, auth.uid!!) //voila l'appel
        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = messageAdapter
        }

        receiverId = arguments?.getString("receiverId")

        listenForMessages()

        binding.buttonSend.setOnClickListener {
            val content = binding.editTextMessage.text.toString().trim()
            if (content.isNotEmpty()) {
                sendMessage(content)
            }
        }

        return binding.root
    }

    private fun sendMessage(content: String) {
        val msg = Message(
            senderId = auth.uid ?: "",
            receiverId = receiverId ?: "",
            content = content,
            timestamp = System.currentTimeMillis()
        )

        db.collection("messages")
            .add(msg)
            .addOnSuccessListener {
                binding.editTextMessage.setText("")
            }
    }

    private fun listenForMessages() {
        db.collection("messages")
            .whereIn("senderId", listOf(auth.uid!!, receiverId!!))
            .whereIn("receiverId", listOf(auth.uid!!, receiverId!!))
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    messages.clear()
                    for (doc in it.documents) {
                        val message = doc.toObject(Message::class.java)
                        if (message != null) messages.add(message)
                    }
                    messageAdapter.notifyDataSetChanged()
                    binding.recyclerViewMessages.scrollToPosition(messages.size - 1)
                }
            }
    }
}

package com.example.foodshareapp.ui.conversation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodshareapp.databinding.FragmentChatBinding
import com.example.foodshareapp.data.model.Message
import com.google.firebase.auth.FirebaseAuth

class ChatFragment : Fragment() {
    private val TAG = "ChatFragment"

    private lateinit var binding: FragmentChatBinding
    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: ChatAdapter
    private lateinit var currentUserId: String
    private lateinit var conversationId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        Log.d(TAG, "Current user ID: $currentUserId")

        conversationId = arguments?.getString("conversationId") ?: ""
        Log.d(TAG, "Conversation ID: $conversationId")

        if (conversationId.isEmpty()) {
            Log.e(TAG, "Error: conversationId is empty")
            throw IllegalArgumentException("conversationId is required to load the chat")
        }

        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        viewModel.setConversationId(conversationId)
        Log.d(TAG, "ViewModel initialized with conversationId")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Log.d(TAG, "onCreateView called")
        binding = FragmentChatBinding.inflate(inflater, container, false)

        adapter = ChatAdapter(emptyList(), currentUserId)
        binding.messagesRecyclerView.adapter = adapter
        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            Log.d(TAG, "New messages received, count: ${messages.size}")
            adapter = ChatAdapter(messages, currentUserId)
            binding.messagesRecyclerView.adapter = adapter
            if (messages.isNotEmpty()) {
                binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
                Log.d(TAG, "Scrolled to position ${messages.size - 1}")
            }
        }

        binding.sendButton.setOnClickListener {
            val content = binding.messageEditText.text.toString()
            if (content.isNotBlank()) {
                Log.d(TAG, "Sending message: $content")
                viewModel.sendMessage(content)
                binding.messageEditText.text.clear()
            } else {
                Log.d(TAG, "Attempted to send empty message")
            }
        }

        return binding.root
    }
}
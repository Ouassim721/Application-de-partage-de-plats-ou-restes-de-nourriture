package com.example.foodshareapp.ui.conversation

import android.os.Bundle
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

    private lateinit var binding: FragmentChatBinding
    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: ChatAdapter
    private lateinit var currentUserId: String
    private lateinit var conversationId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Récupération dynamique de l'ID utilisateur
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // ✅ Récupération de l'ID de la conversation passé en argument
        conversationId = arguments?.getString("conversationId") ?: ""

        // 🔒 Sécurité basique
        if (conversationId.isEmpty()) {
            throw IllegalArgumentException("conversationId is required to load the chat")
        }

        // ✅ Initialisation du ViewModel avec conversationId
        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        viewModel.setConversationId(conversationId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)

        // Initialiser RecyclerView
        adapter = ChatAdapter(emptyList(), currentUserId)
        binding.messagesRecyclerView.adapter = adapter
        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Observer les messages
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter = ChatAdapter(messages, currentUserId)
            binding.messagesRecyclerView.adapter = adapter
            binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
        }

        // Gérer l’envoi de message
        binding.sendButton.setOnClickListener {
            val content = binding.messageEditText.text.toString()
            if (content.isNotBlank()) {
                viewModel.sendMessage(content)
                binding.messageEditText.text.clear()
            }
        }

        return binding.root
    }
}

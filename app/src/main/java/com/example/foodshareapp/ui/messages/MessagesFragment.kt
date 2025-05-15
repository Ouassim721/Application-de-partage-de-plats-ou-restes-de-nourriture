package com.example.foodshareapp.ui.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodshareapp.R
import com.example.foodshareapp.data.model.User
import com.example.foodshareapp.databinding.FragmentMessagesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!

    private val userList = mutableListOf<User>()
    private lateinit var adapter: UserMessageAdapter

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialisation Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Configuration du RecyclerView
        setupRecyclerView()

        // Chargement des utilisateurs
        fetchUsers()
    }

    private fun setupRecyclerView() {
        adapter = UserMessageAdapter(userList) { selectedUser ->
            navigateToChatFragment(selectedUser.uid)
        }

        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MessagesFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun navigateToChatFragment(receiverId: String) {
        val chatFragment = ChatFragment().apply {
            arguments = Bundle().apply {
                putString("receiverId", receiverId)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, chatFragment)
            .addToBackStack(null)
            .commit()
    }


    private fun fetchUsers() {
        val currentUid = auth.currentUser?.uid ?: run {
            Toast.makeText(requireContext(), "Utilisateur non connecté", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                userList.clear()
                result.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.takeIf { it.uid != currentUid }
                }.let {
                    userList.addAll(it)
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Erreur de chargement: ${exception.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
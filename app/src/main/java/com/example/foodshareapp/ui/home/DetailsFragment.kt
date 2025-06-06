package com.example.foodshareapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.foodshareapp.R
import com.example.foodshareapp.data.model.Plat
import com.example.foodshareapp.databinding.FragmentDetailsBinding
import com.google.android.material.chip.Chip
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
import androidx.core.os.bundleOf

class DetailsFragment : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private var plat: Plat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        plat = arguments?.getParcelable(ARG_PLAT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        plat?.let { setupViews(it) }
        binding.btnContacter.setOnClickListener {
            plat?.let { platData ->
                val receiverId = platData.userId
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                val updatedPlat = platData.copy(reserve = true)

                if (currentUserId != null && receiverId.isNotEmpty() && currentUserId != receiverId) {
                    getOrCreateConversationWithUser(currentUserId, receiverId)
                    updatePlatInFirestore(updatedPlat)

                } else {
                    Toast.makeText(requireContext(), "Action impossible", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun updatePlatInFirestore(plat: Plat) {
        val db = FirebaseFirestore.getInstance()

        db.collection("plats")
            .document(plat.id)
            .update("reserve", true)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Plat réservé avec succès", Toast.LENGTH_SHORT).show()
                plat.reserve = true
                binding.reserveBadge.visibility = View.VISIBLE
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Erreur lors de la réservation: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun setupViews(plat: Plat) {
        with(binding) {
            titrePlat.text = plat.titre
            descriptionPlat.text = plat.description
            ingredientsPlat.text = plat.ingredients
            localisationPlat.text = plat.localisation
            portionsPlat.text = getString(R.string.portions_format, plat.portions)
            expirationPlat.text = plat.expiration
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            val isOwner = currentUserId == plat.userId

            btnContacter.visibility = if (isOwner || plat.reserve) View.GONE else View.VISIBLE

            if (plat.imageUrl.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(plat.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(imagePlat)
            }

            reserveBadge.visibility = if (plat.reserve) View.VISIBLE else View.GONE

            if (plat.statut.isNotEmpty()) {
                addChip(chipGroupStatut, plat.statut, R.color.colorPrimaryDark)
            }

            plat.typePlat.forEach { type ->
                val colorRes = when (type) {
                    "Végétarien" -> R.color.colorTertiary
                    "Sucré" -> R.color.colorBrown
                    "Salé" -> R.color.colorPrimary
                    else -> R.color.gray_medium
                }
                addChip(chipGroupTypePlat, type, colorRes)
            }
        }
    }

    private fun addChip(group: com.google.android.material.chip.ChipGroup, text: String, colorRes: Int) {
        val chip = Chip(requireContext()).apply {
            this.text = text
            setChipBackgroundColorResource(colorRes)
            setTextColor(resources.getColor(android.R.color.white, null))
        }
        group.addView(chip)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PLAT = "plat"

        fun newInstance(plat: Plat) = DetailsFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_PLAT, plat)
            }
        }
    }
    private fun getOrCreateConversationWithUser(currentUserId: String, otherUserId: String) {
        val db = FirebaseFirestore.getInstance()
        val conversationsRef = db.collection("conversations")
        val usersRef = db.collection("users")

        // D'abord récupérer le nom de l'autre utilisateur
        usersRef.document(otherUserId).get()
            .addOnSuccessListener { userDoc ->
                val otherUserName = userDoc.getString("username") ?: "Utilisateur"

                // Ensuite vérifier si une conversation existe
                conversationsRef
                    .whereArrayContains("participants", currentUserId)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val existingConversation = querySnapshot.documents.firstOrNull { doc ->
                            val participants = doc.get("participants") as? List<*>
                            participants?.contains(otherUserId) == true
                        }

                        if (existingConversation != null) {
                            val conversationId = existingConversation.id
                            navigateToChat(conversationId)
                        } else {
                            // Crée une nouvelle conversation avec le username
                            val newConversationId = UUID.randomUUID().toString()
                            val newConversation = hashMapOf(
                                "participants" to listOf(currentUserId, otherUserId),
                                "username" to otherUserName, // Ajout du nom d'utilisateur
                                "lastMessage" to "Conversation créée",
                                "timestamp" to System.currentTimeMillis(),
                                "isUnread" to true,
                                "status" to "NEW"
                            )

                            conversationsRef.document(newConversationId).set(newConversation)
                                .addOnSuccessListener {
                                    navigateToChat(newConversationId)
                                }
                                .addOnFailureListener {
                                    Toast.makeText(requireContext(), "Erreur lors de la création", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Erreur de recherche", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Impossible de récupérer les infos utilisateur", Toast.LENGTH_SHORT).show()
            }
    }


    private fun navigateToChat(conversationId: String) {
        findNavController().navigate(
            R.id.action_detailsFragment_to_chatFragment,
            bundleOf("conversationId" to conversationId)
        )
    }


}

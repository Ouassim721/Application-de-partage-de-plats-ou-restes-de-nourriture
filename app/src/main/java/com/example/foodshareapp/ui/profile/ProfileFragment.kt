package com.example.foodshareapp.ui.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.foodshareapp.R
import com.example.foodshareapp.activities.AdminDashboardActivity
import com.example.foodshareapp.activities.LoginActivity
import com.example.foodshareapp.data.model.Plat
import com.example.foodshareapp.data.model.User
import com.example.foodshareapp.databinding.FragmentProfileBinding
import com.example.foodshareapp.ui.adapter.DishHistoryAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {


    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("FragmentProfileBinding is null")

    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var sharedDishesAdapter: DishHistoryAdapter
    private lateinit var recoveredDishesAdapter: DishHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId == null) {
            handleUserNotLoggedIn()
            return
        }

        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        setupRecyclerViews()
        observeProfileData()
        setupClickListeners()
        showLoadingState(true)

        profileViewModel.checkIfUserIsAdmin(currentUserId)
        binding.profileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_IMAGE_PICK)
        }

    }

    private fun setupRecyclerViews() {
        sharedDishesAdapter = DishHistoryAdapter { plat ->
            if (isAdded) Toast.makeText(requireContext(), "Plat partagé: ${plat.titre}", Toast.LENGTH_SHORT).show()
        }

        binding.rvSharedDishes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = sharedDishesAdapter
        }

        recoveredDishesAdapter = DishHistoryAdapter { plat ->
            if (isAdded) Toast.makeText(requireContext(), "Plat récupéré: ${plat.titre}", Toast.LENGTH_SHORT).show()
        }

        binding.rvRecoveredDishes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recoveredDishesAdapter
        }
    }

    private fun observeProfileData() {
        profileViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            showLoadingState(false)
            user?.let {
                updateUserProfileUI(it)
                profileViewModel.fetchUserSharedDishes(it.uid)
                profileViewModel.fetchUserRecoveredDishes(it.uid)
            } ?: run {
                Log.d("ProfileFragment", "Utilisateur non connecté")
                handleUserNotLoggedIn()
            }
        }

        profileViewModel.sharedDishes.observe(viewLifecycleOwner) { dishes ->
            sharedDishesAdapter.submitList(dishes)
            updateEmptyState(dishes, binding.tvNoSharedDishes, binding.rvSharedDishes)
        }

        profileViewModel.recoveredDishes.observe(viewLifecycleOwner) { dishes ->
            recoveredDishesAdapter.submitList(dishes)
            updateEmptyState(dishes, binding.tvNoRecoveredDishes, binding.rvRecoveredDishes)
        }

        profileViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                if (isAdded) {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    Log.e("ProfileFragment", "Erreur : $it")
                }
                profileViewModel.clearErrorMessage()
            }
        }

        profileViewModel.isAdmin.observe(viewLifecycleOwner) { isAdmin ->
            if (isAdmin) {
                binding.adminDashboardButton.visibility = View.VISIBLE
                binding.adminDashboardButton.setOnClickListener {
                    startActivity(Intent(requireContext(), AdminDashboardActivity::class.java))
                }
            }
        }
    }

    private fun updateUserProfileUI(user: User) {
        with(binding) {
            userName.text = user.name
            userEmail.text = user.email
            userCity.text = if (user.city.isNotEmpty()) user.city else "Non renseigné"
            userBio.text = if (user.bio.isNotEmpty()) user.bio else "Aucune bio disponible"

            dishesOfferedCount.text = user.dishesOfferedCount.toString()
            dishesRecoveredCount.text = user.dishesReceivedCount.toString()

            if (user.profileImageUrl.isNotEmpty()) {
                Glide.with(this@ProfileFragment)
                    .load(user.profileImageUrl)
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .circleCrop()
                    .into(profileImage)
            } else {
                profileImage.setImageResource(R.drawable.ic_default_avatar)
            }
        }
    }

    private fun setupClickListeners() {
        binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            val sharedPref = requireContext().getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE)
            sharedPref.edit().putBoolean("notifications_enabled", isChecked).apply()

            val message = if (isChecked) "Notifications activées" else "Notifications désactivées"
            if (isAdded) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        binding.logoutButton.setOnClickListener {
            profileViewModel.logout()
            logout(requireContext())
        }
    }

    private fun showLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.profileContent.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun updateEmptyState(dishes: List<Plat>, emptyTextView: View, recyclerView: View) {
        emptyTextView.visibility = if (dishes.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (dishes.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun handleUserNotLoggedIn() {
        if (!isAdded) return
        Toast.makeText(requireContext(), "Vous devez être connecté pour accéder au profil", Toast.LENGTH_LONG).show()
        logout(requireContext())
    }

    private fun logout(context: Context) {
        context.getSharedPreferences(USER_SESSION, Context.MODE_PRIVATE)
            .edit().clear().apply()

        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
        activity?.finish()
    }

    override fun onResume() {
        super.onResume()
        val sharedPref = requireContext().getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE)
        binding.notificationSwitch.isChecked = sharedPref.getBoolean("notifications_enabled", true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun uploadImageToFirebaseStorage(imageUri: Uri) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/$uid.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    updateUserProfileImageUrlInFirestore(uid, imageUrl)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Échec de l'upload de la photo", Toast.LENGTH_SHORT).show()
            }
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE_PICK && resultCode == AppCompatActivity.RESULT_OK) {
            val imageUri = data?.data
            imageUri?.let {
                uploadImageToFirebaseStorage(it)
            }
        }
    }
    private fun updateUserProfileImageUrlInFirestore(uid: String, imageUrl: String) {
        FirebaseFirestore.getInstance().collection("users")
            .document(uid)
            .update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Photo mise à jour", Toast.LENGTH_SHORT).show()
                profileViewModel.refreshUserData()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val USER_PREFS = "UserPrefs"
        private const val USER_SESSION = "UserSession"
        const val REQUEST_CODE_IMAGE_PICK = 1001
    }


}

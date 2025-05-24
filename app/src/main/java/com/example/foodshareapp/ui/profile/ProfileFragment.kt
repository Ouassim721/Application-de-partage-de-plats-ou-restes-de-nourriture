// com/example/foodshareapp/ui/profile/ProfileFragment.kt
package com.example.foodshareapp.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
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

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

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
        val adminDashboardButton = view.findViewById<Button>(R.id.adminDashboardButton)
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        setupRecyclerViews()
        observeProfileData()
        setupClickListeners()

        // Show loading state initially
        showLoadingState(true)
        if (currentUserId != null) {
            FirebaseFirestore.getInstance().collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    val role = document.getString("role")
                    if (role == "admin") {
                        adminDashboardButton.visibility = View.VISIBLE
                        adminDashboardButton.setOnClickListener {
                            val intent = Intent(requireContext(), AdminDashboardActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }
        }
    }

    private fun setupRecyclerViews() {
        // Setup for Shared Dishes
        sharedDishesAdapter = DishHistoryAdapter { plat ->
            // Handle click on shared dish item
            Toast.makeText(context, "Plat partagé: ${plat.titre}", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to dish detail if needed
        }
        binding.rvSharedDishes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = sharedDishesAdapter
        }

        // Setup for Recovered Dishes
        recoveredDishesAdapter = DishHistoryAdapter { plat ->
            // Handle click on recovered dish item
            Toast.makeText(context, "Plat récupéré: ${plat.titre}", Toast.LENGTH_SHORT).show()
        }
        binding.rvRecoveredDishes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recoveredDishesAdapter
        }
    }

    private fun observeProfileData() {
        // Observe current user data
        profileViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            showLoadingState(false)
            user?.let {
                updateUserProfileUI(it)
                // Fetch dishes after user data is available
                profileViewModel.fetchUserSharedDishes(it.uid)
                profileViewModel.fetchUserRecoveredDishes(it.uid)
            } ?: run {
                // User is null, perhaps not logged in
                Log.d("ProfileFragment", "Utilisateur non connecté")
                handleUserNotLoggedIn()
            }
        }

        // Observe shared dishes
        profileViewModel.sharedDishes.observe(viewLifecycleOwner) { dishes ->
            sharedDishesAdapter.submitList(dishes)
            updateEmptyState(dishes, binding.tvNoSharedDishes, binding.rvSharedDishes)
        }

        // Observe recovered dishes
        profileViewModel.recoveredDishes.observe(viewLifecycleOwner) { dishes ->
            recoveredDishesAdapter.submitList(dishes)
            updateEmptyState(dishes, binding.tvNoRecoveredDishes, binding.rvRecoveredDishes)
        }

        // Observe error messages
        profileViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                profileViewModel.clearErrorMessage()
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

            // Load profile image using Glide
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
            val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            sharedPref.edit().putBoolean("notifications_enabled", isChecked).apply()

            val message = if (isChecked) "Notifications activées" else "Notifications désactivées"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
        if (dishes.isEmpty()) {
            emptyTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun handleUserNotLoggedIn() {
        Toast.makeText(context, "Vous devez être connecté pour accéder au profil", Toast.LENGTH_LONG).show()
        logout(requireContext())
    }

    private fun logout(context: Context) {
        // Clear shared preferences
        val sharedPref = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        // Navigate to login activity
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)

        // Finish current activity if it's an activity
        activity?.finish()
    }

    override fun onResume() {
        super.onResume()
        // Load notification preference
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val notificationsEnabled = sharedPref.getBoolean("notifications_enabled", true)
        binding.notificationSwitch.isChecked = notificationsEnabled
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
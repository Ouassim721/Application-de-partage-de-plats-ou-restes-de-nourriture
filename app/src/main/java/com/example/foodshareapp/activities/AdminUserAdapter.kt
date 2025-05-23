package com.example.foodshareapp.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodshareapp.data.model.User
import com.example.foodshareapp.databinding.ItemAdminUserBinding

class AdminUserAdapter(
    private val onDeleteUser: (String) -> Unit
) : RecyclerView.Adapter<AdminUserAdapter.UserViewHolder>() {

    private var users: List<User> = emptyList()

    fun submitList(newList: List<User>) {
        users = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemAdminUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    inner class UserViewHolder(private val binding: ItemAdminUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            with(binding) {
                userName.text = user.name ?: "Non renseigné"
                userEmail.text = user.email ?: "Non renseigné"
                userCity.text = user.city ?: "Non renseigné"

                btnDeleteUser.setOnClickListener {
                    user.uid?.let { uid -> onDeleteUser(uid) }
                }
            }
        }
    }
}
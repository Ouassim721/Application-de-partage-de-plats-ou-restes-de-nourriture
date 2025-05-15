package com.example.foodshareapp.ui.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodshareapp.data.model.User
import com.example.foodshareapp.databinding.ItemUserMessageBinding

class UserMessageAdapter(
    private val users: List<User>,
    private val onClick: (User) -> Unit
) : RecyclerView.Adapter<UserMessageAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ItemUserMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.tvName.text = user.name
            Glide.with(binding.root.context)
                .load(user.profileImageUrl)
                .into(binding.ivProfile)

            binding.root.setOnClickListener { onClick(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserMessageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserViewHolder(binding)
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }
}

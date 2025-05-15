package com.example.foodshareapp.ui.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodshareapp.data.model.Message
import com.example.foodshareapp.databinding.ItemMessageBinding

class MessageAdapter(
    private val messages: List<Message>,
    private val currentUserId: String  // Nouveau paramètre
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(private val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: Message) {
            if (msg.senderId == currentUserId) {  // Utilisation du paramètre au lieu de FirebaseAuth
                binding.tvMessageSent.text =
                    msg.content  // Note: J'ai changé msg.message en msg.content pour correspondre à votre modèle
                binding.tvMessageSent.visibility = android.view.View.VISIBLE
                binding.tvMessageReceived.visibility = android.view.View.GONE
            } else {
                binding.tvMessageReceived.text = msg.content  // Même changement ici
                binding.tvMessageReceived.visibility = android.view.View.VISIBLE
                binding.tvMessageSent.visibility = android.view.View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun getItemCount() = messages.size

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }
}
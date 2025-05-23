package com.example.foodshareapp.ui.conversation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodshareapp.R
import com.example.foodshareapp.data.model.Conversation
import com.example.foodshareapp.data.model.ConversationStatus
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date
import android.graphics.Typeface


class ConversationsAdapter(
    private val conversations: List<Conversation>,
    private val onClick: (Conversation) -> Unit
) : RecyclerView.Adapter<ConversationsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar = itemView.findViewById<ImageView>(R.id.avatarImageView)
        val username = itemView.findViewById<TextView>(R.id.usernameTextView)
        val lastMessage = itemView.findViewById<TextView>(R.id.lastMessageTextView)
        val time = itemView.findViewById<TextView>(R.id.timeTextView)
        val statusBadge = itemView.findViewById<TextView>(R.id.statusBadge)

        fun bind(convo: Conversation) {
            username.text = convo.username
            lastMessage.text = convo.lastMessage
            time.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(convo.timestamp))

            // Badge status
            statusBadge.text = when (convo.status) {
                ConversationStatus.NEW -> "Nouveau"
                ConversationStatus.CONFIRMED -> "Confirmé"
                ConversationStatus.COMPLETED -> "Terminé"
            }

            if (convo.isUnread) {
                username.setTypeface(null, Typeface.BOLD)
            }

            itemView.setOnClickListener { onClick(convo) }

            // TODO: Charger l'image avec Glide ou Coil
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_conversation, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = conversations.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(conversations[position])
    }
}

package com.example.foodshareapp.ui.conversation

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodshareapp.R
import com.example.foodshareapp.data.model.Message

class ChatAdapter(
    private val messages: List<Message>,
    private val currentUserId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TAG = "ChatAdapter"

    private val TYPE_SENT = 0
    private val TYPE_RECEIVED = 1

    override fun getItemViewType(position: Int): Int {
        val type = if (messages[position].senderId == currentUserId) TYPE_SENT else TYPE_RECEIVED
        Log.d(TAG, "Message at position $position is of type: ${if (type == TYPE_SENT) "SENT" else "RECEIVED"}")
        return type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d(TAG, "Creating view holder for type: ${if (viewType == TYPE_SENT) "SENT" else "RECEIVED"}")
        return if (viewType == TYPE_SENT) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        Log.d(TAG, "Binding message at position $position: ${msg.content}")

        if (holder is SentMessageViewHolder) {
            holder.messageText.text = msg.content
        } else if (holder is ReceivedMessageViewHolder) {
            holder.messageText.text = msg.content
        }
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "Total messages count: ${messages.size}")
        return messages.size
    }

    inner class SentMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.messageText)
    }

    inner class ReceivedMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.messageText)
    }
}
// com/example/foodshareapp/ui/adapter/DishHistoryAdapter.kt
package com.example.foodshareapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodshareapp.R
import com.example.foodshareapp.data.model.Plat
import java.text.SimpleDateFormat
import java.util.*

class DishHistoryAdapter(
    private val onItemClick: (Plat) -> Unit
) : ListAdapter<Plat, DishHistoryAdapter.DishHistoryViewHolder>(DishDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dish_history, parent, false)
        return DishHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DishHistoryViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class DishHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dishImage: ImageView = itemView.findViewById(R.id.iv_dish_image)
        private val dishTitle: TextView = itemView.findViewById(R.id.tv_dish_title)
        private val dishDate: TextView = itemView.findViewById(R.id.tv_dish_date)
        private val dishStatus: TextView = itemView.findViewById(R.id.tv_dish_status)
        private val dishPortions: TextView = itemView.findViewById(R.id.tv_dish_portions)

        fun bind(plat: Plat, onItemClick: (Plat) -> Unit) {
            dishTitle.text = plat.titre
            dishPortions.text = "${plat.portions} portion${if (plat.portions > 1) "s" else ""}"

            // Format date
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dishDate.text = dateFormat.format(plat.datePublication.toDate())

            // Set status with color
            when (plat.statut) {
                "disponible" -> {
                    dishStatus.text = "Disponible"
                    dishStatus.setTextColor(itemView.context.getColor(R.color.colorReservation))
                }
                "reserve" -> {
                    dishStatus.text = "Réservé"
                    dishStatus.setTextColor(itemView.context.getColor(R.color.colorPrimary))
                }
                "recuperer" -> {
                    dishStatus.text = "Récupéré"
                    dishStatus.setTextColor(itemView.context.getColor(R.color.colorBrown))
                }
                else -> {
                    dishStatus.text = "Inconnu"
                    dishStatus.setTextColor(itemView.context.getColor(R.color.gray))
                }
            }

            // Load image
            if (plat.imageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(plat.imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .centerCrop()
                    .into(dishImage)
            } else {
                dishImage.setImageResource(R.drawable.ic_placeholder)
            }

            // Set click listener
            itemView.setOnClickListener {
                onItemClick(plat)
            }
        }
    }

    private class DishDiffCallback : DiffUtil.ItemCallback<Plat>() {
        override fun areItemsTheSame(oldItem: Plat, newItem: Plat): Boolean {
            return oldItem.userId == newItem.userId &&
                    oldItem.titre == newItem.titre &&
                    oldItem.datePublication == newItem.datePublication
        }

        override fun areContentsTheSame(oldItem: Plat, newItem: Plat): Boolean {
            return oldItem == newItem
        }
    }
}
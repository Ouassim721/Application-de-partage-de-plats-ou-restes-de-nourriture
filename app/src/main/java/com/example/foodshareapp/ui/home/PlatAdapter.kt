package com.example.foodshareapp.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodshareapp.R
import com.example.foodshareapp.data.model.Plat

class PlatAdapter(private var plats: List<Plat>) : RecyclerView.Adapter<PlatAdapter.PlatViewHolder>() {

    class PlatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titre: TextView = itemView.findViewById(R.id.titrePlat)
        val description: TextView = itemView.findViewById(R.id.descriptionPlat)
        val expiration: TextView = itemView.findViewById(R.id.expirationPlat)
        val reserve: TextView = itemView.findViewById(R.id.reservePlat)
        val image: ImageView = itemView.findViewById(R.id.imagePlat)
        val btnContacter: Button = itemView.findViewById(R.id.btnContacter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_plat, parent, false)
        return PlatViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlatViewHolder, position: Int) {
        val plat = plats[position]
        holder.titre.text = plat.titre
        holder.description.text = plat.description
        holder.expiration.text = "Expire le : ${plat.expiration}"

        holder.reserve.apply {
            if (plat.reserve) {
                text = "Réservé"
                background = ContextCompat.getDrawable(context, R.drawable.reserved_button_bg)
            } else {
                text = "Disponible"
                background = ContextCompat.getDrawable(context, R.drawable.available_button_bg)
            }
        }

        Glide.with(holder.image.context)
            .load(plat.imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .into(holder.image)

        // 👉 Clic sur le bouton Contacter
        holder.btnContacter.setOnClickListener {
            val context = it.context
            val bundle = android.os.Bundle().apply {
                putString("receiverId", plat.userId)
            }
            it.findNavController().navigate(R.id.action_homeFragment_to_chatFragment, bundle)
        }
    }

    override fun getItemCount(): Int = plats.size

    fun updateData(newPlats: List<Plat>) {
        plats = newPlats
        notifyDataSetChanged()
    }
}

package com.example.foodshareapp.ui.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodshareapp.R
import com.example.foodshareapp.data.model.Plat

class PlatAdapter(private var plats: List<Plat>) : RecyclerView.Adapter<PlatAdapter.PlatViewHolder>() {

    class PlatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titre: TextView = itemView.findViewById(R.id.titrePlat)
        val description: TextView = itemView.findViewById(R.id.descriptionPlat)
        val expiration: TextView = itemView.findViewById(R.id.expirationPlat)
        val image: ImageView = itemView.findViewById(R.id.imagePlat)
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

        Glide.with(holder.image.context)
            .load(plat.imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .into(holder.image)
    }

    override fun getItemCount(): Int = plats.size

    fun updateData(newPlats: List<Plat>) {
        plats = newPlats
        notifyDataSetChanged()
    }
}

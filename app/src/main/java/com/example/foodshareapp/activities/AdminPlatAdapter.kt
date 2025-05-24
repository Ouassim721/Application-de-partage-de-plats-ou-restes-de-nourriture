package com.example.foodshareapp.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodshareapp.data.model.Plat
import com.example.foodshareapp.databinding.ItemAdminPlatBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AdminPlatAdapter(
    private val onDeletePlat: (Plat) -> Unit
) : RecyclerView.Adapter<AdminPlatAdapter.PlatViewHolder>() {

    private var plats: List<Plat> = emptyList()

    fun submitList(newList: List<Plat>) {
        plats = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlatViewHolder {
        val binding = ItemAdminPlatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlatViewHolder(binding)
    }

    override fun getItemCount() = plats.size

    override fun onBindViewHolder(holder: PlatViewHolder, position: Int) {
        holder.bind(plats[position])
    }

    inner class PlatViewHolder(private val binding: ItemAdminPlatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(plat: Plat) {
            with(binding) {
                platTitle.text = plat.titre.ifEmpty { "Titre non disponible" }
                platCity.text = plat.localisation.ifEmpty { "Localisation non disponible" }

                // Conversion du Timestamp vers un format lisible
                val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val dateStr = formatter.format(plat.datePublication.toDate())
                platDate.text = dateStr

                btnDeletePlat.setOnClickListener {
                    onDeletePlat(plat)
                }
            }
        }
    }
}

package com.example.foodshareapp.ui.home

import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Pour charger les images de Firebase Storage
import com.example.foodshareapp.R
import com.example.foodshareapp.data.model.Plat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class PlatAdapter(
    private var plats: List<Plat>,
    private val onItemClick: (Plat) -> Unit,
    private var userLocation: Location? = null // Ajoutez cette propriété
) : RecyclerView.Adapter<PlatAdapter.PlatViewHolder>() {

    // Méthode pour mettre à jour les données (utilisée par HomeFragment)
    fun updateData(newPlats: List<Plat>, location: Location? = null) {
        this.plats = newPlats
        this.userLocation = location
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_plat, parent, false)
        return PlatViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlatViewHolder, position: Int) {
        val plat = plats[position]
        holder.bind(plat, userLocation)
    }

    override fun getItemCount(): Int = plats.size

    inner class PlatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imagePlat: ImageView = itemView.findViewById(R.id.imagePlat)
        private val titrePlat: TextView = itemView.findViewById(R.id.titrePlat)
        private val descriptionPlat: TextView = itemView.findViewById(R.id.descriptionPlat)
        private val expirationPlat: TextView = itemView.findViewById(R.id.expirationPlat)
        private val distancePlat: TextView = itemView.findViewById(R.id.distancePlat) // Nouveau champ
        private val reservePlat: TextView = itemView.findViewById(R.id.reservePlat)
        private val btnDetails: Button = itemView.findViewById(R.id.btnDetails)
        private val chipGroupStatut: ChipGroup = itemView.findViewById(R.id.chipGroupStatut) // Nouveau pour les badges
        private val chipGroupTypePlat: ChipGroup = itemView.findViewById(R.id.chipGroupTypePlat) // Nouveau pour les badges

        fun bind(plat: Plat, userLocation: Location?) {
            titrePlat.text = plat.titre
            descriptionPlat.text = plat.description
            try {
                val expirationDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(plat.expiration)
                val now = Date()
                if (expirationDate != null && expirationDate.after(now)) {
                    val diff = expirationDate.time - now.time
                    val minutes = diff / (1000 * 60)
                    val hours = minutes / 60
                    val days = hours / 24

                    expirationPlat.text = when {
                        days > 0 -> "Expire dans $days jour(s)"
                        hours > 0 -> "Expire dans $hours heure(s)"
                        minutes > 0 -> "Expire dans $minutes minute(s)"
                        else -> "Expire bientôt"
                    }
                    expirationPlat.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorBrown))
                } else {
                    expirationPlat.text = "Expiré"
                    expirationPlat.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorSecondary)) // Rouge pour expiré
                }
            } catch (e: Exception) {
                expirationPlat.text = "Date d'expiration non valide"
                expirationPlat.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray_medium))
            }


            // Afficher la distance si la localisation de l'utilisateur est disponible
            if (userLocation != null && plat.latitude != 0.0 && plat.longitude != 0.0) {
                val distance = calculateDistance(
                    userLocation.latitude, userLocation.longitude,
                    plat.latitude, plat.longitude
                )
                distancePlat.text = String.format(Locale.getDefault(), "%.1f km", distance)
                distancePlat.visibility = View.VISIBLE
            } else {
                distancePlat.visibility = View.GONE
            }

            // Gestion du badge "Réservé"
            reservePlat.visibility = if (plat.reserve) View.VISIBLE else View.GONE

            // Charger l'image avec Glide
            if (plat.imageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(plat.imageUrl)
                    .centerCrop()
                    .into(imagePlat)
            } else {
                imagePlat.setImageResource(R.drawable.placeholder_image) // Image par défaut si pas d'URL
            }

            // Gestion des badges de statut (Restes, Plat Complet)
            chipGroupStatut.removeAllViews()
            if (plat.statut.isNotEmpty()) {
                val chip = LayoutInflater.from(itemView.context).inflate(R.layout.chip_item_badge, chipGroupStatut, false) as Chip
                chip.text = plat.statut
                chip.setChipBackgroundColorResource(R.color.colorPrimaryDark) // Exemple de couleur
                chip.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                chipGroupStatut.addView(chip)
                chipGroupStatut.visibility = View.VISIBLE
            } else {
                chipGroupStatut.visibility = View.GONE
            }

            // Gestion des badges de type de plat (Végétarien, Salé, Sucré)
            chipGroupTypePlat.removeAllViews()
            if (plat.typePlat.isNotEmpty()) {
                for (type in plat.typePlat) {
                    val chip = LayoutInflater.from(itemView.context).inflate(R.layout.chip_item_badge, chipGroupTypePlat, false) as Chip
                    chip.text = type
                    val chipColor = when (type) {
                        "Végétarien" -> R.color.colorTertiary
                        "Sucré" -> R.color.colorBrown
                        "Salé" -> R.color.colorPrimary
                        "Halal" -> R.color.gray // Nouvelle couleur
                        else -> R.color.gray_medium // Couleur par défaut
                    }
                    chip.setChipBackgroundColorResource(chipColor)
                    chip.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                    chipGroupTypePlat.addView(chip)
                }
                chipGroupTypePlat.visibility = View.VISIBLE
            } else {
                chipGroupTypePlat.visibility = View.GONE
            }


            btnDetails.setOnClickListener {
                onItemClick(plat)
            }
        }

        // Fonction de calcul de distance (dupliquée mais nécessaire ici pour l'adaptateur)
        private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val R = 6371.0 // Rayon de la Terre en kilomètres
            val latDistance = Math.toRadians(lat2 - lat1)
            val lonDistance = Math.toRadians(lon2 - lon1)
            val a = sin(latDistance / 2) * sin(latDistance / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(lonDistance / 2) * sin(lonDistance / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return R * c // Distance en kilomètres
        }
    }
}
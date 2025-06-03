package com.example.foodshareapp.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodshareapp.R
import com.example.foodshareapp.data.model.Plat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import androidx.navigation.fragment.findNavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlatAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var searchEditText: EditText
    private lateinit var noPlatsTextView: TextView
    private lateinit var shimmerLayout: LinearLayout // Pour le skeleton loading
    private lateinit var chipFilterDistance: Chip
    private lateinit var chipFilterVegetarian: Chip
    private lateinit var chipFilterSweet: Chip
    private lateinit var chipFilterSalty: Chip

    private var allPlats = mutableListOf<Plat>()
    private var filteredPlats = mutableListOf<Plat>()
    private var currentLocation: Location? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialisation des vues
        recyclerView = view.findViewById(R.id.recyclerViewPlats)
        searchEditText = view.findViewById(R.id.searchEditText)
        noPlatsTextView = view.findViewById(R.id.noPlatsTextView)
        shimmerLayout = view.findViewById(R.id.shimmerLayout)
        chipFilterDistance = view.findViewById(R.id.chipFilterDistance)
        chipFilterVegetarian = view.findViewById(R.id.chipFilterVegetarian)
        chipFilterSweet = view.findViewById(R.id.chipFilterSweet)
        chipFilterSalty = view.findViewById(R.id.chipFilterSalty)

        // Initialisation de l'adapter AVANT son utilisation
        adapter = PlatAdapter(
            plats = emptyList(),
            onItemClick = { plat ->  // Spécifiez clairement le paramètre
                val bundle = Bundle().apply {
                    putParcelable("plat", plat)
                }
                findNavController().navigate(R.id.action_homeFragment_to_detailsFragment, bundle)

            },
            userLocation = currentLocation  // Passez la location si disponible
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        db = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Listeners pour la barre de recherche
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                applyFiltersAndSearch()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Listeners pour les chips de filtre
        chipFilterDistance.setOnCheckedChangeListener { _, isChecked ->
            // Demande de permission de localisation si le filtre est activé
            if (isChecked) {
                checkLocationPermission()
            } else {
                currentLocation = null // Réinitialise la localisation si le filtre est désactivé
                applyFiltersAndSearch()
            }
        }
        chipFilterVegetarian.setOnCheckedChangeListener { _, _ -> applyFiltersAndSearch() }
        chipFilterSweet.setOnCheckedChangeListener { _, _ -> applyFiltersAndSearch() }
        chipFilterSalty.setOnCheckedChangeListener { _, _ -> applyFiltersAndSearch() }
        loadPlats()
        return view
    }


    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getLastLocation()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                Toast.makeText(requireContext(), "Permission de localisation refusée. Le tri par distance ne sera pas disponible.", Toast.LENGTH_SHORT).show()
                chipFilterDistance.isChecked = false // Désactive le chip si la permission est refusée
            }
        }
    }

    private fun getLastLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        currentLocation = location
                        applyFiltersAndSearch()
                        Toast.makeText(requireContext(), "Localisation récupérée.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Impossible de récupérer la localisation. Vérifiez que la localisation est activée.", Toast.LENGTH_LONG).show()
                        chipFilterDistance.isChecked = false // Désactive le chip si la localisation n'est pas dispo
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Erreur de localisation : ${e.message}", Toast.LENGTH_LONG).show()
                    chipFilterDistance.isChecked = false // Désactive le chip en cas d'erreur
                }
        }
    }


    private fun loadPlats() {
        showLoadingState()
        db.collection("plats")
            .whereEqualTo("reserve", false) // Filtre pour les plats non réservés
            .orderBy("datePublication", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                hideLoadingState()
                if (error != null) {
                    Toast.makeText(requireContext(), "Erreur de chargement des plats: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                allPlats.clear()
                val currentDate = Date()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

                for (doc in snapshots!!) {
                    val plat = doc.toObject(Plat::class.java)
                    try {
                        val expirationDate = dateFormat.parse(plat.expiration)
                        // Vérifie si la date d'expiration est dans le futur
                        if (expirationDate != null && expirationDate.after(currentDate)) {
                            allPlats.add(plat)
                        }
                    } catch (e: Exception) {
                        Log.e("HomeFragment", "Erreur de parsing de la date: ${e.message}")
                    }
                }
                applyFiltersAndSearch()
            }
    }

    private fun applyFiltersAndSearch() {
        lifecycleScope.launch(Dispatchers.Default) {
            val currentDate = Date()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

            var tempFilteredPlats = allPlats.filter { plat ->
                // Vérification de la date d'expiration
                val isNotExpired = try {
                    val expirationDate = dateFormat.parse(plat.expiration)
                    expirationDate != null && expirationDate.after(currentDate)
                } catch (e: Exception) {
                    false
                }

                // Filtre par recherche texte
                val searchTerm = searchEditText.text.toString().trim()
                val matchesSearch = searchTerm.isEmpty() ||
                        plat.titre.contains(searchTerm, ignoreCase = true) ||
                        plat.ingredients.contains(searchTerm, ignoreCase = true) ||
                        plat.description.contains(searchTerm, ignoreCase = true)

                // Filtre par type de plat
                val matchesType =
                    (!chipFilterVegetarian.isChecked || plat.typePlat.contains("Végétarien")) &&
                            (!chipFilterSweet.isChecked || plat.typePlat.contains("Sucré")) &&
                            (!chipFilterSalty.isChecked || plat.typePlat.contains("Salé"))

                isNotExpired && matchesSearch && matchesType
            }.toMutableList()

            // Tri par distance si le chip est coché et la localisation disponible
            if (chipFilterDistance.isChecked && currentLocation != null) {
                tempFilteredPlats.sortBy { plat ->
                    calculateDistance(
                        currentLocation!!.latitude,
                        currentLocation!!.longitude,
                        plat.latitude,
                        plat.longitude
                    )
                }
            } else {
                // Si pas de filtre distance, tri par date de publication par défaut
                tempFilteredPlats.sortByDescending { it.datePublication }
            }

            withContext(Dispatchers.Main) {
                filteredPlats.clear()
                filteredPlats.addAll(tempFilteredPlats)
                adapter.updateData(filteredPlats)

                if (filteredPlats.isEmpty() && searchEditText.text.toString().trim().isNotEmpty()) {
                    noPlatsTextView.text = "Aucun plat ne correspond à votre recherche."
                    noPlatsTextView.visibility = View.VISIBLE
                } else if (filteredPlats.isEmpty() && searchEditText.text.toString().trim().isEmpty()) {
                    noPlatsTextView.text = "Aucun plat disponible pour l'instant."
                    noPlatsTextView.visibility = View.VISIBLE
                }
                else {
                    noPlatsTextView.visibility = View.GONE
                }
            }
        }
    }

    private fun showLoadingState() {
        recyclerView.visibility = View.GONE
        noPlatsTextView.visibility = View.GONE
        shimmerLayout.visibility = View.VISIBLE // Affiche le layout de squelette
        // Si vous utilisez une bibliothèque Shimmer, démarrez l'animation ici
        // shimmerLayout.startShimmer()
    }

    private fun hideLoadingState() {
        shimmerLayout.visibility = View.GONE // Cache le layout de squelette
        recyclerView.visibility = View.VISIBLE
        // Si vous utilisez une bibliothèque Shimmer, arrêtez l'animation ici
        // shimmerLayout.stopShimmer()
    }

    // Fonction de calcul de distance haversine
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
package com.example.foodshareapp.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.foodshareapp.R
import com.example.foodshareapp.data.model.Plat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Afficher la localisation de l'utilisateur si la permission est accordée
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        }

        // Récupérer les plats depuis Firestore
        FirebaseFirestore.getInstance().collection("plats")
            .get()
            .addOnSuccessListener { result ->
                var isFirstPlat = true

                for (document in result) {
                    val plat = document.toObject(Plat::class.java)
                    if (plat.latitude != 0.0 && plat.longitude != 0.0) {
                        val position = LatLng(plat.latitude, plat.longitude)
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(position)
                                .title(plat.titre)
                                .snippet(plat.description)
                        )

                        if (isFirstPlat) {
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 12f))
                            isFirstPlat = false
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("MapFragment", "Erreur Firestore: ${e.localizedMessage}")
            }
    }
}

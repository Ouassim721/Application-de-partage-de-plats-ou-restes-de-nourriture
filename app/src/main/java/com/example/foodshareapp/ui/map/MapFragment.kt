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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var database: DatabaseReference

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

        // Activer la localisation (si permission OK)
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
        }

        database = FirebaseDatabase.getInstance().getReference("plats")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (platSnapshot in snapshot.children) {
                    val plat = platSnapshot.getValue(Plat::class.java)
                    plat?.let {
                        val location = LatLng(it.latitude, it.longitude)
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(location)
                                .title(it.titre)
                                .snippet(it.description)
                        )
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MapFragment", "Erreur Firebase : ${error.message}")
            }
        })
    }
    fun onDataChange(snapshot: DataSnapshot) {
        var isFirstPlat = true // pour centrer la carte une seule fois

        for (platSnapshot in snapshot.children) {
            val plat = platSnapshot.getValue(Plat::class.java)
            plat?.let {
                val location = LatLng(it.latitude, it.longitude)
                googleMap.addMarker(
                    MarkerOptions()
                        .position(location)
                        .title(it.titre)
                        .snippet(it.description)
                )

                // Centrer la carte sur le premier plat seulement
                if (isFirstPlat) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12f))
                    isFirstPlat = false
                }
            }
        }
    }

}

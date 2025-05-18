package com.example.foodshareapp.ui.publish

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.foodshareapp.R
import com.example.foodshareapp.data.model.Plat
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class PublishFragment : Fragment() {

    private lateinit var imagePreview: ImageView
    private lateinit var btnAddImage: Button
    private lateinit var btnPublier: Button
    private lateinit var btnDetectLocation: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var editTitre: EditText
    private lateinit var editDescription: EditText
    private lateinit var editPortions: EditText
    private lateinit var editExpiration: EditText
    private lateinit var editLocalisation: EditText

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var imageUri: Uri? = null
    private val storageRef by lazy { FirebaseStorage.getInstance().reference }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val user by lazy { FirebaseAuth.getInstance().currentUser }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val LOCATION_ENABLE_REQUEST_CODE = 1002
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_publish, container, false)

        initViews(view)
        initLocationServices()
        setupDatePicker()
        setupButtonListeners()

        return view
    }

    private fun initViews(view: View) {
        imagePreview = view.findViewById(R.id.imagePreview)
        btnAddImage = view.findViewById(R.id.btnAddImage)
        btnPublier = view.findViewById(R.id.btnPublier)
        btnDetectLocation = view.findViewById(R.id.btnDetectLocation)
        progressBar = view.findViewById(R.id.progressBar)
        editTitre = view.findViewById(R.id.editTitre)
        editDescription = view.findViewById(R.id.editDescription)
        editPortions = view.findViewById(R.id.editPortions)
        editExpiration = view.findViewById(R.id.editExpiration)
        editLocalisation = view.findViewById(R.id.editLocalisation)
    }

    private fun initLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 5000
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                fusedLocationClient.removeLocationUpdates(this)
                handleLocation(locationResult.lastLocation)
            }
        }
    }

    private fun setupDatePicker() {
        editExpiration.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    editExpiration.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = System.currentTimeMillis()
                show()
            }
        }
    }

    private fun setupButtonListeners() {
        btnAddImage.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .compress(1024)
                .start()
        }

        btnDetectLocation.setOnClickListener {
            checkLocationPermission()
        }

        btnPublier.setOnClickListener {
            if (validateFields()) {
                showConfirmationDialog()
            } else {
                showValidationError()
            }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            checkLocationSettings()
        }
    }

    private fun checkLocationSettings() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(requireContext())
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            // GPS activé, démarrer la détection
            startLocationUpdates()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // GPS désactivé, proposer de l'activer
                try {
                    exception.startResolutionForResult(
                        requireActivity(),
                        LOCATION_ENABLE_REQUEST_CODE
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Toast.makeText(requireContext(), "Erreur d'activation du GPS", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Service de localisation indisponible", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        btnDetectLocation.isEnabled = false
        btnDetectLocation.text = "Détection en cours..."

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    private fun handleLocation(location: Location?) {
        btnDetectLocation.isEnabled = true
        btnDetectLocation.text = "Détecter ma position"

        if (location != null) {
            try {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val locationText = buildString {
                        address.locality?.let { append(it) }
                        address.thoroughfare?.let {
                            if (isNotEmpty()) append(", ")
                            append(it)
                        }
                    }
                    editLocalisation.setText(locationText)
                } else {
                    Toast.makeText(requireContext(), "Adresse introuvable", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PublishFragment", "Geocoding error", e)
                Toast.makeText(requireContext(), "Erreur de géocodage", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Localisation non disponible", Toast.LENGTH_SHORT).show()
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
                checkLocationSettings()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permission refusée - La localisation ne fonctionnera pas",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            LOCATION_ENABLE_REQUEST_CODE -> {
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    checkLocationSettings()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "GPS non activé - La localisation ne fonctionnera pas",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else -> {
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    imageUri = data?.data
                    imagePreview.setImageURI(imageUri)
                }
            }
        }
    }

    private fun validateFields(): Boolean {
        return !(editTitre.text.isNullOrEmpty() ||
                editDescription.text.isNullOrEmpty() ||
                editExpiration.text.isNullOrEmpty() ||
                editLocalisation.text.isNullOrEmpty() ||
                imageUri == null)
    }

    private fun showValidationError() {
        Toast.makeText(
            requireContext(),
            "Veuillez remplir tous les champs et choisir une image",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle("Confirmer la publication")
            setMessage("Voulez-vous publier ce plat sur la plateforme ?")
            setPositiveButton("Publier") { dialog, _ ->
                dialog.dismiss()
                uploadPlat()
            }
            setNegativeButton("Annuler") { dialog, _ ->
                dialog.dismiss()
            }
            setCancelable(false)
        }.show()
    }

    private fun uploadPlat() {
        progressBar.visibility = View.VISIBLE
        btnPublier.isEnabled = false

        val imageName = UUID.randomUUID().toString()
        val imageRef = storageRef.child("images_plats/$imageName.jpg")

        imageUri?.let { uri ->
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        savePlatToFirestore(downloadUri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    handleUploadError(e)
                }
        } ?: run {
            Toast.makeText(requireContext(), "Erreur: image non valide", Toast.LENGTH_SHORT).show()
            resetUploadState()
        }
    }

    private fun savePlatToFirestore(imageUrl: String) {
        val plat = Plat(
            titre = editTitre.text.toString().trim(),
            description = editDescription.text.toString().trim(),
            portions = editPortions.text.toString().toIntOrNull() ?: 1,
            expiration = editExpiration.text.toString().trim(),
            localisation = editLocalisation.text.toString().trim(),
            imageUrl = imageUrl,
            datePublication = Timestamp.now(),
            userId = user?.uid ?: ""
        )

        db.collection("plats").add(plat)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Plat publié avec succès!",
                    Toast.LENGTH_SHORT
                ).show()
                clearFields()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Erreur: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnCompleteListener {
                resetUploadState()
            }
    }

    private fun handleUploadError(e: Exception) {
        Log.e("PublishFragment", "Upload error", e)
        Toast.makeText(
            requireContext(),
            "Échec de la publication: ${e.localizedMessage}",
            Toast.LENGTH_LONG
        ).show()
        resetUploadState()
    }

    private fun resetUploadState() {
        progressBar.visibility = View.GONE
        btnPublier.isEnabled = true
    }

    private fun clearFields() {
        editTitre.text.clear()
        editDescription.text.clear()
        editPortions.text.clear()
        editExpiration.text.clear()
        editLocalisation.text.clear()
        imagePreview.setImageResource(R.drawable.ic_round_add)
        imageUri = null
    }
}
package com.example.foodshareapp.ui.publish

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
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
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class PublishFragment : Fragment() {

    private lateinit var imagePreview: ImageView
    private lateinit var btnAddImage: Button
    private lateinit var btnPublier: Button
    private lateinit var btnPreview: Button
    private lateinit var btnDetectLocation: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var editTitre: EditText
    private lateinit var editDescription: EditText
    private lateinit var editIngredients: EditText
    private lateinit var editPortions: EditText
    private lateinit var editExpiration: EditText
    private lateinit var editHeureRecuperation: EditText
    private lateinit var editLocalisation: EditText
    private lateinit var radioGroupStatut: RadioGroup
    private lateinit var chipGroupType: ChipGroup
    private lateinit var formProgressBar: ProgressBar
    private lateinit var textFormProgress: TextView

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var imageUri: Uri? = null
    private val storageRef by lazy { FirebaseStorage.getInstance().reference }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val user by lazy { FirebaseAuth.getInstance().currentUser }
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val LOCATION_ENABLE_REQUEST_CODE = 1002
        private const val MAX_TITLE_LENGTH = 50
        private const val MAX_DESCRIPTION_LENGTH = 500
        private const val MAX_INGREDIENTS_LENGTH = 500
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_publish, container, false)

        initViews(view)
        setupInputLimits()
        initLocationServices()
        setupDateTimePickers()
        setupChipGroup(view)
        setupButtonListeners()
        updateFormProgress()

        return view
    }

    private fun initViews(view: View) {
        imagePreview = view.findViewById(R.id.imagePreview)
        btnAddImage = view.findViewById(R.id.btnAddImage)
        btnPublier = view.findViewById(R.id.btnPublier)
        btnPreview = view.findViewById(R.id.btnPreview)
        btnDetectLocation = view.findViewById(R.id.btnDetectLocation)
        progressBar = view.findViewById(R.id.progressBar)
        editTitre = view.findViewById(R.id.editTitre)
        editDescription = view.findViewById(R.id.editDescription)
        editIngredients = view.findViewById(R.id.editIngredients)
        editPortions = view.findViewById(R.id.editPortions)
        editExpiration = view.findViewById(R.id.editExpiration)
        editHeureRecuperation = view.findViewById(R.id.editHeureRecuperation)
        editLocalisation = view.findViewById(R.id.editLocalisation)
        radioGroupStatut = view.findViewById(R.id.radioGroupStatut)
        chipGroupType = view.findViewById(R.id.chipGroupType)
        formProgressBar = view.findViewById(R.id.formProgressBar)
        textFormProgress = view.findViewById(R.id.textFormProgress)
    }

    private fun setupInputLimits() {
        // Limiter la longueur des champs texte
        editTitre.filters = arrayOf(InputFilter.LengthFilter(MAX_TITLE_LENGTH))
        editDescription.filters = arrayOf(InputFilter.LengthFilter(MAX_DESCRIPTION_LENGTH))
        editIngredients.filters = arrayOf(InputFilter.LengthFilter(MAX_INGREDIENTS_LENGTH))

        // Ne permettre que des nombres positifs pour les portions
        editPortions.inputType = InputType.TYPE_CLASS_NUMBER
    }

    private fun setupChipGroup(view: View) {
        // S'assurer que chipGroupType est correctement initialisé
        val typeOptions =
            listOf("Salé", "Sucré", "Végétarien", "Végan", "Sans gluten", "Sans lactose")

        for (option in typeOptions) {
            val chip = Chip(requireContext())
            chip.text = option
            chip.isCheckable = true
            chipGroupType.addView(chip)
        }

        // Écouteur pour mettre à jour la progression du formulaire
        chipGroupType.setOnCheckedStateChangeListener { _, _ -> updateFormProgress() }
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

    private fun setupDateTimePickers() {
        // Sélecteur de date d'expiration
        editExpiration.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    editExpiration.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
                    updateFormProgress()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = System.currentTimeMillis()
                show()
            }
        }

        // Sélecteur d'heure limite de récupération
        editHeureRecuperation.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    editHeureRecuperation.setText(String.format("%02d:%02d", hourOfDay, minute))
                    updateFormProgress()
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
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

        btnPreview.setOnClickListener {
            if (validateFields(showErrors = true)) {
                showPreviewDialog()
            }
        }

        btnPublier.setOnClickListener {
            if (validateFields(showErrors = true)) {
                showConfirmationDialog()
            }
        }

        // Écouteurs pour mettre à jour la progression du formulaire
        val textWatchers =
            arrayOf(editTitre, editDescription, editPortions, editExpiration, editHeureRecuperation)
        for (editText in textWatchers) {
            editText.addTextChangedListener(object : android.text.TextWatcher {
                override fun afterTextChanged(s: android.text.Editable?) {
                    updateFormProgress()
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

        radioGroupStatut.setOnCheckedChangeListener { _, _ -> updateFormProgress() }
    }

    private fun updateFormProgress() {
        // Calculer la progression du formulaire
        var fieldsCompleted = 0
        var totalRequiredFields =
            7 // champs obligatoires: titre, description, portions, expiration, heure, localisation, image, statut

        if (!editTitre.text.isNullOrEmpty()) fieldsCompleted++
        if (!editDescription.text.isNullOrEmpty()) fieldsCompleted++
        if (!editPortions.text.isNullOrEmpty()) fieldsCompleted++
        if (!editExpiration.text.isNullOrEmpty()) fieldsCompleted++
        if (!editHeureRecuperation.text.isNullOrEmpty()) fieldsCompleted++
        if (!editLocalisation.text.isNullOrEmpty()) fieldsCompleted++
        if (imageUri != null) fieldsCompleted++
        if (radioGroupStatut.checkedRadioButtonId != -1) fieldsCompleted++

        // Mise à jour de la progress bar
        val progressPercent = (fieldsCompleted.toFloat() / totalRequiredFields) * 100
        formProgressBar.progress = progressPercent.toInt()
        textFormProgress.text = "$fieldsCompleted/$totalRequiredFields"
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
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
                    Toast.makeText(
                        requireContext(),
                        "Erreur d'activation du GPS",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Service de localisation indisponible",
                    Toast.LENGTH_SHORT
                ).show()
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
                currentLatitude = location.latitude
                currentLongitude = location.longitude
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
                    updateFormProgress()
                } else {
                    Toast.makeText(requireContext(), "Adresse introuvable", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                Log.e("PublishFragment", "Geocoding error", e)
                Toast.makeText(requireContext(), "Erreur de géocodage", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Localisation non disponible", Toast.LENGTH_SHORT)
                .show()
        }
    }

    @Deprecated("Deprecated in Java")
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
                    if (imageUri != null) {
                        imagePreview.setImageURI(imageUri)
                        updateFormProgress()
                    }
                }
            }
        }
    }

    private fun validateFields(showErrors: Boolean = false): Boolean {
        val validations = mapOf(
            "titre" to !editTitre.text.isNullOrEmpty(),
            "description" to !editDescription.text.isNullOrEmpty(),
            "portions" to (!editPortions.text.isNullOrEmpty() && editPortions.text.toString()
                .toIntOrNull() ?: 0 > 0),
            "expiration" to !editExpiration.text.isNullOrEmpty(),
            "heure" to !editHeureRecuperation.text.isNullOrEmpty(),
            "localisation" to !editLocalisation.text.isNullOrEmpty(),
            "image" to (imageUri != null),
            "statut" to (radioGroupStatut.checkedRadioButtonId != -1)
        )

        val valid = validations.values.all { it }

        if (!valid && showErrors) {
            val missingFields = validations.filter { !it.value }.keys.joinToString(", ")
            val message = "Veuillez compléter les champs suivants: $missingFields"
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }

        return valid
    }

    private fun getSelectedFoodTypes(): List<String> {
        val selectedTypes = mutableListOf<String>()
        for (i in 0 until chipGroupType.childCount) {
            val chip = chipGroupType.getChildAt(i) as? Chip
            if (chip != null && chip.isChecked) {
                selectedTypes.add(chip.text.toString())
            }
        }
        return selectedTypes
    }

    private fun getStatut(): String {
        return when (radioGroupStatut.checkedRadioButtonId) {
            R.id.radioRestes -> "Restes"
            R.id.radioPrepare -> "Plat préparé"
            else -> ""
        }
    }

    private fun showPreviewDialog() {
        val previewView = LayoutInflater.from(requireContext()).inflate(R.layout.preview_plat, null)

        // Remplir la prévisualisation avec les données saisies
        previewView.findViewById<ImageView>(R.id.previewImage).setImageURI(imageUri)
        previewView.findViewById<TextView>(R.id.previewTitre).text = editTitre.text.toString()
        previewView.findViewById<TextView>(R.id.previewDescription).text =
            editDescription.text.toString()
        previewView.findViewById<TextView>(R.id.previewPortions).text =
            "${editPortions.text} portion(s)"
        previewView.findViewById<TextView>(R.id.previewDateExpiration).text =
            "À consommer avant : ${editExpiration.text} à ${editHeureRecuperation.text}"
        previewView.findViewById<TextView>(R.id.previewLocalisation).text =
            editLocalisation.text.toString()
        previewView.findViewById<TextView>(R.id.previewStatut).text = getStatut()

        val typeText = getSelectedFoodTypes().joinToString(", ")
        previewView.findViewById<TextView>(R.id.previewType).text =
            if (typeText.isNotEmpty()) typeText else "Non spécifié"

        val ingredientsText = editIngredients.text.toString()
        val previewIngredients = previewView.findViewById<TextView>(R.id.previewIngredients)
        if (ingredientsText.isNotEmpty()) {
            previewIngredients.text = ingredientsText
            previewIngredients.visibility = View.VISIBLE
            previewView.findViewById<TextView>(R.id.previewIngredientsLabel).visibility =
                View.VISIBLE
        } else {
            previewIngredients.visibility = View.GONE
            previewView.findViewById<TextView>(R.id.previewIngredientsLabel).visibility = View.GONE
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Prévisualisation de l'annonce")
            .setView(previewView)
            .setPositiveButton("Publier") { dialog, _ ->
                dialog.dismiss()
                showConfirmationDialog()
            }
            .setNegativeButton("Modifier") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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
        val platId = db.collection("plats").document().id

        val plat = Plat(
            id = platId,
            titre = editTitre.text.toString().trim(),
            description = editDescription.text.toString().trim(),
            portions = editPortions.text.toString().toIntOrNull() ?: 1,
            expiration = editExpiration.text.toString()
                .trim() + " " + editHeureRecuperation.text.toString().trim(),
            localisation = editLocalisation.text.toString().trim(),
            imageUrl = imageUrl,
            datePublication = Timestamp.now(),
            latitude = currentLatitude ?: 0.0,
            longitude = currentLongitude ?: 0.0,
            userId = user?.uid ?: "",
            ingredients = editIngredients.text.toString().trim(),
            statut = getStatut(),
            typePlat = getSelectedFoodTypes()
        )

        db.collection("plats").document(platId).set(plat)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Plat publié avec succès !",
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
        editIngredients.text.clear()
        editPortions.text.clear()
        editExpiration.text.clear()
        editHeureRecuperation.text.clear()
        editLocalisation.text.clear()
        radioGroupStatut.clearCheck()

        // Désélectionner tous les chips
        for (i in 0 until chipGroupType.childCount) {
            val chip = chipGroupType.getChildAt(i) as? Chip
            chip?.isChecked = false
        }

        imagePreview.setImageResource(R.drawable.ic_round_add)
        imageUri = null

        updateFormProgress()
    }
}
package com.example.foodshareapp.ui.publish

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.foodshareapp.R
import com.example.foodshareapp.data.model.Plat
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.UUID

class PublishFragment : Fragment() {

    private lateinit var imagePreview: ImageView
    private lateinit var btnAddImage: Button
    private lateinit var btnPublier: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var editTitre: EditText
    private lateinit var editDescription: EditText
    private lateinit var editPortions: EditText
    private lateinit var editExpiration: EditText
    private lateinit var editLocalisation: EditText

    private var imageUri: Uri? = null
    private val storageRef by lazy { FirebaseStorage.getInstance().reference }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val user by lazy { FirebaseAuth.getInstance().currentUser }

    @SuppressLint("DefaultLocale")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_publish, container, false)

        initViews(view)
        setupDatePicker()
        setupButtonListeners()

        return view
    }

    private fun initViews(view: View) {
        imagePreview = view.findViewById(R.id.imagePreview)
        btnAddImage = view.findViewById(R.id.btnAddImage)
        btnPublier = view.findViewById(R.id.btnPublier)
        progressBar = view.findViewById(R.id.progressBar)
        editTitre = view.findViewById(R.id.editTitre)
        editDescription = view.findViewById(R.id.editDescription)
        editPortions = view.findViewById(R.id.editPortions)
        editExpiration = view.findViewById(R.id.editExpiration)
        editLocalisation = view.findViewById(R.id.editLocalisation)
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
            ImagePicker.Companion.with(this)
                .galleryOnly()
                .crop()
                .compress(1024)
                .start()
        }

        btnPublier.setOnClickListener {
            if (validateFields()) {
                showConfirmationDialog()
            } else {
                showValidationError()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            imageUri = data?.data
            imagePreview.setImageURI(imageUri)
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
            datePublication = Timestamp.Companion.now(),
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
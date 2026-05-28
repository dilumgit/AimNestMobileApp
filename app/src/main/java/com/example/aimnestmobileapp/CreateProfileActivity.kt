package com.example.aimnestmobileapp

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class CreateProfileActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private lateinit var sharedPreferences: SharedPreferences

    // Register activity result launcher for image picker
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Update the profile photo ImageView with selected image
            val profilePhoto = findViewById<ImageView>(R.id.profile_photo)
            try {
                profilePhoto.setImageURI(it)
                profilePhoto.scaleType = ImageView.ScaleType.CENTER_CROP
            } catch (e: Exception) {
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_profile)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("ProfileData", MODE_PRIVATE)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get references to the input fields and button
        val fullNameInput = findViewById<TextInputEditText>(R.id.full_name_input)
        val emailInput = findViewById<TextInputEditText>(R.id.email_input)
        val saveButton = findViewById<MaterialButton>(R.id.save_profile_button)
        val profilePhoto = findViewById<ImageView>(R.id.profile_photo)
        val uploadPhotoText = findViewById<TextView>(R.id.upload_photo_text)

        // Prevent floating hint overlap so typed text stays clearly visible.
        fullNameInput.hint = "Full Name"
        emailInput.hint = "Email Address"

        // Set click listener on profile photo to open image picker
        profilePhoto.setOnClickListener {
            openImagePicker()
        }

        // Set click listener on upload photo text to open image picker
        uploadPhotoText.setOnClickListener {
            openImagePicker()
        }

        // Set click listener on Save Profile button
        saveButton.setOnClickListener {
            val fullName = fullNameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()

            // Validate inputs
            if (fullName.isEmpty()) {
                Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidEmail(email)) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Extract first name from full name
            val firstName = fullName.split(" ")[0]

            // Save profile data to SharedPreferences
            saveProfileToSharedPreferences(fullName, email, firstName)

            // Navigate to Dashboard
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
            finish() // Close CreateProfileActivity so user can't go back
        }
    }

    // Helper function to open image picker
    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    // Function to save profile data to SharedPreferences
    private fun saveProfileToSharedPreferences(fullName: String, email: String, firstName: String) {
        val editor = sharedPreferences.edit()
        editor.putString("fullName", fullName)
        editor.putString("email", email)
        editor.putString("firstName", firstName)

        // Save image URI if available
        if (selectedImageUri != null) {
            try {
                val cachedImageUri = copyImageToCache(selectedImageUri!!)
                if (cachedImageUri != null) {
                    editor.putString("profileImageUri", cachedImageUri)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        editor.apply()
    }

    // Function to copy image from URI to app cache directory
    private fun copyImageToCache(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val cacheDir = cacheDir
            val fileName = "profile_image.jpg"
            val cachedFile = File(cacheDir, fileName)

            inputStream?.use { input ->
                cachedFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            cachedFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Function to validate email format
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

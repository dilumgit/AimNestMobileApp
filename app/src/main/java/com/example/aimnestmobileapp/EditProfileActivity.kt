package com.example.aimnestmobileapp

import android.os.Bundle
import android.content.Intent
import android.content.SharedPreferences
import android.widget.ImageView
import android.widget.TextView
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import android.graphics.Color
import android.net.Uri
import java.io.File

class EditProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var cameraIcon: ImageView
    private lateinit var changePhotoText: TextView
    private lateinit var profileImageContainer: android.widget.FrameLayout
    private lateinit var fullNameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var updateProfileButton: MaterialButton
    private lateinit var cancelButton: MaterialButton
    private lateinit var sharedPreferences: SharedPreferences
    private var selectedImageUri: Uri? = null
    private var hasPendingImagePreview: Boolean = false
    private val profileImageFileName = "profile_image.jpg"

    // Register for activity result to handle image selection
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = uri
            hasPendingImagePreview = true
            displayProfileImage(profileImage, uri.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("ProfileData", MODE_PRIVATE)

        // Initialize views
        profileImage = findViewById(R.id.profileImage)
        cameraIcon = findViewById(R.id.cameraIcon)
        changePhotoText = findViewById(R.id.changePhotoText)
        profileImageContainer = findViewById(R.id.profileImageContainer)
        fullNameInput = findViewById(R.id.fullNameInput)
        emailInput = findViewById(R.id.emailInput)
        updateProfileButton = findViewById(R.id.updateProfileButton)
        cancelButton = findViewById(R.id.cancelButton)

        // Load existing profile data from SharedPreferences
        loadProfileData()

        // Back button
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            navigateToProfileOverview()
        }

        // Set click listeners for photo selection
        profileImage.setOnClickListener {
            openImagePicker()
        }

        cameraIcon.setOnClickListener {
            openImagePicker()
        }

        changePhotoText.setOnClickListener {
            openImagePicker()
        }

        profileImageContainer.setOnClickListener {
            openImagePicker()
        }

        // Update Profile Button
        updateProfileButton.setOnClickListener {
            updateProfile()
        }

        // Cancel Button
        cancelButton.setOnClickListener {
            navigateToProfileOverview()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        if (!hasPendingImagePreview) {
            loadProfileData()
        }
    }

    // Function to open image picker
    private fun openImagePicker() {
        pickImageLauncher.launch("image/*")
    }

    // Function to navigate back to Profile Overview
    private fun navigateToProfileOverview() {
        val intent = Intent(this, ProfileOverviewActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Function to update profile
    private fun updateProfile() {
        val fullName = fullNameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()

        // Validate inputs
        if (fullName.isEmpty()) {
            Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show()
            return
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        val firstName = extractFirstName(fullName)

        // Save profile data to SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putString("fullName", fullName)
        editor.putString("email", email)
        editor.putString("firstName", firstName)

        // Save image if a new image was selected
        if (selectedImageUri != null) {
            try {
                val savedPath = copyImageToInternalStorage(selectedImageUri!!)
                if (savedPath != null) {
                    editor.putString("profileImageUri", savedPath)
                    hasPendingImagePreview = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        editor.apply()

        // Show success message
        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()

        // Navigate to Profile Overview after successful update
        navigateToProfileOverview()
    }

    // Function to validate email format
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Function to load existing profile data from SharedPreferences
    private fun loadProfileData() {
        try {
            val savedFullName = sharedPreferences.getString("fullName", "Kavindu Hasaranga") ?: "Kavindu Hasaranga"
            val savedEmail = sharedPreferences.getString("email", "kavinduhasaranga@gmail.com") ?: "kavinduhasaranga@gmail.com"
            val savedImagePath = sharedPreferences.getString("profileImageUri", null)

            fullNameInput.setText(savedFullName)
            emailInput.setText(savedEmail)
            displayProfileImage(profileImage, savedImagePath)
            if (!savedImagePath.isNullOrBlank()) {
                selectedImageUri = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            setDefaultProfileImage(profileImage)
        }
    }

    private fun copyImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val profileDir = File(filesDir, "profile")
            if (!profileDir.exists()) {
                profileDir.mkdirs()
            }

            val imageFile = File(profileDir, profileImageFileName)
            inputStream.use { input ->
                imageFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            imageFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun displayProfileImage(imageView: ImageView, storedValue: String?) {
        imageView.setImageDrawable(null)
        imageView.setBackgroundColor(Color.TRANSPARENT)

        if (storedValue.isNullOrBlank()) {
            setDefaultProfileImage(imageView)
            return
        }

        val file = File(storedValue)
        val loaded = if (file.exists()) {
            imageView.setImageURI(Uri.fromFile(file))
            imageView.drawable != null
        } else {
            imageView.setImageURI(Uri.parse(storedValue))
            imageView.drawable != null
        }

        if (loaded) {
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        } else {
            setDefaultProfileImage(imageView)
        }
    }

    private fun setDefaultProfileImage(imageView: ImageView) {
        imageView.setImageResource(R.drawable.profilepic)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
    }

    private fun extractFirstName(fullName: String): String {
        return fullName
            .trim()
            .split("\\s+".toRegex())
            .firstOrNull { it.isNotBlank() }
            ?: "User"
    }
}

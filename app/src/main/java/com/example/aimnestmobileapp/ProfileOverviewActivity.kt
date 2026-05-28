package com.example.aimnestmobileapp

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class ProfileOverviewActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile_overview)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("ProfileData", MODE_PRIVATE)

        // Load and display profile data
        loadAndDisplayProfileData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up back button click listener to navigate to Dashboard
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
            finish()
        }

        // Edit Profile button navigates to EditProfileActivity
        val editProfileButton = findViewById<MaterialButton>(R.id.editProfileButton)
        editProfileButton.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Delete Profile button navigates to DeleteProfileActivity
        val deleteProfileButton = findViewById<MaterialButton>(R.id.deleteProfileButton)
        deleteProfileButton.setOnClickListener {
            startActivity(Intent(this, DeleteProfileActivity::class.java))
        }

        // Footer Menu - Manage Income button
        val menuItemManageIncome = findViewById<LinearLayout>(R.id.menuItemManageIncome)
        menuItemManageIncome.setOnClickListener {
            startActivity(Intent(this, income_list::class.java))
        }

        // Footer Menu - Manage Expenses button
        val menuItemManageExpenses = findViewById<LinearLayout>(R.id.menuItemManageExpenses)
        menuItemManageExpenses.setOnClickListener {
            startActivity(Intent(this, ExpensesListActivity::class.java))
        }

        // Footer Menu - Manage Target button
        val menuItemManageTarget = findViewById<LinearLayout>(R.id.menuItemManageTarget)
        menuItemManageTarget.setOnClickListener {
            startActivity(Intent(this, target_list::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadAndDisplayProfileData()
    }

    // Function to load and display profile data from SharedPreferences
    private fun loadAndDisplayProfileData() {
        try {
            // Retrieve saved profile data
            val fullName = sharedPreferences.getString("fullName", "Kavindu Hasaranga") ?: "Kavindu Hasaranga"
            val email = sharedPreferences.getString("email", "kavinduhasaranga@gmail.com") ?: "kavinduhasaranga@gmail.com"
            val savedImagePath = sharedPreferences.getString("profileImageUri", null)

            // Update UI with saved data
            val userNameTextView = findViewById<TextView>(R.id.userName)
            val userEmailTextView = findViewById<TextView>(R.id.userEmail)
            val fullNameValueTextView = findViewById<TextView>(R.id.fullNameValue)
            val emailValueTextView = findViewById<TextView>(R.id.emailValue)
            val profileImageView = findViewById<ImageView>(R.id.profileImage)

            // Set the text values
            userNameTextView?.text = fullName
            userEmailTextView?.text = email
            fullNameValueTextView?.text = fullName
            emailValueTextView?.text = email

            // Set the profile image if available
            if (profileImageView != null) {
                profileImageView.setImageDrawable(null)
                profileImageView.setBackgroundColor(Color.TRANSPARENT)

                if (savedImagePath.isNullOrBlank()) {
                    setDefaultProfileImage(profileImageView)
                    return
                }

                val imageFile = java.io.File(savedImagePath)
                val loaded = if (imageFile.exists()) {
                    profileImageView.setImageURI(Uri.fromFile(imageFile))
                    profileImageView.drawable != null
                } else {
                    profileImageView.setImageURI(Uri.parse(savedImagePath))
                    profileImageView.drawable != null
                }

                if (loaded) {
                    profileImageView.scaleType = ImageView.ScaleType.CENTER_CROP
                } else {
                    setDefaultProfileImage(profileImageView)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val profileImageView = findViewById<ImageView>(R.id.profileImage)
            setDefaultProfileImage(profileImageView)
        }
    }

    private fun setDefaultProfileImage(imageView: ImageView) {
        imageView.setImageResource(R.drawable.profilepic)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
    }
}
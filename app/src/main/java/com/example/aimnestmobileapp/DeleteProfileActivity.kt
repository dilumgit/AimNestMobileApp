package com.example.aimnestmobileapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class DeleteProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_delete_profile)

        val deleteButton: MaterialButton = findViewById(R.id.dialogDeleteButton)
        val cancelButton: MaterialButton = findViewById(R.id.dialogCancelButton)

        deleteButton.setOnClickListener {
            // Clear all profile data from SharedPreferences
            val sharedPreferences = getSharedPreferences("ProfileData", MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()

            Toast.makeText(this, "Profile deleted successfully", Toast.LENGTH_SHORT).show()

            // Navigate to Create Profile screen and clear back stack
            val intent = Intent(this, CreateProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        cancelButton.setOnClickListener {
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
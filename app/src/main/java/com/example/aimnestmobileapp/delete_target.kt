package com.example.aimnestmobileapp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import org.json.JSONArray

class delete_target : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_delete_target)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val targetName = intent.getStringExtra("target_name").orEmpty()
        val targetAmount = intent.getStringExtra("target_amount").orEmpty()

        val deleteButton = findViewById<MaterialButton?>(R.id.deleteButton)
        val cancelButton = findViewById<MaterialButton?>(R.id.cancelButton)

        cancelButton?.setOnClickListener {
            startActivity(Intent(this, target_details::class.java).apply {
                putExtra("target_name", targetName)
                putExtra("target_amount", targetAmount)
                putExtra("target_deadline", intent.getStringExtra("target_deadline").orEmpty())
                putExtra("target_priority", intent.getStringExtra("target_priority").orEmpty())
            })
            finish()
        }

        deleteButton?.setOnClickListener {
            val prefs = getSharedPreferences("targets_prefs", MODE_PRIVATE)
            val json = prefs.getString("targets_json", "[]") ?: "[]"
            val targets = JSONArray(json)

            for (i in 0 until targets.length()) {
                val target = targets.getJSONObject(i)
                if (target.optString("name") == targetName) {
                    targets.remove(i)
                    break
                }
            }

            prefs.edit().putString("targets_json", targets.toString()).apply()

            startActivity(Intent(this, target_list::class.java))
            finish()
        }
    }
}
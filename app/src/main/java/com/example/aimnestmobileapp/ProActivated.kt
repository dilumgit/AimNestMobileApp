package com.example.aimnestmobileapp

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProActivated : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pro_activated)

        val renewalText = findViewById<TextView>(R.id.renewalDateText)
        val calendar = Calendar.getInstance().apply { add(Calendar.YEAR, 1) }
        val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(calendar.time)
        renewalText.text = "Next renewal : $formattedDate"

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
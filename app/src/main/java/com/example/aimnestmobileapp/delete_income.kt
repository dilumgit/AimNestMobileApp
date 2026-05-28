package com.example.aimnestmobileapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONArray

class delete_income : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_delete_income)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val deleteButton = findViewById<Button>(R.id.deleteButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)

        val incomeIndex = intent.getIntExtra("income_index", -1)

        deleteButton.setOnClickListener {
            if (incomeIndex >= 0) {
                deleteIncome(incomeIndex)
            }
            startActivity(Intent(this, income_list::class.java))
            finish()
        }

        cancelButton.setOnClickListener {
            startActivity(Intent(this, income_list::class.java))
            finish()
        }
    }

    private fun deleteIncome(index: Int) {
        val prefs = getSharedPreferences("aimnest_income_data", MODE_PRIVATE)
        val json = prefs.getString("saved_incomes", "[]") ?: "[]"
        val array = JSONArray(json)

        // Create new array without the deleted item
        val newArray = JSONArray()
        for (i in 0 until array.length()) {
            if (i != index) {
                newArray.put(array.getJSONObject(i))
            }
        }

        // Save updated array
        prefs.edit().putString("saved_incomes", newArray.toString()).apply()
    }
}
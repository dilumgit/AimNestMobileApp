package com.example.aimnestmobileapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import org.json.JSONArray

class DeleteExpensesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_delete_expenses)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val cancelButton = findViewById<MaterialButton>(R.id.cancelButton)
        val deleteButton = findViewById<MaterialButton>(R.id.deleteButton)
        val expenseIndex = intent.getIntExtra("expense_index", -1)

        cancelButton.setOnClickListener {
            startActivity(Intent(this, ExpensesListActivity::class.java))
            finish()
        }

        deleteButton.setOnClickListener {
            if (expenseIndex < 0) {
                Toast.makeText(this, "Unable to delete expense", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ExpensesListActivity::class.java))
                finish()
                return@setOnClickListener
            }

            val prefs = getSharedPreferences("aimnest_expense_data", MODE_PRIVATE)
            val json = prefs.getString("saved_expenses", "[]") ?: "[]"
            val array = JSONArray(json)

            if (expenseIndex in 0 until array.length()) {
                val updated = JSONArray()
                for (i in 0 until array.length()) {
                    if (i != expenseIndex) {
                        updated.put(array.getJSONObject(i))
                    }
                }
                prefs.edit().putString("saved_expenses", updated.toString()).apply()
                Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Unable to delete expense", Toast.LENGTH_SHORT).show()
            }

            startActivity(Intent(this, ExpensesListActivity::class.java))
            finish()
        }
    }
}
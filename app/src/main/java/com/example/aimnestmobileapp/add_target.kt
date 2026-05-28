package com.example.aimnestmobileapp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import org.json.JSONArray
import org.json.JSONObject
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class add_target : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_target)

        findViewById<View?>(R.id.main)?.let { root ->
            ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        findViewById<ImageView?>(R.id.backButton)?.setOnClickListener {
            startActivity(Intent(this, target_list::class.java))
            finish()
        }

        val deadlineInputText = findViewById<TextView?>(R.id.deadlineInputText)
        val calendarIcon = findViewById<ImageView?>(R.id.calendarIcon)
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        if (deadlineInputText?.text.isNullOrBlank()) {
            deadlineInputText?.text = formatter.format(calendar.time)
        }

        val openDatePicker = {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    deadlineInputText?.text = formatter.format(calendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        deadlineInputText?.setOnClickListener { openDatePicker() }
        calendarIcon?.setOnClickListener { openDatePicker() }

        val footerPanel = findViewById<LinearLayout?>(R.id.footerPanel)
        if (footerPanel != null && footerPanel.childCount >= 4) {
            footerPanel.getChildAt(0)?.setOnClickListener {
                startActivity(Intent(this, income_list::class.java))
                finish()
            }
            footerPanel.getChildAt(1)?.setOnClickListener {
                startActivity(Intent(this, ExpensesListActivity::class.java))
                finish()
            }
            footerPanel.getChildAt(2)?.setOnClickListener {
                startActivity(Intent(this, target_list::class.java))
                finish()
            }
            footerPanel.getChildAt(3)?.setOnClickListener {
                // Navigate to Overall Analysis screen
                startActivity(Intent(this, AnalysisActivity::class.java))
                finish()
            }
        }

        val prioritySpinner = findViewById<Spinner?>(R.id.prioritySpinner)
        val priorityOptions = listOf(
            "High - Urgent",
            "Medium - Important but not urgent",
            "Low - Optional"
        )
        val priorityAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            priorityOptions
        )
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        prioritySpinner?.adapter = priorityAdapter
        prioritySpinner?.setSelection(1)

        val saveTargetButton = findViewById<MaterialButton?>(R.id.saveTargetButton)
        val targetNameInput = findViewById<EditText?>(R.id.targetNameInput)
        val targetAmountInput = findViewById<EditText?>(R.id.targetAmountInput)

        saveTargetButton?.setOnClickListener {
            val name = targetNameInput?.text?.toString()?.trim().orEmpty()
            val amount = targetAmountInput?.text?.toString()?.trim().orEmpty()
            val deadline = deadlineInputText?.text?.toString()?.trim().orEmpty()
            val priority = prioritySpinner?.selectedItem?.toString()?.trim().orEmpty()

            if (name.isBlank() || amount.isBlank() || deadline.isBlank()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = getSharedPreferences("targets_prefs", MODE_PRIVATE)
            val existing = prefs.getString("targets_json", "[]") ?: "[]"
            val arr = try { JSONArray(existing) } catch (_: Exception) { JSONArray() }

            val item = JSONObject().apply {
                put("name", name)
                put("amount", amount)
                put("deadline", deadline)
                put("priority", priority)
            }
            arr.put(item)

            prefs.edit().putString("targets_json", arr.toString()).apply()
            startActivity(Intent(this, target_list::class.java))
            finish()
        }

        val cancelTargetButton = findViewById<MaterialButton?>(R.id.cancelTargetButton)
        cancelTargetButton?.setOnClickListener {
            startActivity(Intent(this, target_list::class.java))
            finish()
        }
    }
}
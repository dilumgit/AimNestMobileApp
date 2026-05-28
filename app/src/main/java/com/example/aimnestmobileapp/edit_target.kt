package com.example.aimnestmobileapp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class edit_target : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_target)

        findViewById<View?>(R.id.main)?.let { root ->
            ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // Read incoming target data from target_details FIRST
        val incomingName = intent.getStringExtra("target_name").orEmpty()
        val incomingAmount = intent.getStringExtra("target_amount").orEmpty()
        val incomingDeadline = intent.getStringExtra("target_deadline").orEmpty()
        val incomingPriority = intent.getStringExtra("target_priority").orEmpty()

        // NOW set up back button with access to incoming variables
        findViewById<ImageView?>(R.id.backButton)?.setOnClickListener {
            val intent = Intent(this, target_details::class.java).apply {
                putExtra("target_name", incomingName)
                putExtra("target_amount", incomingAmount)
                putExtra("target_deadline", incomingDeadline)
                putExtra("target_priority", incomingPriority)
            }
            startActivity(intent)
            finish()
        }

        val targetNameInput = findViewById<EditText?>(R.id.targetNameInput)
        targetNameInput?.isEnabled = true
        targetNameInput?.isFocusable = true
        targetNameInput?.isFocusableInTouchMode = true
        if (incomingName.isNotBlank()) {
            targetNameInput?.setText(incomingName)
        }

        val targetAmountInput = findViewById<EditText?>(R.id.targetAmountInput)
        if (incomingAmount.isNotBlank()) {
            targetAmountInput?.setText(incomingAmount)
        }

        val deadlineInputText = findViewById<TextView?>(R.id.deadlineInputText)
        val calendarIcon = findViewById<ImageView?>(R.id.calendarIcon)
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        if (incomingDeadline.isNotBlank()) {
            deadlineInputText?.text = incomingDeadline
            runCatching {
                val parsed = formatter.parse(incomingDeadline)
                if (parsed != null) {
                    calendar.time = parsed
                }
            }
        } else if (deadlineInputText?.text.isNullOrBlank()) {
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

        val normalizedPriority = incomingPriority.lowercase(Locale.getDefault())
        val selectedIndex = when {
            normalizedPriority.contains("high") || normalizedPriority.contains("urgent") -> 0
            normalizedPriority.contains("medium") || normalizedPriority.contains("important") -> 1
            normalizedPriority.contains("low") || normalizedPriority.contains("optional") -> 2
            else -> 1
        }
        prioritySpinner?.setSelection(selectedIndex)

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
        }

        findViewById<MaterialButton?>(R.id.cancelButton)?.setOnClickListener {
            val intent = Intent(this, target_details::class.java).apply {
                putExtra("target_name", incomingName)
                putExtra("target_amount", incomingAmount)
                putExtra("target_deadline", incomingDeadline)
                putExtra("target_priority", incomingPriority)
            }
            startActivity(intent)
            finish()
        }

        findViewById<MaterialButton?>(R.id.updateTargetButton)?.setOnClickListener {
            val updatedName = targetNameInput?.text?.toString().orEmpty()
            val updatedAmount = targetAmountInput?.text?.toString().orEmpty()
            val updatedDeadline = deadlineInputText?.text?.toString().orEmpty()
            val updatedPriority = prioritySpinner?.selectedItem?.toString().orEmpty()

            // Save updated target to storage
            val prefs = getSharedPreferences("targets_prefs", MODE_PRIVATE)
            val json = prefs.getString("targets_json", "[]") ?: "[]"
            val targets = org.json.JSONArray(json)

            // Find and update the target in the list
            for (i in 0 until targets.length()) {
                val target = targets.getJSONObject(i)
                if (target.optString("name") == incomingName) {
                    target.put("name", updatedName)
                    target.put("amount", updatedAmount)
                    target.put("deadline", updatedDeadline)
                    target.put("priority", updatedPriority)
                    break
                }
            }

            prefs.edit().putString("targets_json", targets.toString()).apply()

            // Navigate back to target_details with updated values
            val intent = Intent(this, target_details::class.java).apply {
                putExtra("target_name", updatedName)
                putExtra("target_amount", updatedAmount)
                putExtra("target_deadline", updatedDeadline)
                putExtra("target_priority", updatedPriority)
            }
            startActivity(intent)
            finish()
        }
    }
}
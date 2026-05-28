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
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class edit_income : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_income)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<View>(R.id.backButton).setOnClickListener { finish() }
        findViewById<View>(R.id.cancelButton).setOnClickListener { finish() }

        val incomeIndex = intent.getIntExtra("income_index", -1)
        val title = intent.getStringExtra("income_title").orEmpty().trim()
        val amount = intent.getStringExtra("income_amount").orEmpty()
        val date = intent.getStringExtra("income_date").orEmpty()
        val notes = intent.getStringExtra("income_notes").orEmpty()

        val incomeTypeOptions = listOf(
            "Salary",
            "Freelance",
            "Business",
            "Investments",
            "Rental Income",
            "Bonus",
            "Gift",
            "Other"
        )
        val spIncomeType = findViewById<Spinner>(R.id.spIncomeType)
        val incomeTypeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            incomeTypeOptions
        )
        incomeTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spIncomeType.adapter = incomeTypeAdapter

        val selectedIndex = incomeTypeOptions.indexOfFirst { it.equals(title, ignoreCase = true) }
            .takeIf { it >= 0 } ?: 0
        spIncomeType.setSelection(selectedIndex, false)

        val amountInput = findViewById<EditText>(R.id.amountInput)
        amountInput.setText(amount)
        val dateInputText = findViewById<TextView>(R.id.dateInputText)
        dateInputText.text = date
        val notesInput = findViewById<EditText>(R.id.notesInput)
        notesInput.setText(notes)

        val calendarIcon = findViewById<ImageView>(R.id.calendarIcon)
        val dateFieldContainer = findViewById<LinearLayout>(R.id.dateFieldContainer)
        val calendar = Calendar.getInstance()

        // Initialize picker with existing date when possible.
        if (date.isNotBlank()) {
            runCatching {
                val parsed = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(date)
                if (parsed != null) calendar.time = parsed
            }
        }

        val openDatePicker = {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(calendar.time)
                    dateInputText.text = formattedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        calendarIcon.setOnClickListener { openDatePicker() }
        dateFieldContainer.setOnClickListener { openDatePicker() }
        dateInputText.setOnClickListener { openDatePicker() }

        findViewById<View>(R.id.updateIncomeButton).setOnClickListener {
            val updatedTitle = spIncomeType.selectedItem?.toString()?.trim().orEmpty()
            val updatedAmount = amountInput.text?.toString()?.trim().orEmpty()
            val updatedDate = dateInputText.text?.toString()?.trim().orEmpty()
            val updatedNotes = notesInput.text?.toString()?.trim().orEmpty()

            if (incomeIndex >= 0) {
                val prefs = getSharedPreferences("aimnest_income_data", MODE_PRIVATE)
                val json = prefs.getString("saved_incomes", "[]") ?: "[]"
                val array = JSONArray(json)

                if (incomeIndex < array.length()) {
                    val updatedObj = JSONObject().apply {
                        put("title", updatedTitle)
                        put("amount", updatedAmount)
                        put("date", updatedDate)
                        put("notes", updatedNotes)
                    }
                    array.put(incomeIndex, updatedObj)
                    prefs.edit().putString("saved_incomes", array.toString()).apply()
                }
            }

            startActivity(Intent(this, income_list::class.java))
            finish()
        }
    }
}
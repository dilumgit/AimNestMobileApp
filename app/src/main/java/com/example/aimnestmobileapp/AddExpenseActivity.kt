package com.example.aimnestmobileapp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddExpenseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_expense)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val categorySpinner = findViewById<Spinner>(R.id.categorySpinner)
        val categories = listOf(
            "Rent", "Utilities", "Food", "Transport", "Loan", "Groceries",
            "Entertainment", "Medical", "Education", "Other"
        )
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        val dateInputText = findViewById<TextView>(R.id.dateInputText)
        val calendarIcon = findViewById<ImageView>(R.id.calendarIcon)

        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        if (dateInputText.text.isNullOrBlank()) {
            dateInputText.text = formatter.format(calendar.time)
        }

        val openDatePicker = {
            val dialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    dateInputText.text = formatter.format(calendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dialog.show()
        }

        dateInputText.setOnClickListener { openDatePicker() }
        calendarIcon?.setOnClickListener { openDatePicker() }

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton?.setOnClickListener {
            startActivity(Intent(this, ExpensesListActivity::class.java))
            finish()
        }

        val saveButton = findViewById<MaterialButton>(R.id.saveExpenseButton)
        saveButton?.setOnClickListener {
            saveExpense()
        }

        val cancelButton = findViewById<MaterialButton>(R.id.cancelExpenseButton)
        cancelButton?.setOnClickListener {
            startActivity(Intent(this, ExpensesListActivity::class.java))
            finish()
        }

        val menuItemManageIncome = findViewById<LinearLayout>(R.id.menuItemManageIncome)
        menuItemManageIncome?.setOnClickListener {
            startActivity(Intent(this, income_list::class.java))
        }

        val menuItemManageExpenses = findViewById<LinearLayout>(R.id.menuItemManageExpenses)
        menuItemManageExpenses?.setOnClickListener {
            startActivity(Intent(this, ExpensesListActivity::class.java))
            finish()
        }

        val menuItemManageTarget = findViewById<LinearLayout>(R.id.menuItemManageTarget)
        menuItemManageTarget?.setOnClickListener {
            startActivity(Intent(this, target_list::class.java))
        }

        // Add View Analysis footer navigation
        val menuItemViewAnalysis = findViewById<LinearLayout>(R.id.menuItemViewAnalysis)
        menuItemViewAnalysis?.setOnClickListener {
            startActivity(Intent(this, AnalysisActivity::class.java))
            finish()
        }
    }

    private fun saveExpense() {
        val nameInput = findViewById<EditText>(R.id.expenseNameInput)
        val amountInput = findViewById<EditText>(R.id.amountInput)
        val categorySpinner = findViewById<Spinner>(R.id.categorySpinner)
        val mandatorySwitch = findViewById<SwitchCompat>(R.id.mandatorySwitch)
        val dateInput = findViewById<TextView>(R.id.dateInputText)

        val name = nameInput?.text.toString().trim()
        val amount = amountInput?.text.toString().trim()
        val category = categorySpinner?.selectedItem.toString()
        val isMandatory = mandatorySwitch?.isChecked ?: false
        val date = dateInput?.text.toString()

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter expense name", Toast.LENGTH_SHORT).show()
            return
        }

        if (amount.isEmpty()) {
            Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("aimnest_expense_data", MODE_PRIVATE)
        val json = prefs.getString("saved_expenses", "[]") ?: "[]"
        val array = JSONArray(json)

        val newExpense = JSONObject().apply {
            put("name", name)
            put("amount", amount)
            put("category", category)
            put("isMandatory", isMandatory)
            put("date", date)
        }

        array.put(newExpense)
        prefs.edit().putString("saved_expenses", array.toString()).apply()

        Toast.makeText(this, "Expense saved successfully", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, ExpensesListActivity::class.java))
        finish()
    }
}
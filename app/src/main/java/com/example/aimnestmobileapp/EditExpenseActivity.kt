package com.example.aimnestmobileapp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditExpenseActivity : AppCompatActivity() {

    private lateinit var categorySpinner: Spinner
    private lateinit var dateInputText: TextView
    private lateinit var expenseNameInput: EditText
    private lateinit var amountInput: EditText
    private lateinit var mandatorySwitch: SwitchCompat
    private var expenseIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_expense)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        expenseIndex = intent.getIntExtra("expense_index", -1)

        categorySpinner = findViewById(R.id.categorySpinner)
        expenseNameInput = findViewById(R.id.expenseNameInput)
        amountInput = findViewById(R.id.amountInput)
        mandatorySwitch = findViewById(R.id.mandatorySwitch)
        dateInputText = findViewById(R.id.dateInputText)
        val calendarIcon = findViewById<ImageView>(R.id.calendarIcon)

        val categories = listOf(
            "Rent", "Utilities", "Food", "Transport", "Loan", "Groceries",
            "Entertainment", "Medical", "Education", "Other"
        )
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        loadExpenseIntoForm(categories)

        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

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
        calendarIcon.setOnClickListener { openDatePicker() }

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.updateExpenseButton).setOnClickListener {
            saveUpdatedExpense()
        }

        findViewById<MaterialButton>(R.id.cancelButton).setOnClickListener {
            finish()
        }
    }

    private fun loadExpenseIntoForm(categories: List<String>) {
        if (expenseIndex < 0) return

        val prefs = getSharedPreferences("aimnest_expense_data", MODE_PRIVATE)
        val array = JSONArray(prefs.getString("saved_expenses", "[]") ?: "[]")
        if (expenseIndex !in 0 until array.length()) return

        val expense = array.getJSONObject(expenseIndex)
        expenseNameInput.setText(expense.optString("name", ""))
        amountInput.setText(expense.optString("amount", ""))
        dateInputText.text = expense.optString("date", "04/01/2026")
        mandatorySwitch.isChecked = expense.optBoolean("isMandatory", false)

        val category = expense.optString("category", "Other")
        val position = categories.indexOf(category)
        if (position >= 0) categorySpinner.setSelection(position)
    }

    private fun saveUpdatedExpense() {
        if (expenseIndex < 0) {
            Toast.makeText(this, "Unable to update expense", Toast.LENGTH_SHORT).show()
            return
        }

        val name = expenseNameInput.text.toString().trim()
        val amount = amountInput.text.toString().trim()
        val category = categorySpinner.selectedItem?.toString().orEmpty()
        val isMandatory = mandatorySwitch.isChecked
        val date = dateInputText.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter expense name", Toast.LENGTH_SHORT).show()
            return
        }
        if (amount.isEmpty()) {
            Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("aimnest_expense_data", MODE_PRIVATE)
        val array = JSONArray(prefs.getString("saved_expenses", "[]") ?: "[]")
        if (expenseIndex !in 0 until array.length()) return

        val updatedExpense = array.getJSONObject(expenseIndex)
        updatedExpense.put("name", name)
        updatedExpense.put("amount", amount)
        updatedExpense.put("category", category)
        updatedExpense.put("isMandatory", isMandatory)
        updatedExpense.put("date", date)

        prefs.edit {
            putString("saved_expenses", array.toString())
        }

        Toast.makeText(this, "Expense updated successfully", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK, Intent().putExtra("expense_index", expenseIndex))
        finish()
    }
}
package com.example.aimnestmobileapp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddIncomeActivity : AppCompatActivity() {

    data class IncomeItem(
        val title: String,
        val amount: String,
        val date: String,
        val notes: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_income)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val spIncomeType = findViewById<Spinner>(R.id.spIncomeType)
        val etAmount = findViewById<EditText>(R.id.etAmount)
        val etDateReceived = findViewById<EditText>(R.id.etDateReceived)
        val etNotes = findViewById<EditText>(R.id.etNotes)
        val btnSaveIncome = findViewById<Button>(R.id.btnSaveIncome)
        val btnCancelIncome = findViewById<Button>(R.id.btnCancelIncome)
        val backButton = findViewById<ImageView>(R.id.backButton)

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
        val incomeTypeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            incomeTypeOptions
        )
        incomeTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spIncomeType.adapter = incomeTypeAdapter
        spIncomeType.setSelection(0)

        val ivCalendar = findViewById<ImageView>(R.id.ivCalendar)
        val dateFieldContainer = findViewById<LinearLayout>(R.id.dateFieldContainer)
        val calendar = Calendar.getInstance()

        val openDatePicker = {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(calendar.time)
                    etDateReceived.setText(formattedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        ivCalendar.setOnClickListener { openDatePicker() }
        dateFieldContainer.setOnClickListener { openDatePicker() }
        etDateReceived.setOnClickListener { openDatePicker() }

        fun goToIncomeList() {
            startActivity(Intent(this, income_list::class.java))
            finish()
        }

        backButton.setOnClickListener { goToIncomeList() }
        btnCancelIncome.setOnClickListener { goToIncomeList() }

        // Footer navigation
        findViewById<LinearLayout>(R.id.menuItemManageIncome).setOnClickListener {
            startActivity(Intent(this, income_list::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.menuItemManageExpenses).setOnClickListener {
            startActivity(Intent(this, ExpensesListActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.menuItemManageTarget).setOnClickListener {
            startActivity(Intent(this, target_list::class.java))
            finish()
        }

        // Add View Analysis footer navigation
        findViewById<LinearLayout>(R.id.menuItemViewAnalysis).setOnClickListener {
            startActivity(Intent(this, AnalysisActivity::class.java))
            finish()
        }

        btnSaveIncome.setOnClickListener {
            val title = spIncomeType.selectedItem?.toString()?.trim().orEmpty()
            val amountRaw = etAmount.text.toString().trim()
            val date = etDateReceived.text.toString().trim()
            val notes = etNotes.text.toString().trim()

            if (title.isEmpty() || amountRaw.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val item = IncomeItem(
                title = title,
                amount = amountRaw,
                date = date,
                notes = notes
            )
            saveIncomeItem(item)
            Toast.makeText(this, "Income saved", Toast.LENGTH_SHORT).show()
            goToIncomeList()
        }
    }

    private fun saveIncomeItem(item: IncomeItem) {
        val prefs = getSharedPreferences("aimnest_income_data", MODE_PRIVATE)
        val existing = prefs.getString("saved_incomes", "[]") ?: "[]"
        val array = JSONArray(existing)
        val obj = JSONObject().apply {
            put("title", item.title)
            put("amount", item.amount)
            put("date", item.date)
            put("notes", item.notes)
        }
        array.put(obj)
        prefs.edit().putString("saved_incomes", array.toString()).apply()
    }
}
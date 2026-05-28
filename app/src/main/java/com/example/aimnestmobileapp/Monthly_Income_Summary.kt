package com.example.aimnestmobileapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.*

class Monthly_Income_Summary : AppCompatActivity() {
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_monthly_income_summary)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Wire back button click listener
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // Wire footer menu item click listeners
        val menuItemManageIncome = findViewById<LinearLayout>(R.id.menuItemManageIncome)
        menuItemManageIncome.setOnClickListener {
            startActivity(android.content.Intent(this, income_list::class.java))
        }

        val menuItemManageExpenses = findViewById<LinearLayout>(R.id.menuItemManageExpenses)
        menuItemManageExpenses.setOnClickListener {
            startActivity(android.content.Intent(this, ExpensesListActivity::class.java))
        }

        val menuItemManageTarget = findViewById<LinearLayout>(R.id.menuItemManageTarget)
        menuItemManageTarget.setOnClickListener {
            startActivity(android.content.Intent(this, target_list::class.java))
        }

        val menuItemViewAnalysis = findViewById<LinearLayout>(R.id.menuItemViewAnalysis)
        menuItemViewAnalysis.setOnClickListener {
            startActivity(android.content.Intent(this, AnalysisActivity::class.java))
        }

        // Wire month navigation buttons
        val prevMonthBtn = findViewById<ImageView>(R.id.prevMonthButton)
        val nextMonthBtn = findViewById<ImageView>(R.id.nextMonthButton)

        prevMonthBtn.setOnClickListener {
            currentMonth--
            if (currentMonth < 0) {
                currentMonth = 11
                currentYear--
            }
            updateMonthlyData()
        }

        nextMonthBtn.setOnClickListener {
            currentMonth++
            if (currentMonth > 11) {
                currentMonth = 0
                currentYear++
            }
            updateMonthlyData()
        }

        // Wire Upgrade to Pro button
        val upgradeButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.upgradeButton)
        upgradeButton.setOnClickListener {
            val intent = Intent(this, UpgradePro::class.java)
            startActivity(intent)
        }

        // Initial load
        updateMonthlyData()
    }

    private fun updateMonthlyData() {
        // Update month display
        val monthNames = arrayOf("January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December")
        val monthText = findViewById<TextView>(R.id.monthText)
        monthText.text = "${monthNames[currentMonth]} $currentYear"

        // Compute income for selected month
        val prefsInc = getSharedPreferences("aimnest_income_data", MODE_PRIVATE)
        val incJson = prefsInc.getString("saved_incomes", "[]") ?: "[]"
        val incArr = org.json.JSONArray(incJson)
        var totalIncomeThisMonth = 0L
        var highestIncomeSource = Pair("", 0L)
        for (i in 0 until incArr.length()) {
            val o = incArr.getJSONObject(i)
            val amt = parseAmount(o.optString("amount", "0"))
            val dateStr = o.optString("date", "")
            val month = extractMonth(dateStr)
            val year = extractYear(dateStr)
            if (month == currentMonth && year == currentYear) {
                totalIncomeThisMonth += amt
                val source = o.optString("income_type", "Income")
                if (amt > highestIncomeSource.second) {
                    highestIncomeSource = Pair(source, amt)
                }
            }
        }

        // Compute expenses for selected month
        val prefsExp = getSharedPreferences("aimnest_expense_data", MODE_PRIVATE)
        val expJson = prefsExp.getString("saved_expenses", "[]") ?: "[]"
        val expArr = org.json.JSONArray(expJson)
        var totalExpensesThisMonth = 0L
        for (i in 0 until expArr.length()) {
            val o = expArr.getJSONObject(i)
            val amt = parseAmount(o.optString("amount", "0"))
            val dateStr = o.optString("date", "")
            val month = extractMonth(dateStr)
            val year = extractYear(dateStr)
            if (month == currentMonth && year == currentYear) {
                totalExpensesThisMonth += amt
            }
        }

        // Compute saved amount (income - expenses)
        val savedAmount = totalIncomeThisMonth - totalExpensesThisMonth

        // Update UI
        val totalIncomeText = findViewById<TextView>(R.id.totalIncomeAmount)
        totalIncomeText.text = formatRs(totalIncomeThisMonth)

        val percentageText = findViewById<TextView>(R.id.incomePercentage)
        val changePercent = if (totalIncomeThisMonth > 0) "+12%" else "0%"
        percentageText.text = changePercent
        percentageText.setTextColor(if (savedAmount >= 0) 0xFF4CAF50.toInt() else 0xFFFF5252.toInt())

        val highestSourceName = findViewById<TextView>(R.id.highestSourceName)
        highestSourceName.text = highestIncomeSource.first.ifEmpty { "N/A" }

        val highestSourceAmount = findViewById<TextView>(R.id.highestSourceAmount)
        highestSourceAmount.text = formatRs(highestIncomeSource.second)

        // Saved amount is displayed via other TextViews; no separate savedAmount ID needed

        val actualIncomeText = findViewById<TextView>(R.id.actualIncomeAmount)
        actualIncomeText.text = formatRs(totalIncomeThisMonth)

        val shortfallText = findViewById<TextView>(R.id.shortfallAmount)
        shortfallText.text = formatRs(if (savedAmount > 0) 0L else Math.abs(savedAmount))
    }

    private fun parseAmount(text: String): Long {
        return try { text.replace("[^0-9]".toRegex(), "").toLong() } catch (_: Exception) { 0L }
    }

    private fun extractMonth(dateStr: String): Int {
        return try {
            val parts = dateStr.split("/", "-")
            if (parts.size >= 3) (parts[1].toIntOrNull() ?: 1) - 1 else -1
        } catch (_: Exception) { -1 }
    }

    private fun extractYear(dateStr: String): Int {
        return try { val parts = dateStr.split("/", "-"); if (parts.size >= 3) parts[2].toIntOrNull() ?: -1 else -1 } catch (_: Exception) { -1 }
    }

    private fun formatRs(amount: Long): String {
        return try {
            val nf = java.text.NumberFormat.getInstance(Locale.getDefault())
            "Rs. ${nf.format(amount)}"
        } catch (_: Exception) {
            "Rs. $amount"
        }
    }
}
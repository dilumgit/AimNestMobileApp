package com.example.aimnestmobileapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AlertsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alerts)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Back button click listener
        val backButton = findViewById<View>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // Footer menu item click listeners
        val menuItemManageIncome = findViewById<View>(R.id.menuItemManageIncome)
        menuItemManageIncome.setOnClickListener {
            startActivity(Intent(this, income_list::class.java))
            finish()
        }

        val menuItemManageExpenses = findViewById<View>(R.id.menuItemManageExpenses)
        menuItemManageExpenses.setOnClickListener {
            startActivity(Intent(this, ExpensesListActivity::class.java))
            finish()
        }

        val menuItemManageTarget = findViewById<View>(R.id.menuItemManageTarget)
        menuItemManageTarget.setOnClickListener {
            startActivity(Intent(this, target_list::class.java))
            finish()
        }

        val menuItemViewAnalysis = findViewById<View>(R.id.menuItemViewAnalysis)
        menuItemViewAnalysis.setOnClickListener {
            startActivity(Intent(this, AnalysisActivity::class.java))
            finish()
        }

        // After current setup, evaluate alerts
        evaluateAndRenderAlerts()
    }

    private fun evaluateAndRenderAlerts() {
        val alertsContainer = findViewById<LinearLayout>(R.id.alertsContainer)
        alertsContainer.removeAllViews()

        // Read incomes and expenses from shared preferences
        val incomes = readSavedIncomes()
        val expenses = readSavedExpenses()

        val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

        var totalIncomeForMonth = 0L
        incomes.forEach { inc ->
            val (amt, month, year) = parseAmountAndMonthYear(inc.amount, inc.date)
            if (month == currentMonth && year == currentYear) totalIncomeForMonth += amt
        }

        var totalExpensesForMonth = 0L
        var expenseCountThisMonth = 0
        val upcomingExpenses = mutableListOf<org.json.JSONObject>()
        expenses.forEach { exp ->
            val dateStr = exp.optString("date", "")
            val amtStr = exp.optString("amount", "0")
            val (amt, month, year) = parseAmountAndMonthYear(amtStr, dateStr)
            if (month == currentMonth && year == currentYear) {
                totalExpensesForMonth += amt
                expenseCountThisMonth++
            }
            // check if within next 7 days
            if (isDateWithinNextDays(dateStr, 7)) upcomingExpenses.add(exp)
        }

        // Read configuration thresholds from SharedPreferences
        val prefs = getSharedPreferences("aimnest_settings", MODE_PRIVATE)
        val lowIncomeThreshold = prefs.getLong("low_income_threshold", 50000L)
        val expenseRatioThreshold = prefs.getFloat("expense_ratio_threshold", 0.7f)
        val expenseCountThreshold = prefs.getInt("expense_count_threshold", 12)

        // Low income alert
        if (totalIncomeForMonth < lowIncomeThreshold) {
            val shortfall = (lowIncomeThreshold - totalIncomeForMonth)
            alertsContainer.addView(createAlertCard("Medium", "Income Shortfall", "Your total income for this month is Rs $totalIncomeForMonth. Estimated shortfall: Rs $shortfall", "#FFE5D3"))
        }

        // Expense overload alert
        if (totalExpensesForMonth > (totalIncomeForMonth * expenseRatioThreshold).toLong() || expenseCountThisMonth > expenseCountThreshold) {
            alertsContainer.addView(createAlertCard("Low", "Expense Overload", "You have high expenses this month: Rs $totalExpensesForMonth", "#FFF3CD"))
        }

        // Upcoming payments
        if (upcomingExpenses.isNotEmpty()) {
            alertsContainer.addView(createAlertCard("High", "Upcoming Payment Risks", "You have ${upcomingExpenses.size} upcoming payments within 7 days.", "#FFCDD2"))
        }

        // If no alerts, show an informational card
        if (alertsContainer.childCount == 0) {
            alertsContainer.addView(createAlertCard("Info", "No Alerts", "You're all caught up for this month.", "#E6F4EA"))
        }
    }

    private data class IncItem(val title: String, val amount: String, val date: String)
    private fun readSavedIncomes(): List<IncItem> {
        val prefs = getSharedPreferences("aimnest_income_data", MODE_PRIVATE)
        val json = prefs.getString("saved_incomes", "[]") ?: "[]"
        val arr = org.json.JSONArray(json)
        val list = mutableListOf<IncItem>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(IncItem(o.optString("title", ""), o.optString("amount", "0"), o.optString("date", "")))
        }
        return list
    }

    private fun readSavedExpenses(): List<org.json.JSONObject> {
        val prefs = getSharedPreferences("aimnest_expense_data", MODE_PRIVATE)
        val json = prefs.getString("saved_expenses", "[]") ?: "[]"
        val arr = org.json.JSONArray(json)
        val list = mutableListOf<org.json.JSONObject>()
        for (i in 0 until arr.length()) list.add(arr.getJSONObject(i))
        return list
    }

    private fun parseAmountAndMonthYear(amountStr: String, dateStr: String): Triple<Long, Int, Int> {
        val amt = try { amountStr.replace("[^0-9]".toRegex(), "").toLong() } catch (_: Exception) { 0L }
        var month = -1
        var year = -1
        try {
            if (dateStr.isNotBlank()) {
                // expecting format dd/MM/yyyy or dd-MM-yyyy
                val parts = dateStr.split("/", "-")
                if (parts.size >= 3) {
                    month = (parts[1].toIntOrNull() ?: 1) - 1
                    year = parts[2].toIntOrNull() ?: java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                }
            }
        } catch (_: Exception) {
        }
        return Triple(amt, month, year)
    }

    private fun isDateWithinNextDays(dateStr: String, days: Int): Boolean {
        try {
            if (dateStr.isBlank()) return false
            val fmt = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val d = fmt.parse(dateStr) ?: return false
            val now = java.util.Date()
            val diff = d.time - now.time
            val daysDiff = diff / (1000 * 60 * 60 * 24)
            return daysDiff in 0..days
        } catch (_: Exception) { return false }
    }

    private fun createAlertCard(level: String, title: String, description: String, headerColor: String): androidx.cardview.widget.CardView {
        val card = androidx.cardview.widget.CardView(this).apply {
            radius = dp(12).toFloat()
            cardElevation = dp(4).toFloat()
            setCardBackgroundColor(android.graphics.Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(12)
            }
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(android.graphics.Color.parseColor(headerColor))
            setPadding(dp(12), dp(12), dp(12), dp(12))
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val icon = ImageView(this).apply {
            setImageResource(R.drawable.alert1)
            layoutParams = LinearLayout.LayoutParams(dp(28), dp(28)).apply { marginEnd = dp(12) }
        }

        val badge = TextView(this).apply {
            text = level
            textSize = 10f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(dp(8), dp(4), dp(8), dp(4))
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 14f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#333333"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        header.addView(icon)
        header.addView(badge)
        header.addView(titleView)

        val desc = TextView(this).apply {
            text = description
            textSize = 12f
            setTextColor(android.graphics.Color.parseColor("#666666"))
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }

        container.addView(header)
        container.addView(desc)
        card.addView(container)
        return card
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
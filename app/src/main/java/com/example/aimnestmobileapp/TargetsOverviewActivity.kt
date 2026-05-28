package com.example.aimnestmobileapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TargetsOverviewActivity : AppCompatActivity() {

    private val TAG = "TargetsOverviewActivity"
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var totalIncomeCard: TextView
    private lateinit var totalExpensesCard: TextView
    private lateinit var progressPercentageText: TextView
    private lateinit var totalTargetValue: TextView
    private lateinit var totalSavedValue: TextView
    private lateinit var remainingValue: TextView
    private lateinit var onTrackCount: TextView
    private lateinit var delayedCount: TextView
    private lateinit var completedCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        try {
            setContentView(R.layout.activity_targets_overview)

            sharedPreferences = getSharedPreferences("targets_prefs", MODE_PRIVATE)

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            findViewById<ImageView>(R.id.backButton).setOnClickListener {
                startActivity(Intent(this, Dashboard::class.java))
                finish()
            }

            totalIncomeCard = findViewById(R.id.totalIncomeValue)
            totalExpensesCard = findViewById(R.id.totalExpensesValue)
            progressPercentageText = findViewById(R.id.progressPercentageText)
            totalTargetValue = findViewById(R.id.totalTargetValue)
            totalSavedValue = findViewById(R.id.totalSavedValue)
            remainingValue = findViewById(R.id.remainingValue)
            onTrackCount = findViewById(R.id.onTrackCount)
            delayedCount = findViewById(R.id.delayedCount)
            completedCount = findViewById(R.id.completedCount)

            refreshOverview()
            setupFooterNavigation()
        } catch (ex: Exception) {
            Log.e(TAG, "Error initializing TargetsOverviewActivity", ex)
            Toast.makeText(this, "Unable to open Targets Overview (see log)", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshOverview()
    }

    private fun refreshOverview() {
        updateTargetsOverviewData()
        updateIncomeExpenseDisplay()
    }

    private fun updateTargetsOverviewData() {
        val json = sharedPreferences.getString("targets_json", "[]") ?: "[]"
        val targets = JSONArray(json)

        var totalTargetAmount = 0
        var totalSavedAmount = 0
        var onTrack = 0
        var delayed = 0
        var completed = 0

        for (i in 0 until targets.length()) {
            val target = targets.getJSONObject(i)
            val amountRaw = target.optString("amount", "0")
            val deadlineRaw = target.optString("deadline", "")
            val setDateRaw = target.optString("setDate", getTodayDateString())

            val totalTarget = parseAmount(amountRaw)
            val requiredMonths = calculateRequiredMonths(deadlineRaw)
            val completedMonths = calculateFullyCompletedMonthsFromStart(setDateRaw)

            totalTargetAmount += totalTarget

            val requiredMonthly = if (requiredMonths > 0) kotlin.math.ceil(totalTarget.toDouble() / requiredMonths).toInt() else 0
            val savedAmount = (completedMonths * requiredMonthly).coerceAtMost(totalTarget)
            totalSavedAmount += savedAmount

            val remaining = (totalTarget - savedAmount).coerceAtLeast(0)
            if (remaining == 0) {
                completed++
            } else if (completedMonths >= requiredMonths && requiredMonths > 0) {
                delayed++
            } else {
                onTrack++
            }
        }

        val totalRemaining = (totalTargetAmount - totalSavedAmount).coerceAtLeast(0)
        val progress = if (totalTargetAmount > 0) ((totalSavedAmount.toDouble() / totalTargetAmount.toDouble()) * 100).toInt() else 0

        progressPercentageText.text = "$progress%"
        totalTargetValue.text = "Rs ${formatInteger(totalTargetAmount)}"
        totalSavedValue.text = "Rs ${formatInteger(totalSavedAmount)}"
        remainingValue.text = "Rs ${formatInteger(totalRemaining)}"
        onTrackCount.text = onTrack.toString()
        delayedCount.text = delayed.toString()
        completedCount.text = completed.toString()
    }

    private fun updateIncomeExpenseDisplay() {
        val monthlyIncome = calculateMonthlyIncomeTotal()
        val monthlyExpenses = calculateMonthlyExpensesTotal()

        totalIncomeCard.text = "Rs ${formatInteger(monthlyIncome)}"
        totalExpensesCard.text = "Rs ${formatInteger(monthlyExpenses)}"
    }

    private fun calculateMonthlyIncomeTotal(): Int {
        val prefs = getSharedPreferences("aimnest_income_data", MODE_PRIVATE)
        val json = prefs.getString("saved_incomes", "[]") ?: "[]"
        val incomes = JSONArray(json)

        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)
        val parser = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        var monthlyTotal = 0.0

        for (i in 0 until incomes.length()) {
            val income = incomes.getJSONObject(i)
            val dateString = income.optString("date", "")
            val amountString = income.optString("amount", "0")
            val parsedDate = runCatching { parser.parse(dateString) }.getOrNull() ?: continue
            val incomeCalendar = Calendar.getInstance().apply { time = parsedDate }
            if (incomeCalendar.get(Calendar.MONTH) != currentMonth || incomeCalendar.get(Calendar.YEAR) != currentYear) continue
            monthlyTotal += parseNumeric(amountString)
        }
        return monthlyTotal.toInt()
    }

    private fun calculateMonthlyExpensesTotal(): Int {
        val prefs = getSharedPreferences("aimnest_expense_data", MODE_PRIVATE)
        val json = prefs.getString("saved_expenses", "[]") ?: "[]"
        val expenses = JSONArray(json)

        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)
        val parser = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        var monthlyTotal = 0.0

        for (i in 0 until expenses.length()) {
            val expense = expenses.getJSONObject(i)
            val dateString = expense.optString("date", "")
            val amountString = expense.optString("amount", "0")
            val parsedDate = runCatching { parser.parse(dateString) }.getOrNull() ?: continue
            val expenseCalendar = Calendar.getInstance().apply { time = parsedDate }
            if (expenseCalendar.get(Calendar.MONTH) != currentMonth || expenseCalendar.get(Calendar.YEAR) != currentYear) continue
            monthlyTotal += parseNumeric(amountString)
        }
        return monthlyTotal.toInt()
    }

    private fun setupFooterNavigation() {
        val footerPanel = findViewById<LinearLayout>(R.id.bottomNavigationBar)
        if (footerPanel.childCount >= 4) {
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
    }

    private fun parseAmount(raw: String): Int = parseNumeric(raw).toInt()

    private fun parseNumeric(raw: String): Double {
        return raw.replace("Rs", "", ignoreCase = true)
            .replace("$", "")
            .replace(":", "")
            .replace(",", "")
            .trim()
            .toDoubleOrNull() ?: 0.0
    }

    private fun formatInteger(value: Int): String = value.toString()

    private fun calculateRequiredMonths(deadline: String): Int {
        if (deadline.isBlank()) return 0
        val parser = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val parsedDeadline = runCatching { parser.parse(deadline) }.getOrNull() ?: return 0
        val today = Calendar.getInstance()
        val end = Calendar.getInstance().apply { time = parsedDeadline }
        var months = (end.get(Calendar.YEAR) - today.get(Calendar.YEAR)) * 12 +
            (end.get(Calendar.MONTH) - today.get(Calendar.MONTH))
        if (end.get(Calendar.DAY_OF_MONTH) >= today.get(Calendar.DAY_OF_MONTH)) months += 1
        return months.coerceAtLeast(0)
    }

    private fun calculateFullyCompletedMonthsFromStart(setDateRaw: String): Int {
        if (setDateRaw.isBlank()) return 0
        val parser = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val parsedSetDate = runCatching { parser.parse(setDateRaw) }.getOrNull() ?: return 0
        val today = Calendar.getInstance()
        val start = Calendar.getInstance().apply { time = parsedSetDate }
        var completedMonths = (today.get(Calendar.YEAR) - start.get(Calendar.YEAR)) * 12 +
            (today.get(Calendar.MONTH) - start.get(Calendar.MONTH))
        if (today.get(Calendar.DAY_OF_MONTH) < start.get(Calendar.DAY_OF_MONTH)) {
            completedMonths = (completedMonths - 1).coerceAtLeast(0)
        }
        return completedMonths
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
    }
}

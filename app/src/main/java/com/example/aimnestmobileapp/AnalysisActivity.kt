package com.example.aimnestmobileapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONArray
import java.util.Locale

class AnalysisActivity : AppCompatActivity() {
    private val TAG = "AnalysisActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        try {
            setContentView(R.layout.activity_overall_analysis)

            val root = findViewById<android.view.View>(R.id.main)
            if (root != null) {
                ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                    insets
                }
            }

            findViewById<android.widget.ImageView?>(R.id.backButton)?.setOnClickListener {
                startActivity(Intent(this, Dashboard::class.java))
                finish()
            }

            setupBottomNavigation()
            refreshUI()
        } catch (ex: Exception) {
            Log.e(TAG, "Error inflating AnalysisActivity layout", ex)
            Toast.makeText(this, "Unable to open Analysis screen (see log)", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
    }

    private fun setupBottomNavigation() {
        val footerPanel = findViewById<LinearLayout?>(R.id.footerPanel)
        footerPanel?.let { panel ->
            if (panel.childCount >= 4) {
                panel.getChildAt(0)?.setOnClickListener {
                    startActivity(Intent(this, income_list::class.java))
                    finish()
                }
                panel.getChildAt(1)?.setOnClickListener {
                    startActivity(Intent(this, ExpensesListActivity::class.java))
                    finish()
                }
                panel.getChildAt(2)?.setOnClickListener {
                    startActivity(Intent(this, target_list::class.java))
                    finish()
                }
                panel.getChildAt(3)?.setOnClickListener {
                    // current screen; no-op
                }
            }
        }
    }

    private fun refreshUI() {
        try {
            val totalIncome = calculateAllTimeTotalFromPrefs("aimnest_income_data", "saved_incomes")
            val totalExpenses = calculateAllTimeTotalFromPrefs("aimnest_expense_data", "saved_expenses")
            val highestExpense = calculateHighestExpenseCategory()

            val fullSavings = totalIncome - totalExpenses
            val savingsRate = if (totalIncome > 0) ((fullSavings.toDouble() / totalIncome.toDouble()) * 100) else 0.0

            val totalIncomeView = findViewById<TextView>(R.id.totalIncomeValue)
            val totalExpensesView = findViewById<TextView>(R.id.totalExpensesValue)
            val netBalanceView = findViewById<TextView>(R.id.netBalanceValue)
            val savingsRateView = findViewById<TextView>(R.id.savingsRateValue)
            val highestCircle = findViewById<TextView>(R.id.highestExpenseCategoryText)
            val highestTitle = findViewById<TextView>(R.id.highestExpenseTitleText)
            val highestSubtitle = findViewById<TextView>(R.id.highestExpenseSubtitleText)
            val highestAmount = findViewById<TextView>(R.id.highestExpenseAmountText)
            val highestSecondAmount = findViewById<TextView>(R.id.highestExpenseSecondAmountText)

            totalIncomeView?.text = formatRs(totalIncome)
            totalExpensesView?.text = formatRs(totalExpenses)
            netBalanceView?.text = formatRs(fullSavings)
            savingsRateView?.text = formatPercent(savingsRate)

            val categoryLabel = highestExpense.category.ifBlank { "N/A" }
            highestCircle?.text = categoryLabel
            highestTitle?.text = highestExpenseTitle(categoryLabel)
            highestSubtitle?.text = highestExpenseSubtitle()
            highestAmount?.text = formatRs(highestExpense.amount)
            highestSecondAmount?.text = formatRs(highestExpense.amount)
        } catch (ex: Exception) {
            Log.e(TAG, "Error updating UI", ex)
        }
    }

    private fun calculateAllTimeTotalFromPrefs(prefName: String, key: String): Int {
        val prefs = getSharedPreferences(prefName, MODE_PRIVATE)
        val json = prefs.getString(key, "[]") ?: "[]"
        val array = JSONArray(json)
        var total = 0.0

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val amountStr = obj.optString("amount", "0")
            total += parseNumeric(amountStr)
        }
        return total.toInt()
    }

    private data class HighestExpense(val category: String, val amount: Int)

    private fun calculateHighestExpenseCategory(): HighestExpense {
        val prefs = getSharedPreferences("aimnest_expense_data", MODE_PRIVATE)
        val json = prefs.getString("saved_expenses", "[]") ?: "[]"
        val array = JSONArray(json)
        val totalsByCategory = linkedMapOf<String, Double>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val category = obj.optString("category", obj.optString("tag", obj.optString("type", "Other"))).trim().ifBlank { "Other" }
            val amount = parseNumeric(obj.optString("amount", "0"))
            totalsByCategory[category] = (totalsByCategory[category] ?: 0.0) + amount
        }

        val topEntry = totalsByCategory.maxByOrNull { it.value }
        return if (topEntry != null) HighestExpense(topEntry.key, topEntry.value.toInt()) else HighestExpense("N/A", 0)
    }

    private fun highestExpenseTitle(category: String): String = "$category is your highest"
    private fun highestExpenseSubtitle(): String = "long-term expense"

    private fun parseNumeric(raw: String): Double {
        return raw.replace("Rs", "", ignoreCase = true)
            .replace("$", "")
            .replace(":", "")
            .replace(",", "")
            .trim()
            .toDoubleOrNull() ?: 0.0
    }

    private fun formatInteger(value: Int): String = value.toString()
    private fun formatRs(value: Int): String = "Rs ${formatInteger(value)}"
    private fun formatPercent(value: Double): String = "${String.format(Locale.getDefault(), "%.1f", value)}%"
}

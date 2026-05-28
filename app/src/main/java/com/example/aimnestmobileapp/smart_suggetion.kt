package com.example.aimnestmobileapp

import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class smart_suggetion : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_smart_suggetion)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Populate recommendations dynamically
        populateRecommendations()

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
    }

    override fun onResume() {
        super.onResume()
        // Refresh suggestions and top card when returning to this screen
        populateRecommendations()
    }

    private fun populateRecommendations() {
        val container = findViewById<LinearLayout>(R.id.recommendationsRow)
        // Remove static children (we will add dynamic ones)
        container.removeAllViews()

        // Compute current month income and detect shortfall
        val prefsInc = getSharedPreferences("aimnest_income_data", MODE_PRIVATE)
        val incJson = prefsInc.getString("saved_incomes", "[]") ?: "[]"
        val incArr = org.json.JSONArray(incJson)
        val now = java.util.Calendar.getInstance()
        val cm = now.get(java.util.Calendar.MONTH)
        val cy = now.get(java.util.Calendar.YEAR)
        var totalIncomeThisMonth = 0L
        for (i in 0 until incArr.length()) {
            val o = incArr.getJSONObject(i)
            val amt = parseAmount(o.optString("amount", "0"))
            val dateStr = o.optString("date", "")
            val month = extractMonth(dateStr)
            val year = extractYear(dateStr)
            if (month == cm && year == cy) totalIncomeThisMonth += amt
        }

        // Compute current month expenses
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
            if (month == cm && year == cy) totalExpensesThisMonth += amt
        }

        // Compute total target savings (sum of required monthly for all targets)
        val prefsTgt = getSharedPreferences("aimnest_target_data", MODE_PRIVATE)
        val tgtJson = prefsTgt.getString("saved_targets", "[]") ?: "[]"
        val tgtArr = org.json.JSONArray(tgtJson)
        var totalTargetSavings = 0L
        for (i in 0 until tgtArr.length()) {
            val o = tgtArr.getJSONObject(i)
            val requiredMonthly = o.optLong("required_monthly", 0L)
            totalTargetSavings += requiredMonthly
        }

        val settings = getSharedPreferences("aimnest_settings", MODE_PRIVATE)
        val lowIncomeThreshold = settings.getLong("low_income_threshold", 50000L)

        // Calculate available to spend (income - expenses - target savings)
        val availableToSpend = totalIncomeThisMonth - totalExpensesThisMonth - totalTargetSavings

        // Update top suggestion card title/description based on dashboard metrics
        val topTitle = findViewById<TextView>(R.id.topSuggestionTitle)
        val topDesc = findViewById<TextView>(R.id.topSuggestionDesc)

        val suggestions = mutableListOf<Suggestion>()
        val todayCal = java.util.Calendar.getInstance()
        fun nextDate(daysAhead: Int): String {
            val c = todayCal.clone() as java.util.Calendar
            c.add(java.util.Calendar.DAY_OF_YEAR, daysAhead)
            val fmt = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            return fmt.format(c.time)
        }

        // Logic based on dashboard metrics
        when {
            totalIncomeThisMonth < lowIncomeThreshold -> {
                // Income shortfall - suggest income boosters
                val shortfall = lowIncomeThreshold - totalIncomeThisMonth
                topTitle.text = "Income Shortfall"
                topDesc.text = "You are short by ${formatRs(shortfall)} this month to meet your target. Here are ways to boost income:"

                val first = (shortfall * 50L + 99L) / 100L
                val second = (shortfall * 30L + 99L) / 100L
                val third = shortfall - first - second
                val s1 = if (first <= 0L) 1000L else first
                val s2 = if (second <= 0L) 500L else second
                val s3 = if (third <= 0L) 0L else third

                suggestions.add(Suggestion(R.drawable.smart2, "Freelance", formatRs(s1), nextDate(2), recommended = true))
                suggestions.add(Suggestion(R.drawable.smart3, "Part-time", formatRs(s2), nextDate(4)))
                if (s3 > 0L) suggestions.add(Suggestion(R.drawable.smart4, "Gig Work", formatRs(s3), nextDate(6)))
            }
            totalExpensesThisMonth > totalIncomeThisMonth * 80L / 100L -> {
                // High expense ratio - suggest expense reduction
                topTitle.text = "High Expenses"
                topDesc.text = "Your expenses are ${((totalExpensesThisMonth * 100L) / totalIncomeThisMonth)}% of income. Consider reducing discretionary spending."

                val reduction = totalExpensesThisMonth / 5L
                suggestions.add(Suggestion(R.drawable.smart2, "Cut Utilities", formatRs(reduction), nextDate(1), recommended = true))
                suggestions.add(Suggestion(R.drawable.smart3, "Reduce Transport", formatRs(reduction / 2), nextDate(3)))
                suggestions.add(Suggestion(R.drawable.smart4, "Save on Food", formatRs(reduction / 2), nextDate(5)))
            }
            availableToSpend <= 0L -> {
                // No available spending - suggest income increase or expense cut
                topTitle.text = "Budget Tight"
                topDesc.text = "Your income (${formatRs(totalIncomeThisMonth)}) minus expenses (${formatRs(totalExpensesThisMonth)}) and targets (${formatRs(totalTargetSavings)}) leaves no buffer."

                val needed = (totalExpensesThisMonth + totalTargetSavings - totalIncomeThisMonth) + 5000L
                suggestions.add(Suggestion(R.drawable.smart2, "Freelance", formatRs(needed / 2), nextDate(2), recommended = true))
                suggestions.add(Suggestion(R.drawable.smart3, "Cut Expenses", formatRs(needed / 3), nextDate(4)))
                suggestions.add(Suggestion(R.drawable.smart4, "Side Gig", formatRs(needed / 3), nextDate(6)))
            }
            availableToSpend in 1L..10000L -> {
                // Low buffer - suggest boost to savings
                topTitle.text = "Low Buffer"
                topDesc.text = "Your available to spend is only ${formatRs(availableToSpend)}. Aim to increase income or reduce expenses for better safety."

                val boost = 5000L
                suggestions.add(Suggestion(R.drawable.smart2, "Freelance", formatRs(boost), nextDate(2), recommended = true))
                suggestions.add(Suggestion(R.drawable.smart3, "Part-time", formatRs(boost / 2), nextDate(4)))
                suggestions.add(Suggestion(R.drawable.smart4, "Bonus Work", formatRs(boost / 2), nextDate(6)))
            }
            else -> {
                // On track - suggest savings or optional boost
                topTitle.text = "On Track"
                topDesc.text = "Great! Your available to spend is ${formatRs(availableToSpend)}. Consider extra income to boost emergency fund."

                suggestions.add(Suggestion(R.drawable.smart2, "Freelance", formatRs(3000L), nextDate(3)))
                suggestions.add(Suggestion(R.drawable.smart3, "Part-time", formatRs(2500L), nextDate(5)))
                suggestions.add(Suggestion(R.drawable.smart4, "Consulting", formatRs(6000L), nextDate(7), recommended = true))
            }
        }

        // Build card views and add
        suggestions.forEachIndexed { index, s ->
            val card = CardView(this).apply {
                radius = dp(12).toFloat()
                cardElevation = dp(3).toFloat()
                setCardBackgroundColor(android.graphics.Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(0, dp(180)).apply {
                    weight = 1f
                    if (index < 2) marginEnd = dp(8)
                }
            }

            val frame = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(12), dp(12), dp(12), dp(12))
            }

            val iv = ImageView(this).apply {
                setImageResource(s.imageRes)
                layoutParams = LinearLayout.LayoutParams(dp(60), dp(60))
            }
            val title = TextView(this).apply {
                text = s.title
                textSize = 14f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(android.graphics.Color.parseColor("#333333"))
                setPadding(0, dp(8), 0, 0)
            }
            val amt = TextView(this).apply {
                text = s.amount
                textSize = 13f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(android.graphics.Color.parseColor("#1976D2"))
                setPadding(0, dp(6), 0, 0)
            }
            val date = TextView(this).apply {
                text = s.date
                textSize = 11f
                setTextColor(android.graphics.Color.parseColor("#999999"))
                setPadding(0, dp(6), 0, 0)
            }
            frame.addView(iv)
            frame.addView(title)
            frame.addView(amt)
            frame.addView(date)

            // If recommended, overlay badge using FrameLayout behavior: create a small card
            if (s.recommended) {
                val containerFrame = android.widget.FrameLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                }
                // add the main content frame
                containerFrame.addView(frame)

                // create overlay badge
                val overlay = CardView(this).apply {
                    radius = dp(6).toFloat()
                    cardElevation = dp(2).toFloat()
                    setCardBackgroundColor(android.graphics.Color.parseColor("#FF9800"))
                    val badge = TextView(this@smart_suggetion).apply {
                        text = "Recommended"
                        textSize = 11f
                        setTextColor(android.graphics.Color.WHITE)
                        setPadding(dp(6), dp(4), dp(6), dp(4))
                    }
                    addView(badge)
                }

                val overlayLp = android.widget.FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.END).apply {
                    val m = dp(8)
                    setMargins(m, m, m, 0)
                }
                containerFrame.addView(overlay, overlayLp)
                card.addView(containerFrame)
            } else {
                card.addView(frame)
            }

            container.addView(card)
        }
    }

    private data class Suggestion(val imageRes: Int, val title: String, val amount: String, val date: String, val recommended: Boolean = false)

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

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun formatRs(amount: Long): String {
        return try {
            val nf = java.text.NumberFormat.getInstance(java.util.Locale.getDefault())
            "Rs : ${nf.format(amount)}"
        } catch (_: Exception) {
            "Rs : $amount"
        }
    }
}
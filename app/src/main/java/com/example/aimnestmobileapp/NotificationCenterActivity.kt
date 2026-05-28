package com.example.aimnestmobileapp

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NotificationCenterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification_center)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // populate dynamic notifications
        populateNotifications()

        // Back button behavior: go back to previous screen (finish)
        findViewById<View>(R.id.backButton)?.setOnClickListener {
            finish()
        }

        // Footer navigation
        findViewById<View>(R.id.menuItemManageIncome)?.setOnClickListener {
            startActivity(android.content.Intent(this, income_list::class.java))
        }
        findViewById<View>(R.id.menuItemManageExpenses)?.setOnClickListener {
            startActivity(android.content.Intent(this, ExpensesListActivity::class.java))
        }
        findViewById<View>(R.id.menuItemManageTarget)?.setOnClickListener {
            startActivity(android.content.Intent(this, target_list::class.java))
        }
        findViewById<View>(R.id.menuItemViewAnalysis)?.setOnClickListener {
            startActivity(android.content.Intent(this, AnalysisActivity::class.java))
            finish()
        }
    }

    private fun populateNotifications() {
        val container = findViewById<android.widget.LinearLayout>(R.id.notificationsContainer)
        container.removeAllViews()

        // Load incomes and expenses
        val prefsInc = getSharedPreferences("aimnest_income_data", MODE_PRIVATE)
        val incJson = prefsInc.getString("saved_incomes", "[]") ?: "[]"
        val incArr = org.json.JSONArray(incJson)

        // Recent incomes (show up to 3)
        val recentIncomes = mutableListOf<org.json.JSONObject>()
        for (i in 0 until incArr.length()) recentIncomes.add(incArr.getJSONObject(i))
        val recent = recentIncomes.reversed().take(3)
        recent.forEach { o ->
            val title = o.optString("title", "Income")
            val amount = o.optString("amount", "0")
            val date = o.optString("date", "")
            container.addView(createNotificationCard(R.drawable.notification1, "$title received: Rs $amount", date))
        }

        // Upcoming expense reminders (within 7 days)
        val prefsExp = getSharedPreferences("aimnest_expense_data", MODE_PRIVATE)
        val expJson = prefsExp.getString("saved_expenses", "[]") ?: "[]"
        val expArr = org.json.JSONArray(expJson)
        for (i in 0 until expArr.length()) {
            val e = expArr.getJSONObject(i)
            val dateStr = e.optString("date", "")
            if (isDateWithinNextDays(dateStr, 7)) {
                val name = e.optString("name", "Expense")
                container.addView(createNotificationCard(R.drawable.notification2, "Reminder: $name is due soon", dateStr))
            }
        }

        // Income shortfall note
        val totalThisMonth = calculateTotalIncomeForCurrentMonth()
        val settings = getSharedPreferences("aimnest_settings", MODE_PRIVATE)
        val lowIncomeThreshold = settings.getLong("low_income_threshold", 50000L)
        if (totalThisMonth < lowIncomeThreshold) {
            container.addView(createNotificationCard(R.drawable.notification3, "Income shortfall: your total income this month is Rs $totalThisMonth", ""))
        }

        // If container empty, show informational notification
        if (container.childCount == 0) {
            container.addView(createNotificationCard(R.drawable.notification4, "No notifications", "You're up to date"))
        }
    }

    private fun calculateTotalIncomeForCurrentMonth(): Long {
        val prefs = getSharedPreferences("aimnest_income_data", MODE_PRIVATE)
        val json = prefs.getString("saved_incomes", "[]") ?: "[]"
        val arr = org.json.JSONArray(json)
        val now = java.util.Calendar.getInstance()
        val cm = now.get(java.util.Calendar.MONTH)
        val cy = now.get(java.util.Calendar.YEAR)
        var total = 0L
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val amtStr = o.optString("amount", "0")
            val dateStr = o.optString("date", "")
            val (amt, month, year) = parseAmountAndMonthYear(amtStr, dateStr)
            if (month == cm && year == cy) total += amt
        }
        return total
    }

    private fun parseAmountAndMonthYear(amountStr: String, dateStr: String): Triple<Long, Int, Int> {
        val amt = try { amountStr.replace("[^0-9]".toRegex(), "").toLong() } catch (_: Exception) { 0L }
        var month = -1
        var year = -1
        try {
            if (dateStr.isNotBlank()) {
                val parts = dateStr.split("/", "-")
                if (parts.size >= 3) {
                    month = (parts[1].toIntOrNull() ?: 1) - 1
                    year = parts[2].toIntOrNull() ?: java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                }
            }
        } catch (_: Exception) {}
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

    private fun createNotificationCard(drawableId: Int, title: String, subtitle: String): androidx.cardview.widget.CardView {
        val card = androidx.cardview.widget.CardView(this).apply {
            radius = dp(12).toFloat()
            cardElevation = dp(4).toFloat()
            setCardBackgroundColor(android.graphics.Color.WHITE)
            layoutParams = android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(12) }
        }

        val row = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        val icon = android.widget.ImageView(this).apply {
            setImageResource(drawableId)
            layoutParams = android.widget.LinearLayout.LayoutParams(dp(32), dp(32)).apply { rightMargin = dp(12) }
        }

        val col = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvTitle = android.widget.TextView(this).apply {
            text = title
            textSize = 13f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#333333"))
        }

        val tvSub = android.widget.TextView(this).apply {
            text = subtitle
            textSize = 11f
            setTextColor(android.graphics.Color.parseColor("#999999"))
        }

        col.addView(tvTitle)
        col.addView(tvSub)
        row.addView(icon)
        row.addView(col)
        card.addView(row)
        return card
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
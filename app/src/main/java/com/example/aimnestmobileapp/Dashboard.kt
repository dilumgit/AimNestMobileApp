package com.example.aimnestmobileapp

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Dashboard : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var greetingText: TextView
    private lateinit var totalIncomeAmountText: TextView
    private lateinit var totalExpensesAmountText: TextView
    private lateinit var targetSavingsAmountText: TextView
    private lateinit var availableSpendAmountText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard2)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("ProfileData", MODE_PRIVATE)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get the greeting TextView
        greetingText = findViewById(R.id.greetingText)
        totalIncomeAmountText = findViewById(R.id.totalIncomeAmount)
        totalExpensesAmountText = findViewById(R.id.totalExpensesAmount)
        targetSavingsAmountText = findViewById(R.id.targetSavingsAmount)
        availableSpendAmountText = findViewById(R.id.availableSpendAmount)

        updateGreetingMessage()
        updateMonthlyIncomeTotal()
        updateMonthlyExpensesTotal()
        updateTargetSavingsTotal()
        updateAvailableToSpend()
        loadProfilePicture()

        // Set up menu icon click listener to navigate to SideMenuActivity
        val menuIcon = findViewById<ImageView>(R.id.menuIcon)
        menuIcon.setOnClickListener {
            val intent = Intent(this, SideMenuActivity::class.java)
            startActivity(intent)
        }

        // Set up profile picture click listener to navigate to ProfileOverviewActivity
        val profilePicture = findViewById<ImageView>(R.id.profilePicture)
        profilePicture.setOnClickListener {
            val intent = Intent(this, ProfileOverviewActivity::class.java)
            startActivity(intent)
        }

        // Set up Manage Income button click listener to navigate to income_list activity
        val manageIncomeButton = findViewById<LinearLayout>(R.id.menuItemManageIncome)
        manageIncomeButton.setOnClickListener {
            val intent = Intent(this, income_list::class.java)
            startActivity(intent)
        }

        // Set up Manage Expenses button click listener to navigate to ExpensesListActivity
        val manageExpensesButton = findViewById<LinearLayout>(R.id.menuItemManageExpenses)
        manageExpensesButton.setOnClickListener {
            val intent = Intent(this, ExpensesListActivity::class.java)
            startActivity(intent)
        }

        // Set up Manage Target button click listener to navigate to target_list activity
        val manageTargetButton = findViewById<LinearLayout>(R.id.menuItemManageTarget)
        manageTargetButton.setOnClickListener {
            val intent = Intent(this, target_list::class.java)
            startActivity(intent)
        }

        // Set up View Analysis button click listener to navigate to AnalysisActivity
        val viewAnalysisButton = findViewById<LinearLayout>(R.id.menuItemViewAnalysis)
        viewAnalysisButton.setOnClickListener {
            val intent = Intent(this, AnalysisActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            if (::greetingText.isInitialized) {
                updateGreetingMessage()
            }
            if (::totalIncomeAmountText.isInitialized) {
                updateMonthlyIncomeTotal()
            }
            if (::totalExpensesAmountText.isInitialized) {
                updateMonthlyExpensesTotal()
            }
            if (::targetSavingsAmountText.isInitialized) {
                updateTargetSavingsTotal()
            }
            if (::availableSpendAmountText.isInitialized) {
                updateAvailableToSpend()
            }
            loadProfilePicture()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateGreetingMessage() {
        val firstName = getSavedFirstName()

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val greeting = when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }

        greetingText.text = String.format(Locale.getDefault(), "%s, %s", greeting, firstName)
    }

    private fun updateMonthlyIncomeTotal() {
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

            val isCurrentMonth = incomeCalendar.get(Calendar.MONTH) == currentMonth &&
                incomeCalendar.get(Calendar.YEAR) == currentYear
            if (!isCurrentMonth) continue

            val normalizedAmount = amountString
                .replace("Rs", "", ignoreCase = true)
                .replace(":", "")
                .replace(",", "")
                .trim()
            val amount = normalizedAmount.toDoubleOrNull() ?: 0.0
            monthlyTotal += amount
        }

        totalIncomeAmountText.text = "${monthlyTotal.toInt()}"
    }

    private fun updateMonthlyExpensesTotal() {
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

            val isCurrentMonth = expenseCalendar.get(Calendar.MONTH) == currentMonth &&
                expenseCalendar.get(Calendar.YEAR) == currentYear
            if (!isCurrentMonth) continue

            val normalizedAmount = amountString
                .replace("Rs", "", ignoreCase = true)
                .replace(":", "")
                .replace(",", "")
                .trim()
            val amount = normalizedAmount.toDoubleOrNull() ?: 0.0
            monthlyTotal += amount
        }

        totalExpensesAmountText.text = "${monthlyTotal.toInt()}"
    }

    private fun updateTargetSavingsTotal() {
        val prefs = getSharedPreferences("targets_prefs", MODE_PRIVATE)
        val json = prefs.getString("targets_json", "[]") ?: "[]"
        val targets = JSONArray(json)

        var totalRequiredMonthly = 0
        for (i in 0 until targets.length()) {
            val target = targets.getJSONObject(i)
            val amountRaw = target.optString("amount", "0")
            val deadlineRaw = target.optString("deadline", "")

            val totalTarget = parseAmountInt(amountRaw)
            val requiredMonths = calculateMonthsUntilDeadline(deadlineRaw)
            val requiredMonthly = if (requiredMonths > 0) totalTarget / requiredMonths else 0

            totalRequiredMonthly += requiredMonthly
        }

        targetSavingsAmountText.text = totalRequiredMonthly.toString()
    }

    private fun updateAvailableToSpend() {
        val totalIncome = parseIntFromView(totalIncomeAmountText)
        val totalExpenses = parseIntFromView(totalExpensesAmountText)
        val targetSavings = parseIntFromView(targetSavingsAmountText)

        val available = totalIncome - (totalExpenses + targetSavings)
        availableSpendAmountText.text = available.toString()
    }

    private fun parseIntFromView(tv: TextView): Int {
        return tv.text.toString()
            .replace("Rs", "", ignoreCase = true)
            .replace(":", "")
            .replace(",", "")
            .trim()
            .toIntOrNull() ?: 0
    }

    private fun calculateMonthsUntilDeadline(deadline: String): Int {
        if (deadline.isBlank()) return 0

        val parser = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val parsedDeadline = runCatching { parser.parse(deadline) }.getOrNull() ?: return 0

        val today = Calendar.getInstance()
        val end = Calendar.getInstance().apply { time = parsedDeadline }

        var months = (end.get(Calendar.YEAR) - today.get(Calendar.YEAR)) * 12 +
            (end.get(Calendar.MONTH) - today.get(Calendar.MONTH))

        if (end.get(Calendar.DAY_OF_MONTH) > today.get(Calendar.DAY_OF_MONTH)) {
            months += 1
        }

        return months.coerceAtLeast(0)
    }

    private fun parseAmountInt(raw: String): Int {
        return raw
            .replace("Rs", "", ignoreCase = true)
            .replace(":", "")
            .replace(",", "")
            .trim()
            .toIntOrNull() ?: 0
    }

    private fun getSavedFirstName(): String {
        val storedFirstName = sharedPreferences.getString("firstName", null)?.trim().orEmpty()
        if (storedFirstName.isNotBlank()) {
            return storedFirstName
        }

        val fullName = sharedPreferences.getString("fullName", null)?.trim().orEmpty()
        return fullName
            .split("\\s+".toRegex())
            .firstOrNull { it.isNotBlank() }
            ?: "User"
    }

    private fun loadProfilePicture() {
        try {
            val savedImagePath = sharedPreferences.getString("profileImageUri", null)
            val profilePictureView = findViewById<ImageView>(R.id.profilePicture)

            profilePictureView.setImageDrawable(null)
            profilePictureView.setBackgroundColor(Color.TRANSPARENT)

            if (savedImagePath.isNullOrBlank()) {
                setDefaultProfileImage(profilePictureView)
                return
            }

            val imageFile = java.io.File(savedImagePath)
            val loaded = if (imageFile.exists()) {
                profilePictureView.setImageURI(Uri.fromFile(imageFile))
                profilePictureView.drawable != null
            } else {
                profilePictureView.setImageURI(Uri.parse(savedImagePath))
                profilePictureView.drawable != null
            }

            if (loaded) {
                profilePictureView.scaleType = ImageView.ScaleType.CENTER_CROP
            } else {
                setDefaultProfileImage(profilePictureView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val profilePictureView = findViewById<ImageView>(R.id.profilePicture)
            setDefaultProfileImage(profilePictureView)
        }
    }

    private fun setDefaultProfileImage(imageView: ImageView) {
        imageView.setImageResource(R.drawable.profilepic)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
    }
}

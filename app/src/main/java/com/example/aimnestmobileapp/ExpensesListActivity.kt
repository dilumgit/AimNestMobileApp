package com.example.aimnestmobileapp

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONArray
import org.json.JSONObject

class ExpensesListActivity : AppCompatActivity() {

    private lateinit var expensesContainer: LinearLayout
    private val allExpenses = mutableListOf<JSONObject>()
    private var selectedCategory: String = "All"
    private var currentQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_expenses_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        expensesContainer = findViewById(R.id.expensesContainer)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton?.setOnClickListener {
            startActivity(Intent(this, Dashboard::class.java))
            finish()
        }

        val addExpenseButton = findViewById<CardView>(R.id.fabAddExpense)
        addExpenseButton?.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        val menuItemManageIncome = findViewById<LinearLayout>(R.id.menuItemManageIncome)
        menuItemManageIncome?.setOnClickListener {
            startActivity(Intent(this, income_list::class.java))
        }

        val menuItemManageTarget = findViewById<LinearLayout>(R.id.menuItemManageTarget)
        menuItemManageTarget?.setOnClickListener {
            startActivity(Intent(this, target_list::class.java))
        }

        val menuItemViewAnalysis = findViewById<View>(R.id.menuItemViewAnalysis)
        menuItemViewAnalysis?.setOnClickListener {
            startActivity(Intent(this, AnalysisActivity::class.java))
            finish()
        }

        setupSearch()
        setupCategoryChips()
        loadSavedExpenses()
        applyFiltersAndRender()
    }

    override fun onResume() {
        super.onResume()
        loadSavedExpenses()
        applyFiltersAndRender()
    }

    private fun setupSearch() {
        val searchEditText = findViewById<EditText>(R.id.searchEditText)
        searchEditText?.setOnEditorActionListener { _, _, _ ->
            currentQuery = searchEditText.text.toString()
            applyFiltersAndRender()
            true
        }
        searchEditText?.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: android.text.Editable?) {
                currentQuery = s?.toString().orEmpty()
                applyFiltersAndRender()
            }
        })
    }

    private fun setupCategoryChips() {
        bindChip(R.id.chipAll, "All")
        bindChip(R.id.chipRent, "Rent")
        bindChip(R.id.chipUtilities, "Utilities")
        bindChip(R.id.chipFood, "Food")
        bindChip(R.id.chipTransport, "Transport")
        bindChip(R.id.chipLoan, "Loan")
        bindChip(R.id.chipGroceries, "Groceries")
        bindChip(R.id.chipEntertainment, "Entertainment")
        bindChip(R.id.chipMedical, "Medical")
        bindChip(R.id.chipEducation, "Education")
        bindChip(R.id.chipOther, "Other")
    }

    private fun bindChip(chipId: Int, category: String) {
        findViewById<TextView>(chipId)?.setOnClickListener {
            selectedCategory = category
            applyFiltersAndRender()
        }
    }

    private fun applyFiltersAndRender() {
        val query = currentQuery.trim().lowercase()
        val filtered = allExpenses.filter { expense ->
            val expenseCategory = expense.optString("category", "Other")
            val categoryMatch = selectedCategory.equals("All", ignoreCase = true) ||
                expenseCategory.equals(selectedCategory, ignoreCase = true)

            val name = expense.optString("name", "")
            val queryMatch = query.isEmpty() ||
                name.lowercase().contains(query) ||
                expenseCategory.lowercase().contains(query)

            categoryMatch && queryMatch
        }
        renderExpenses(filtered)
    }

    private fun loadSavedExpenses() {
        allExpenses.clear()
        val prefs = getSharedPreferences("aimnest_expense_data", MODE_PRIVATE)
        val json = prefs.getString("saved_expenses", "[]") ?: "[]"
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            allExpenses.add(array.getJSONObject(i))
        }
    }

    private fun renderExpenses(expenses: List<JSONObject>) {
        expensesContainer.removeAllViews()
        expenses.forEachIndexed { index, expense ->
            expensesContainer.addView(createExpenseCard(expense, index))
        }
    }

    private fun createExpenseCard(expense: JSONObject, index: Int): CardView {
        val card = CardView(this).apply {
            radius = dp(16f)
            cardElevation = dp(4f)
            setCardBackgroundColor(ContextCompat.getColor(this@ExpensesListActivity, android.R.color.white))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12f).toInt()
            }
        }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16f).toInt(), dp(16f).toInt(), dp(16f).toInt(), dp(16f).toInt())
        }

        val left = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val title = TextView(this).apply {
            text = expense.optString("name", "Expense")
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@ExpensesListActivity, android.R.color.black))
        }

        val amount = TextView(this).apply {
            text = "Rs : ${expense.optString("amount", "0")}"
            textSize = 15f
            setTextColor(0xFF1976D2.toInt())
            setPadding(0, dp(6f).toInt(), 0, 0)
        }

        val category = TextView(this).apply {
            text = expense.optString("category", "Other")
            textSize = 12f
            setTextColor(ContextCompat.getColor(this@ExpensesListActivity, android.R.color.darker_gray))
            setPadding(0, dp(6f).toInt(), 0, 0)
        }

        val isMandatory = expense.optBoolean("isMandatory", false)
        val mandatoryStatus = TextView(this).apply {
            text = if (isMandatory) "Mandatory: Yes" else "Mandatory: No"
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(if (isMandatory) 0xFFD32F2F.toInt() else 0xFF388E3C.toInt())
            setPadding(0, dp(6f).toInt(), 0, 0)
        }

        val date = TextView(this).apply {
            text = expense.optString("date", "")
            textSize = 12f
            setTextColor(ContextCompat.getColor(this@ExpensesListActivity, android.R.color.darker_gray))
            setPadding(0, dp(6f).toInt(), 0, 0)
            visibility = if (text.isNullOrBlank()) View.GONE else View.VISIBLE
        }

        left.addView(title)
        left.addView(amount)
        left.addView(category)
        left.addView(mandatoryStatus)
        left.addView(date)

        val right = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        fun actionIcon(drawableId: Int, onClick: (() -> Unit)? = null): ImageView = ImageView(this).apply {
            setImageResource(drawableId)
            layoutParams = LinearLayout.LayoutParams(dp(24f).toInt(), dp(24f).toInt()).apply {
                marginStart = dp(12f).toInt()
            }
            contentDescription = "Action"
            onClick?.let { click ->
                isClickable = true
                isFocusable = true
                setOnClickListener { click() }
            }
        }

        right.addView(
            actionIcon(R.drawable.edit) {
                val intent = Intent(this@ExpensesListActivity, EditExpenseActivity::class.java)
                intent.putExtra("expense_index", index)
                startActivity(intent)
            }
        )
        right.addView(
            actionIcon(R.drawable.delete) {
                val intent = Intent(this@ExpensesListActivity, DeleteExpensesActivity::class.java)
                intent.putExtra("expense_index", index)
                startActivity(intent)
            }
        )

        row.addView(left)
        row.addView(right)
        card.addView(row)
        return card
    }

    private fun deleteExpenseAt(index: Int) {
        if (index !in allExpenses.indices) return
        allExpenses.removeAt(index)
        val array = JSONArray()
        allExpenses.forEach { array.put(it) }
        getSharedPreferences("aimnest_expense_data", MODE_PRIVATE).edit {
            putString("saved_expenses", array.toString())
        }
        applyFiltersAndRender()
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
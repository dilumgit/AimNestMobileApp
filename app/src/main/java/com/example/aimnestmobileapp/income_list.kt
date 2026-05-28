package com.example.aimnestmobileapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONArray

class income_list : AppCompatActivity() {

    data class IncomeItem(
        val title: String,
        val amount: String,
        val date: String,
        val notes: String,
        val originalIndex: Int
    )

    private lateinit var searchEditText: EditText
    private lateinit var incomeListContainer: LinearLayout
    private var allIncomes: List<IncomeItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_income_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        searchEditText = findViewById(R.id.searchEditText)
        incomeListContainer = findViewById(R.id.incomeListContainer)

        findViewById<View>(R.id.backButton).setOnClickListener {
            startActivity(Intent(this, Dashboard::class.java))
            finish()
        }

        findViewById<View>(R.id.fabAddIncome).setOnClickListener {
            startActivity(Intent(this, AddIncomeActivity::class.java))
        }

        findViewById<View>(R.id.menuItemManageExpenses).setOnClickListener {
            startActivity(Intent(this, ExpensesListActivity::class.java))
        }

        findViewById<View>(R.id.menuItemManageTarget).setOnClickListener {
            startActivity(Intent(this, target_list::class.java))
        }

        findViewById<View>(R.id.menuItemViewAnalysis).setOnClickListener {
            startActivity(Intent(this, AnalysisActivity::class.java))
            finish()
        }

        setupSearch()
        loadAndRenderIncomes()
    }

    override fun onResume() {
        super.onResume()
        loadAndRenderIncomes()
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilter(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun loadAndRenderIncomes() {
        allIncomes = readSavedIncomes()
        applyFilter(searchEditText.text?.toString().orEmpty())
    }

    private fun applyFilter(query: String) {
        val trimmed = query.trim()
        val filtered = if (trimmed.isEmpty()) {
            allIncomes
        } else {
            allIncomes.filter {
                it.title.contains(trimmed, ignoreCase = true) ||
                    it.notes.contains(trimmed, ignoreCase = true)
            }
        }
        renderIncomes(filtered, isFiltering = trimmed.isNotEmpty())
    }

    private fun renderIncomes(incomes: List<IncomeItem>, isFiltering: Boolean) {
        incomeListContainer.removeAllViews()

        if (incomes.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = if (isFiltering) "No matching incomes found" else "No incomes added yet"
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@income_list, android.R.color.darker_gray))
                gravity = Gravity.CENTER_HORIZONTAL
                setPadding(0, 24, 0, 24)
            }
            incomeListContainer.addView(emptyText)
            return
        }

        incomes.forEach { item ->
            incomeListContainer.addView(createIncomeCard(item))
        }
    }

    private fun readSavedIncomes(): List<IncomeItem> {
        val prefs = getSharedPreferences("aimnest_income_data", MODE_PRIVATE)
        val json = prefs.getString("saved_incomes", "[]") ?: "[]"
        val array = JSONArray(json)
        val items = mutableListOf<IncomeItem>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            items.add(
                IncomeItem(
                    title = obj.optString("title", "Income"),
                    amount = obj.optString("amount", "0"),
                    date = obj.optString("date", ""),
                    notes = obj.optString("notes", ""),
                    originalIndex = i
                )
            )
        }
        return items.reversed()
    }

    private fun createIncomeCard(item: IncomeItem): View {
        val card = CardView(this).apply {
            radius = dp(16).toFloat()
            cardElevation = dp(4).toFloat()
            setCardBackgroundColor(ContextCompat.getColor(this@income_list, android.R.color.white))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(12) }
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        val left = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val title = TextView(this).apply {
            text = item.title
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@income_list, android.R.color.black))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        val amount = TextView(this).apply {
            text = "Rs : ${item.amount}"
            textSize = 15f
            setTextColor(android.graphics.Color.parseColor("#1976D2"))
            setPadding(0, dp(6), 0, 0)
        }

        val date = TextView(this).apply {
            text = item.date
            textSize = 12f
            setTextColor(ContextCompat.getColor(this@income_list, android.R.color.darker_gray))
            setPadding(0, dp(6), 0, 0)
        }

        left.addView(title)
        left.addView(amount)
        left.addView(date)

        if (item.notes.isNotBlank()) {
            val notesText = TextView(this).apply {
                text = item.notes
                textSize = 12f
                setTextColor(ContextCompat.getColor(this@income_list, android.R.color.darker_gray))
                setPadding(0, dp(6), 0, 0)
                maxLines = 2
            }
            left.addView(notesText)
        }

        val right = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        fun actionIcon(drawableId: Int, onClick: (() -> Unit)? = null): ImageView = ImageView(this).apply {
            setImageResource(drawableId)
            layoutParams = LinearLayout.LayoutParams(dp(24), dp(24)).apply {
                marginStart = dp(12)
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
                val intent = Intent(this@income_list, edit_income::class.java).apply {
                    putExtra("income_title", item.title)
                    putExtra("income_amount", item.amount)
                    putExtra("income_date", item.date)
                    putExtra("income_notes", item.notes)
                    putExtra("income_index", item.originalIndex)
                }
                startActivity(intent)
            }
        )
        right.addView(
            actionIcon(R.drawable.delete) {
                val intent = Intent(this@income_list, delete_income::class.java).apply {
                    putExtra("income_index", item.originalIndex)
                }
                startActivity(intent)
            }
        )

        root.addView(left)
        root.addView(right)
        card.addView(root)
        return card
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
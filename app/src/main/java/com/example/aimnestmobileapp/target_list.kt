package com.example.aimnestmobileapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
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
import com.google.android.material.button.MaterialButton
import org.json.JSONArray
import org.json.JSONObject

class target_list : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_target_list)

        findViewById<View?>(R.id.main)?.let { root ->
            ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // Header back button -> Dashboard
        findViewById<ImageView?>(R.id.backButton)?.setOnClickListener {
            startActivity(Intent(this, Dashboard::class.java))
            finish()
        }

        // Add Target FAB -> Add Target screen
        findViewById<CardView?>(R.id.fabAddTarget)?.setOnClickListener {
            startActivity(Intent(this, add_target::class.java))
        }

        // Footer navigation (safe even if layout IDs/order vary)
        val footerPanel = findViewById<LinearLayout?>(R.id.footerPanel)
        if (footerPanel != null && footerPanel.childCount >= 4) {
            footerPanel.getChildAt(0)?.setOnClickListener {
                startActivity(Intent(this, income_list::class.java))
                finish()
            }
            footerPanel.getChildAt(1)?.setOnClickListener {
                startActivity(Intent(this, ExpensesListActivity::class.java))
                finish()
            }
            footerPanel.getChildAt(3)?.setOnClickListener {
                startActivity(Intent(this, AnalysisActivity::class.java))
                finish()
            }
        }

        renderSavedTargets()
        setupSearchListener()
    }

    override fun onResume() {
        super.onResume()
        renderSavedTargets()
        setupSearchListener()
    }

    private fun setupSearchListener() {
        val searchInput = findViewById<EditText?>(R.id.searchTargetInput)
        searchInput?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterTargets(s?.toString()?.trim().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterTargets(query: String) {
        val targetsContainer = findViewById<LinearLayout?>(R.id.targetsContainer) ?: return
        targetsContainer.removeAllViews()

        val prefs = getSharedPreferences("targets_prefs", MODE_PRIVATE)
        val raw = prefs.getString("targets_json", "[]") ?: "[]"
        val arr = try { JSONArray(raw) } catch (_: Exception) { JSONArray() }

        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: JSONObject()
            val name = obj.optString("name", "Target")
            val amount = obj.optString("amount", "0")
            val deadline = obj.optString("deadline", "")
            val priority = obj.optString("priority", "")

            if (name.contains(query, ignoreCase = true)) {
                targetsContainer.addView(createTargetCard(name, amount, deadline, priority))
            }
        }

        if (targetsContainer.childCount == 0 && query.isNotBlank()) {
            val noResultsTv = TextView(this).apply {
                text = "No targets found"
                textSize = 14f
                setTextColor(Color.parseColor("#999999"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(24f).toInt()
                }
                setPadding(dp(16f).toInt(), dp(16f).toInt(), dp(16f).toInt(), dp(16f).toInt())
            }
            targetsContainer.addView(noResultsTv)
        }
    }

    private fun renderSavedTargets() {
        val targetsContainer = findViewById<LinearLayout?>(R.id.targetsContainer) ?: return
        targetsContainer.removeAllViews()

        val prefs = getSharedPreferences("targets_prefs", MODE_PRIVATE)
        val raw = prefs.getString("targets_json", "[]") ?: "[]"
        val arr = try { JSONArray(raw) } catch (_: Exception) { JSONArray() }

        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: JSONObject()
            val name = obj.optString("name", "Target")
            val amount = obj.optString("amount", "0")
            val deadline = obj.optString("deadline", "")
            val priority = obj.optString("priority", "")

            targetsContainer.addView(createTargetCard(name, amount, deadline, priority))
        }
    }

    private fun createTargetCard(name: String, amount: String, deadline: String, priority: String): CardView {
        val card = CardView(this).apply {
            radius = dp(16f)
            cardElevation = dp(4f)
            setCardBackgroundColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(16f).toInt()
            }
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16f).toInt(), dp(16f).toInt(), dp(16f).toInt(), dp(16f).toInt())
        }

        val title = TextView(this).apply {
            text = name
            textSize = 16f
            setTextColor(Color.parseColor("#333333"))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        val amountTv = TextView(this).apply {
            text = "Target Rs : $amount"
            textSize = 14f
            setTextColor(Color.parseColor("#666666"))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, dp(12f).toInt(), 0, 0)
        }

        val deadlineTv = TextView(this).apply {
            text = "Deadline: $deadline"
            textSize = 12f
            setTextColor(Color.parseColor("#666666"))
            setPadding(0, dp(12f).toInt(), 0, 0)
        }

        val priorityTv = TextView(this).apply {
            text = priority
            textSize = 12f
            setTextColor(Color.parseColor("#1976D2"))
            setPadding(0, dp(8f).toInt(), 0, 0)
        }

        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(1f).toInt()
            ).apply {
                topMargin = dp(12f).toInt()
                bottomMargin = dp(12f).toInt()
            }
            setBackgroundColor(Color.parseColor("#E0E0E0"))
        }

        val viewBtn = MaterialButton(this).apply {
            text = "View Details"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#1976D2"))
            cornerRadius = dp(10f).toInt()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(45f).toInt()
            )
        }

        viewBtn.setOnClickListener {
            val intent = Intent(this, target_details::class.java).apply {
                putExtra("target_name", name)
                putExtra("target_amount", amount)
                putExtra("target_deadline", deadline)
                putExtra("target_priority", priority)
            }
            startActivity(intent)
        }

        content.addView(title)
        content.addView(amountTv)
        content.addView(deadlineTv)
        if (priority.isNotBlank()) content.addView(priorityTv)
        content.addView(divider)
        content.addView(viewBtn)

        card.addView(content)
        return card
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
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
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class target_details : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_target_details)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView?>(R.id.backButton)?.setOnClickListener {
            startActivity(Intent(this, target_list::class.java))
            finish()
        }

        val targetName = intent.getStringExtra("target_name").orEmpty()
        val targetAmount = intent.getStringExtra("target_amount").orEmpty()
        val targetDeadline = intent.getStringExtra("target_deadline").orEmpty()
        val targetPriority = intent.getStringExtra("target_priority").orEmpty()
        val targetSetDate = intent.getStringExtra("target_set_date").orEmpty().ifBlank { getTodayDateString() }

        findViewById<TextView>(R.id.targetNameValue).text = if (targetName.isBlank()) "Target" else targetName
        findViewById<TextView>(R.id.totalTargetValue).text = if (targetAmount.isBlank()) "0" else targetAmount
        findViewById<TextView>(R.id.deadlineValue).text = if (targetDeadline.isBlank()) "-" else targetDeadline
        findViewById<TextView>(R.id.priorityValue).text = if (targetPriority.isBlank()) "-" else targetPriority
        findViewById<TextView>(R.id.targetSetDateValue).text = targetSetDate

        val totalTarget = parseAmount(targetAmount)
        val requiredMonths = calculateRequiredMonths(targetDeadline)
        findViewById<TextView>(R.id.requiredMonthsValue).text = requiredMonths.toString()

        // Saved amount should appear only after a month is fully completed.
        val completedMonths = calculateFullyCompletedMonthsFromStart(targetSetDate)

        val initialMonthly = if (requiredMonths > 0) Math.ceil(totalTarget.toDouble() / requiredMonths).toInt() else 0
        val savedAmount = (completedMonths * initialMonthly).coerceAtMost(totalTarget)
        findViewById<TextView>(R.id.savedValue).text = savedAmount.toString()

        val remaining = (totalTarget - savedAmount).coerceAtLeast(0)
        findViewById<TextView>(R.id.remainingValue).text = remaining.toString()

        val requiredMonthly = if (requiredMonths > 0) Math.ceil(remaining.toDouble() / requiredMonths).toInt() else 0
        findViewById<TextView>(R.id.requiredMonthlyValue).text = requiredMonthly.toString()

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
            footerPanel.getChildAt(2)?.setOnClickListener {
                startActivity(Intent(this, target_list::class.java))
                finish()
            }
        }

        findViewById<MaterialButton?>(R.id.editTargetButton)?.setOnClickListener {
            val intent = Intent(this, edit_target::class.java).apply {
                putExtra("target_name", targetName)
                putExtra("target_amount", targetAmount)
                putExtra("target_deadline", targetDeadline)
                putExtra("target_priority", targetPriority)
                putExtra("target_set_date", targetSetDate)
            }
            startActivity(intent)
        }

        findViewById<MaterialButton?>(R.id.deleteTargetButton)?.setOnClickListener {
            val intent = Intent(this, delete_target::class.java).apply {
                putExtra("target_name", targetName)
                putExtra("target_amount", targetAmount)
                putExtra("target_deadline", targetDeadline)
                putExtra("target_priority", targetPriority)
                putExtra("target_set_date", targetSetDate)
            }
            startActivity(intent)
        }
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
    }

    private fun calculateRequiredMonths(deadlineDate: String): Int {
        if (deadlineDate.isBlank()) return 0

        val parser = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val end = runCatching { parser.parse(deadlineDate) }.getOrNull() ?: return 0

        val today = Calendar.getInstance()
        val endCal = Calendar.getInstance().apply { time = end }

        var months = (endCal.get(Calendar.YEAR) - today.get(Calendar.YEAR)) * 12 +
            (endCal.get(Calendar.MONTH) - today.get(Calendar.MONTH))

        if (endCal.get(Calendar.DAY_OF_MONTH) > today.get(Calendar.DAY_OF_MONTH)) {
            months += 1
        }

        return months.coerceAtLeast(0)
    }

    // Only count fully completed months after the target set date.
    private fun calculateFullyCompletedMonthsFromStart(startDate: String): Int {
        if (startDate.isBlank()) return 0

        val parser = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val start = runCatching { parser.parse(startDate) }.getOrNull() ?: return 0

        val startCal = Calendar.getInstance().apply { time = start }
        val today = Calendar.getInstance()

        var months = (today.get(Calendar.YEAR) - startCal.get(Calendar.YEAR)) * 12 +
            (today.get(Calendar.MONTH) - startCal.get(Calendar.MONTH))

        // If today is still inside the current month period relative to the start day,
        // that month is not fully completed yet.
        if (today.get(Calendar.DAY_OF_MONTH) < startCal.get(Calendar.DAY_OF_MONTH)) {
            months -= 1
        }

        return months.coerceAtLeast(0)
    }

    private fun parseAmount(raw: String): Int {
        return raw
            .replace("Rs", "", ignoreCase = true)
            .replace(":", "")
            .replace(",", "")
            .trim()
            .toIntOrNull() ?: 0
    }
}
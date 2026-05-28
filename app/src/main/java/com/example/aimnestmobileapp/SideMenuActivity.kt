package com.example.aimnestmobileapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SideMenuActivity : AppCompatActivity() {
    private val TAG = "SideMenuActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_side_menu)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Helper to safely start activities
        fun safeStartActivity(target: Class<*>) {
            try {
                startActivity(Intent(this, target))
                finish()
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to start activity ${target.simpleName}", ex)
                Toast.makeText(this, "Unable to open screen: ${target.simpleName}", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up click listener for the overlay view (right side white space)
        val overlayView = findViewById<View?>(R.id.overlayView)
        overlayView?.setOnClickListener {
            // Close the side menu and return to the previous screen (Dashboard)
            finish()
        }

        // Alerts and Warnings menu item click listener
        val alertsText = findViewById<View?>(R.id.alertsText)
        alertsText?.setOnClickListener { safeStartActivity(AlertsActivity::class.java) }
        val alertsIcon = findViewById<View?>(R.id.alertsIcon)
        alertsIcon?.setOnClickListener { safeStartActivity(AlertsActivity::class.java) }

        // Notification Center menu item click listener
        val notificationsText = findViewById<View?>(R.id.notificationsText)
        notificationsText?.setOnClickListener { safeStartActivity(NotificationCenterActivity::class.java) }
        val notificationsIcon = findViewById<View?>(R.id.notificationsIcon)
        notificationsIcon?.setOnClickListener { safeStartActivity(NotificationCenterActivity::class.java) }

        // Targets Overview menu item click listener
        val targetsText = findViewById<View?>(R.id.targetsText)
        targetsText?.setOnClickListener { safeStartActivity(TargetsOverviewActivity::class.java) }
        val targetsIcon = findViewById<View?>(R.id.targetsIcon)
        targetsIcon?.setOnClickListener { safeStartActivity(TargetsOverviewActivity::class.java) }

        // Smart Suggestions menu item click listener
        val suggestionsText = findViewById<View?>(R.id.suggestionsText)
        suggestionsText?.setOnClickListener { safeStartActivity(smart_suggetion::class.java) }
        val suggestionsIcon = findViewById<View?>(R.id.suggestionsIcon)
        suggestionsIcon?.setOnClickListener { safeStartActivity(smart_suggetion::class.java) }

        // Monthly Income Summary menu item click listener
        val incomeText = findViewById<View?>(R.id.incomeText)
        incomeText?.setOnClickListener { safeStartActivity(Monthly_Income_Summary::class.java) }
        val incomeIcon = findViewById<View?>(R.id.incomeIcon)
        incomeIcon?.setOnClickListener { safeStartActivity(Monthly_Income_Summary::class.java) }

        // Overall Analysis menu item click listener
        val analysisText = findViewById<View?>(R.id.analysisText)
        analysisText?.setOnClickListener { safeStartActivity(AnalysisActivity::class.java) }
        val analysisIcon = findViewById<View?>(R.id.analysisIcon)
        analysisIcon?.setOnClickListener { safeStartActivity(AnalysisActivity::class.java) }
    }
}
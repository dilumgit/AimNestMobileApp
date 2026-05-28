package com.example.aimnestmobileapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class UpgradePro : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_upgrade_pro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Back button
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // Maybe later text button - go back
        val maybeLaterButton = findViewById<TextView>(R.id.maybeLaterButton)
        maybeLaterButton.setOnClickListener {
            finish()
        }

        // Get payment method radio buttons
        val visaRadio = findViewById<RadioButton>(R.id.visaRadio)
        val mastercardRadio = findViewById<RadioButton>(R.id.mastercardRadio)
        val googlePayRadio = findViewById<RadioButton>(R.id.googlePayRadio)
        val bankTransferRadio = findViewById<RadioButton>(R.id.bankTransferRadio)
        val applePayRadio = findViewById<RadioButton>(R.id.applePayRadio)

        // Get VISA components
        val visaHeader = findViewById<LinearLayout>(R.id.visaHeader)
        val visaDetailsContainer = findViewById<LinearLayout>(R.id.visaDetailsContainer)

        // Set VISA as default selected
        visaRadio.isChecked = true

        // VISA header click listener - expand/collapse details
        visaHeader.setOnClickListener {
            if (visaDetailsContainer.visibility == View.GONE) {
                visaDetailsContainer.visibility = View.VISIBLE
                visaRadio.isChecked = true
            } else {
                visaDetailsContainer.visibility = View.GONE
            }
        }

        // VISA radio button click
        visaRadio.setOnClickListener {
            visaRadio.isChecked = true
            visaDetailsContainer.visibility = View.VISIBLE
            mastercardRadio.isChecked = false
            googlePayRadio.isChecked = false
            bankTransferRadio.isChecked = false
            applePayRadio.isChecked = false
        }

        // Mastercard radio button click
        mastercardRadio.setOnClickListener {
            mastercardRadio.isChecked = true
            visaRadio.isChecked = false
            visaDetailsContainer.visibility = View.GONE
            googlePayRadio.isChecked = false
            bankTransferRadio.isChecked = false
            applePayRadio.isChecked = false
        }

        // Google Pay radio button click
        googlePayRadio.setOnClickListener {
            googlePayRadio.isChecked = true
            visaRadio.isChecked = false
            visaDetailsContainer.visibility = View.GONE
            mastercardRadio.isChecked = false
            bankTransferRadio.isChecked = false
            applePayRadio.isChecked = false
        }

        // Bank Transfer radio button click
        bankTransferRadio.setOnClickListener {
            bankTransferRadio.isChecked = true
            visaRadio.isChecked = false
            visaDetailsContainer.visibility = View.GONE
            mastercardRadio.isChecked = false
            googlePayRadio.isChecked = false
            applePayRadio.isChecked = false
        }

        // Apple Pay radio button click
        applePayRadio.setOnClickListener {
            applePayRadio.isChecked = true
            visaRadio.isChecked = false
            visaDetailsContainer.visibility = View.GONE
            mastercardRadio.isChecked = false
            googlePayRadio.isChecked = false
            bankTransferRadio.isChecked = false
        }

        // Continue to Payment button - process payment
        val continuePaymentButton = findViewById<MaterialButton>(R.id.continuePaymentButton)
        continuePaymentButton.setOnClickListener {
            val selectedMethod = when {
                visaRadio.isChecked -> {
                    val cardholderName = findViewById<TextInputEditText>(R.id.cardholderNameInput).text.toString()
                    val cardNumber = findViewById<TextInputEditText>(R.id.cardNumberInput).text.toString()
                    val expiryDate = findViewById<TextInputEditText>(R.id.expiryDateInput).text.toString()
                    val cvv = findViewById<TextInputEditText>(R.id.cvvInput).text.toString()

                    if (cardholderName.isEmpty() || cardNumber.isEmpty() || expiryDate.isEmpty() || cvv.isEmpty()) {
                        Toast.makeText(this, "Please fill in all card details", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    "VISA - $cardholderName"
                }
                mastercardRadio.isChecked -> "Mastercard •••• 8891"
                googlePayRadio.isChecked -> "Google Pay"
                bankTransferRadio.isChecked -> "Bank Transfer"
                applePayRadio.isChecked -> "Apple Pay"
                else -> "No method selected"
            }

            Toast.makeText(this, "Payment method: $selectedMethod", Toast.LENGTH_SHORT).show()
            // Navigate to Pro Activated screen
            startActivity(Intent(this, ProActivated::class.java))
            finish()
        }
    }
}
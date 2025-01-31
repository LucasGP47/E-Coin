package com.example.e_coin

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var fromCurrencySpinner: Spinner
    private lateinit var toCurrencySpinner: Spinner
    private lateinit var amountEditText: EditText
    private lateinit var resultTextView: TextView

    companion object {
        val conversions = ArrayList<Conversion>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupSpinners()
    }

    private fun initViews() {
        fromCurrencySpinner = findViewById(R.id.fromCurrencySpinner)
        toCurrencySpinner = findViewById(R.id.toCurrencySpinner)
        amountEditText = findViewById(R.id.amountEditText)
        resultTextView = findViewById(R.id.resultTextView)

        findViewById<Button>(R.id.convertButton).setOnClickListener { convertCurrency() }
        findViewById<Button>(R.id.historyButton).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun setupSpinners() {
        val currencies = arrayOf("USD", "EUR", "BRL", "GBP", "JPY")

        val adapter = object : ArrayAdapter<String>(
            this,
            R.layout.spinner_item,
            android.R.id.text1,
            currencies
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.spinner_item, parent, false)

                view.findViewById<TextView>(android.R.id.text1).apply {
                    text = currencies[position]
                    setTextColor(Color.WHITE)
                    textSize = 16f
                }
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.spinner_dropdown_item, parent, false)

                view.findViewById<TextView>(android.R.id.text1).apply {
                    text = currencies[position]
                    setTextColor(Color.WHITE)
                    background = ContextCompat.getDrawable(context, R.drawable.spinner_bg)
                    textSize = 16f
                }
                return view
            }
        }

        fromCurrencySpinner.adapter = adapter
        toCurrencySpinner.adapter = adapter
    }

    private fun convertCurrency() {
        val from = fromCurrencySpinner.selectedItem.toString()
        val to = toCurrencySpinner.selectedItem.toString()
        val amountText = amountEditText.text.toString()

        if (amountText.isEmpty()) {
            Toast.makeText(this, "Digite um valor válido", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDouble()
        val url = "https://v6.exchangerate-api.com/v6/ded7f9faee93971a5e3b8d58/pair/$from/$to"

        Volley.newRequestQueue(this).add(StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("result") == "success") {
                        val rate = jsonResponse.getDouble("conversion_rate")
                        val result = amount * rate
                        resultTextView.text = "%.2f %s".format(result, to)
                        saveConversion(from, to, amount, result, rate)
                    } else {
                        Toast.makeText(this, "Erro na API", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro na resposta", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(this, "Falha na conexão", Toast.LENGTH_SHORT).show()
            }
        ))
    }

    private fun saveConversion(
        from: String,
        to: String,
        amount: Double,
        result: Double,
        rate: Double
    ) {
        val conversion = Conversion(
            from = from,
            to = to,
            amount = amount,
            result = result,
            rate = rate,
            date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
            trend = if (rate > 1.0) "↑" else "↓"
        )
        conversions.add(conversion)
    }
}

data class Conversion(
    val from: String,
    val to: String,
    val amount: Double,
    val result: Double,
    val rate: Double,
    val date: String,
    val trend: String
)
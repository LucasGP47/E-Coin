package com.example.e_coin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class HistoryActivity : AppCompatActivity() {
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        listView = findViewById(R.id.historyListView)
        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }

        setupListView()
    }

    private fun setupListView() {
        val adapter = object : ArrayAdapter<Conversion>(
            this,
            R.layout.history_item,
            android.R.id.text1,
            MainActivity.conversions.reversed()
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.history_item, parent, false)

                val conversion = getItem(position)!!
                view.findViewById<TextView>(R.id.dateText).text = conversion.date
                view.findViewById<TextView>(R.id.conversionText).text =
                    "${conversion.amount} ${conversion.from} → ${conversion.result} ${conversion.to}"

                view.findViewById<TextView>(R.id.trendText).apply {
                    text = conversion.trend
                    setTextColor(
                        ContextCompat.getColor(
                            context,
                            if (conversion.trend == "↑") R.color.green_up
                            else R.color.red_down
                        )
                    )
                }
                return view
            }
        }

        listView.adapter = adapter
    }
}
package com.example.e_coin

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.privacysandbox.tools.core.model.Method
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.e_coin.MainActivity.Companion.conversions
import org.json.JSONArray

class HistoryActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private val serverUrl = "http://26.3.8.59/conversion_service.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        listView = findViewById(R.id.historyListView)
        findViewById<Button>(R.id.exportButton).setOnClickListener { finish() }
        findViewById<Button>(R.id.exportButton).setOnClickListener { exportConversions() }

        loadConversions()
    }

    private fun loadConversions() {
        val queue = Volley.newRequestQueue(this)
        val request = JsonArrayRequest(
            com.android.volley.Request.Method.GET, serverUrl, null,
            { response ->
                val conversions = parseConversions(response)
                setupListView(conversions.reversed())
            },
            { error ->
                Log.e("API_ERROR", "Erro na requisição: ${error.message}")
                error.networkResponse?.let {
                    Log.e("API_ERROR", "Status Code: ${it.statusCode}")
                    Log.e("API_ERROR", "Response Data: ${String(it.data)}")
                }
                Toast.makeText(this, "Erro ao carregar histórico", Toast.LENGTH_LONG).show()
            })
        queue.add(request)
    }

    private fun parseConversions(response: JSONArray): List<Conversion> {
        val conversions = mutableListOf<Conversion>()
        for (i in 0 until response.length()) {
            val item = response.getJSONObject(i)
            conversions.add(Conversion(
                item.getString("from"),
                item.getString("to"),
                item.getDouble("amount"),
                item.getDouble("result"),
                item.getDouble("rate"),
                item.getString("date"),
                item.getString("trend")
            ))
        }
        return conversions
    }

    private fun setupListView(reversed: List<Conversion>) {
        val adapter = object : ArrayAdapter<Conversion>(
            this,
            R.layout.history_item,
            android.R.id.text1,
            conversions
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

    private fun exportConversions() {
        val exportUrl = "$serverUrl?action=export"
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(exportUrl))
            .setTitle("Exportar Conversões")
            .setDescription("Baixando arquivo de conversões")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(
                this,
                Environment.DIRECTORY_DOWNLOADS,
                "conversoes_moedas.txt"
            )

        downloadManager.enqueue(request)
        Toast.makeText(this, "Iniciando exportação...", Toast.LENGTH_SHORT).show()
    }
}
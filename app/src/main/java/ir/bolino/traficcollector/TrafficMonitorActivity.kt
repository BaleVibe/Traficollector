package ir.bolino.traficcollector

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import ir.bolino.traficcollector.adapter.TrafficDataAdapter
import ir.bolino.traficcollector.databinding.ActivityTrafficMonitorBinding
import ir.bolino.traficcollector.model.TrafficData
import ir.bolino.traficcollector.utils.TrafficDataStore
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class TrafficMonitorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrafficMonitorBinding
    private lateinit var adapter: TrafficDataAdapter
    private val TAG = "TrafficMonitorActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrafficMonitorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        setupFab()
        loadData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.traffic_data)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        adapter = TrafficDataAdapter { trafficData ->
            showTrafficDetails(trafficData)
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@TrafficMonitorActivity)
            adapter = this@TrafficMonitorActivity.adapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadData()
        }
    }

    private fun setupFab() {
        binding.exportFab.setOnClickListener {
            exportData()
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                val trafficData = TrafficDataStore.getTrafficData()
                adapter.submitList(trafficData.reversed()) // Show newest first
                
                binding.swipeRefresh.isRefreshing = false
                
                // Show/hide empty state
                if (trafficData.isEmpty()) {
                    binding.recyclerView.visibility = View.GONE
                    binding.emptyText.visibility = View.VISIBLE
                } else {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.emptyText.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading traffic data", e)
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun showTrafficDetails(trafficData: TrafficData) {
        // Create a dialog to show detailed information
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Connection Details")
            .setMessage(
                "Timestamp: ${TrafficDataStore.formatTimestamp(trafficData.timestamp)}\n" +
                "Direction: ${trafficData.direction}\n" +
                "Protocol: ${trafficData.protocol}\n" +
                "Source: ${trafficData.sourceIp}:${trafficData.sourcePort}\n" +
                "Destination: ${trafficData.destIp}:${trafficData.destPort}\n" +
                "Data Size: ${TrafficDataStore.formatBytes(trafficData.dataLength)}"
            )
            .setPositiveButton("Close", null)
            .create()
        
        dialog.show()
    }

    private fun exportData() {
        lifecycleScope.launch {
            try {
                val csvData = TrafficDataStore.exportToCSV()
                val fileName = "traffic_data_${System.currentTimeMillis()}.csv"
                val file = File(getExternalFilesDir(null), fileName)
                
                FileOutputStream(file).use { output ->
                    output.write(csvData.toByteArray())
                }
                
                // Share the file
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, android.net.Uri.fromFile(file))
                    putExtra(Intent.EXTRA_SUBJECT, "Traffic Data Export")
                    putExtra(Intent.EXTRA_TEXT, "Network traffic data from Traficcollector")
                }
                
                startActivity(Intent.createChooser(shareIntent, "Export Traffic Data"))
                
            } catch (e: Exception) {
                Log.e(TAG, "Error exporting data", e)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
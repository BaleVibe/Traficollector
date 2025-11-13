package ir.bolino.traficcollector

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import ir.bolino.traficcollector.adapter.AppSelectionAdapter
import ir.bolino.traficcollector.databinding.ActivityAppSelectionBinding
import ir.bolino.traficcollector.utils.TrafficDataStore
import kotlinx.coroutines.launch

class AppSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppSelectionBinding
    private lateinit var adapter: AppSelectionAdapter
    private val TAG = "AppSelectionActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        loadApps()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Select Application"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        adapter = AppSelectionAdapter { appInfo ->
            selectApp(appInfo)
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@AppSelectionActivity)
            adapter = this@AppSelectionActivity.adapter
        }
    }

    private fun loadApps() {
        lifecycleScope.launch {
            try {
                val apps = TrafficDataStore.getInstalledApps(this@AppSelectionActivity)
                adapter.submitList(apps)
                binding.progressBar?.visibility = View.GONE
            } catch (e: Exception) {
                Log.e(TAG, "Error loading apps", e)
                binding.progressBar?.visibility = View.GONE
            }
        }
    }

    private fun selectApp(appInfo: ir.bolino.traficcollector.model.AppInfo) {
        TrafficDataStore.selectedAppPackage = appInfo.packageName
        
        val result = Intent().apply {
            putExtra("app_name", appInfo.appName)
            putExtra("package_name", appInfo.packageName)
        }
        setResult(RESULT_OK, result)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
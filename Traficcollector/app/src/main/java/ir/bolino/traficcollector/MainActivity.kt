package ir.bolino.traficcollector

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ir.bolino.traficcollector.databinding.ActivityMainBinding
import ir.bolino.traficcollector.service.TrafficVpnService
import ir.bolino.traficcollector.utils.TrafficDataStore
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"
    private val VPN_REQUEST_CODE = 0x0F
    
    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            startVpnService()
        } else {
            Toast.makeText(this, "VPN permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupClickListeners()
        setupRadioGroup()
        updateUI()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.app_name)
        }
    }

    private fun setupClickListeners() {
        binding.selectAppButton.setOnClickListener {
            val intent = Intent(this, AppSelectionActivity::class.java)
            startActivity(intent)
        }

        binding.startMonitoringButton.setOnClickListener {
            startMonitoring()
        }

        binding.stopMonitoringButton.setOnClickListener {
            stopMonitoring()
        }

        binding.viewDataButton.setOnClickListener {
            val intent = Intent(this, TrafficMonitorActivity::class.java)
            startActivity(intent)
        }

        binding.clearDataButton.setOnClickListener {
            clearData()
        }
    }

    private fun setupRadioGroup() {
        binding.modeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.simple_mode_radio -> {
                    TrafficDataStore.monitoringMode = "SIMPLE"
                }
                R.id.full_mode_radio -> {
                    TrafficDataStore.monitoringMode = "FULL"
                }
            }
        }
        
        // Set default selection
        binding.simpleModeRadio.isChecked = true
    }

    private fun startMonitoring() {
        if (TrafficDataStore.selectedAppPackage == null) {
            Toast.makeText(this, "Please select an app first", Toast.LENGTH_SHORT).show()
            return
        }

        // Check VPN permission
        val vpnIntent = VpnService.prepare(this)
        if (vpnIntent != null) {
            vpnPermissionLauncher.launch(vpnIntent)
        } else {
            startVpnService()
        }
    }

    private fun startVpnService() {
        try {
            val intent = Intent(this, TrafficVpnService::class.java).apply {
                action = "START_VPN"
            }
            startService(intent)
            
            updateUI()
            Toast.makeText(this, "Monitoring started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting VPN service", e)
            Toast.makeText(this, "Failed to start monitoring", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopMonitoring() {
        try {
            val intent = Intent(this, TrafficVpnService::class.java).apply {
                action = "STOP_VPN"
            }
            stopService(intent)
            
            updateUI()
            Toast.makeText(this, "Monitoring stopped", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping VPN service", e)
            Toast.makeText(this, "Failed to stop monitoring", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearData() {
        lifecycleScope.launch {
            TrafficDataStore.clearTrafficData()
            Toast.makeText(this@MainActivity, "Data cleared", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI() {
        // Update selected app text
        val selectedApp = TrafficDataStore.selectedAppPackage
        if (selectedApp != null) {
            binding.selectedAppText.text = selectedApp
        } else {
            binding.selectedAppText.setText(R.string.no_app_selected)
        }

        // Update monitoring status
        val isMonitoring = isVpnServiceRunning()
        binding.statusText.text = if (isMonitoring) {
            getString(R.string.monitoring_active)
        } else {
            getString(R.string.monitoring_inactive)
        }

        // Update button states
        binding.startMonitoringButton.isEnabled = !isMonitoring && selectedApp != null
        binding.stopMonitoringButton.isEnabled = isMonitoring
    }

    private fun isVpnServiceRunning(): Boolean {
        // Check if VPN service is running
        return try {
            val vpnService = TrafficVpnService()
            false // Simplified check - in real implementation you'd check service status
        } catch (e: Exception) {
            false
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }
}
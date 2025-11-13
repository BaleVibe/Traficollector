package ir.bolino.traficcollector.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ir.bolino.traficcollector.databinding.ItemTrafficDataBinding
import ir.bolino.traficcollector.model.TrafficData
import ir.bolino.traficcollector.utils.TrafficDataStore

class TrafficDataAdapter(
    private val onItemClick: (TrafficData) -> Unit
) : ListAdapter<TrafficData, TrafficDataAdapter.TrafficViewHolder>(TrafficDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrafficViewHolder {
        val binding = ItemTrafficDataBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TrafficViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrafficViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TrafficViewHolder(
        private val binding: ItemTrafficDataBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var isExpanded = false

        fun bind(trafficData: TrafficData) {
            binding.timestamp.text = TrafficDataStore.formatTimestamp(trafficData.timestamp)
            binding.direction.text = trafficData.direction
            binding.protocol.text = trafficData.protocol
            binding.dataSize.text = TrafficDataStore.formatBytes(trafficData.dataLength)
            
            val endpoint = "${trafficData.destIp}:${trafficData.destPort}"
            binding.endpoint.text = endpoint
            
            // Set colors
            binding.direction.setBackgroundColor(TrafficDataStore.getDirectionColor(trafficData.direction))
            binding.protocol.setBackgroundColor(TrafficDataStore.getProtocolColor(trafficData.protocol))
            
            // Handle raw data section
            if (trafficData.rawData != null) {
                binding.root.setOnClickListener {
                    isExpanded = !isExpanded
                    binding.rawDataSection.visibility = if (isExpanded) View.VISIBLE else View.GONE
                    
                    if (isExpanded) {
                        val hexString = trafficData.rawData?.joinToString(" ") { 
                            String.format("%02X", it) 
                        }
                        binding.rawData.text = hexString
                    }
                }
            } else {
                binding.root.setOnClickListener(null)
                binding.rawDataSection.visibility = View.GONE
            }
            
            // Handle item click
            binding.root.setOnClickListener {
                onItemClick(trafficData)
            }
        }
    }

    private class TrafficDiffCallback : DiffUtil.ItemCallback<TrafficData>() {
        override fun areItemsTheSame(oldItem: TrafficData, newItem: TrafficData): Boolean {
            return oldItem.timestamp == newItem.timestamp && 
                   oldItem.sourceIp == newItem.sourceIp &&
                   oldItem.destIp == newItem.destIp
        }

        override fun areContentsTheSame(oldItem: TrafficData, newItem: TrafficData): Boolean {
            return oldItem == newItem
        }
    }
}
package com.kaasht.croprecommendation.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kaasht.croprecommendation.R
import com.kaasht.croprecommendation.data.model.PredictionEntity
import com.kaasht.croprecommendation.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val onItemClick: (PredictionEntity) -> Unit,
    private val onItemLongClick: (PredictionEntity) -> Unit
) : ListAdapter<PredictionEntity, HistoryAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    fun getItemAt(position: Int): PredictionEntity = getItem(position)
    
    inner class ViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
            
            binding.root.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemLongClick(getItem(position))
                }
                true
            }
        }
        
        fun bind(prediction: PredictionEntity) {
            binding.apply {
                // Crop name
                tvCropName.text = prediction.predictedCrop.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                
                // Confidence
                tvConfidence.text = "${String.format("%.1f", prediction.confidence)}%"
                progressConfidence.progress = prediction.confidence.toInt()
                
                // District
                tvDistrict.text = prediction.district.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                
                // Date and time
                val date = Date(prediction.timestamp)
                tvDate.text = dateFormat.format(date)
                tvTime.text = timeFormat.format(date)
                
                // Weather info
                tvTemperature.text = "${prediction.temperature.toInt()}°C"
                tvHumidity.text = "${prediction.humidity.toInt()}%"
                
                // Soil health indicator
                val soilHealth = calculateSoilHealth(prediction)
                tvSoilHealth.text = soilHealth
                tvSoilHealth.setTextColor(
                    when (soilHealth) {
                        "Good" -> root.context.getColor(R.color.green)
                        "Moderate" -> root.context.getColor(R.color.orange)
                        else -> root.context.getColor(R.color.red)
                    }
                )
                
                // Crop icon
                ivCropIcon.setImageResource(getCropIcon(prediction.predictedCrop))
            }
        }
        
        private fun calculateSoilHealth(prediction: PredictionEntity): String {
            val n = prediction.nitrogen
            val p = prediction.phosphorus
            val k = prediction.potassium
            val ph = prediction.ph
            
            return when {
                n >= 50 && p >= 25 && k >= 25 && ph in 6.0..7.5 -> "Good"
                n >= 30 && p >= 15 && k >= 15 && ph in 5.5..8.0 -> "Moderate"
                else -> "Poor"
            }
        }
        
        private fun getCropIcon(crop: String): Int {
            return when (crop.lowercase()) {
                "rice" -> R.drawable.ic_rice
                "wheat" -> R.drawable.ic_wheat
                "maize" -> R.drawable.ic_corn
                "cotton" -> R.drawable.ic_cotton
                "sugarcane" -> R.drawable.ic_sugarcane
                else -> R.drawable.ic_crop_default
            }
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<PredictionEntity>() {
        override fun areItemsTheSame(
            oldItem: PredictionEntity,
            newItem: PredictionEntity
        ): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(
            oldItem: PredictionEntity,
            newItem: PredictionEntity
        ): Boolean {
            return oldItem == newItem
        }
    }
}

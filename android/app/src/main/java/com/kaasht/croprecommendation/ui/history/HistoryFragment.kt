package com.kaasht.croprecommendation.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.kaasht.croprecommendation.R
import com.kaasht.croprecommendation.data.model.PredictionEntity
import com.kaasht.croprecommendation.data.model.Resource
import com.kaasht.croprecommendation.databinding.FragmentHistoryBinding
import com.kaasht.croprecommendation.utils.ExportHelper
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HistoryFragment : Fragment() {
    
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var historyAdapter: HistoryAdapter
    
    @Inject
    lateinit var exportHelper: ExportHelper
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupChart()
        setupObservers()
        setupListeners()
        
        viewModel.loadPredictions()
    }
    
    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(
            onItemClick = { prediction ->
                showPredictionDetails(prediction)
            },
            onItemLongClick = { prediction ->
                showDeleteConfirmation(prediction)
            }
        )
        
        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
            setHasFixedSize(true)
        }
        
        // Swipe to delete
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false
            
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val prediction = historyAdapter.getItemAt(position)
                viewModel.deletePrediction(prediction)
                
                Snackbar.make(binding.root, "Prediction deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") {
                        viewModel.undoDelete()
                    }
                    .show()
            }
        })
        
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewHistory)
    }
    
    private fun setupChart() {
        binding.chartCropDistribution.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawGridBackground(false)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelRotationAngle = -45f
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                granularity = 1f
                axisMinimum = 0f
            }
            
            axisRight.isEnabled = false
            legend.isEnabled = true
        }
    }
    
    private fun setupObservers() {
        // Predictions list observer
        viewModel.predictions.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recyclerViewHistory.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    
                    val predictions = resource.data ?: emptyList()
                    
                    if (predictions.isEmpty()) {
                        binding.recyclerViewHistory.visibility = View.GONE
                        binding.layoutEmpty.visibility = View.VISIBLE
                    } else {
                        binding.recyclerViewHistory.visibility = View.VISIBLE
                        binding.layoutEmpty.visibility = View.GONE
                        historyAdapter.submitList(predictions)
                        
                        // Update statistics
                        updateStatistics(predictions)
                        updateChart(predictions)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showSnackbar("Error loading predictions: ${resource.message}")
                }
            }
        }
        
        // Filtered predictions observer
        viewModel.filteredPredictions.observe(viewLifecycleOwner) { predictions ->
            historyAdapter.submitList(predictions)
        }
        
        // Export status observer
        viewModel.exportStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showSnackbar("Exporting data...")
                }
                is Resource.Success -> {
                    showSnackbar("Export successful: ${resource.data}")
                }
                is Resource.Error -> {
                    showSnackbar("Export failed: ${resource.message}")
                }
            }
        }
    }
    
    private fun updateStatistics(predictions: List<PredictionEntity>) {
        binding.apply {
            // Total predictions
            tvTotalPredictions.text = predictions.size.toString()
            
            // Most predicted crop
            val mostPredicted = predictions
                .groupingBy { it.predictedCrop }
                .eachCount()
                .maxByOrNull { it.value }
            tvMostPredicted.text = mostPredicted?.key?.capitalize() ?: "N/A"
            
            // Average confidence
            val avgConfidence = predictions.map { it.confidence }.average()
            tvAvgConfidence.text = "${String.format("%.1f", avgConfidence)}%"
            
            // Last prediction date
            val lastPrediction = predictions.maxByOrNull { it.timestamp }
            val dateFormat = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
            tvLastPrediction.text = lastPrediction?.let { 
                dateFormat.format(java.util.Date(it.timestamp)) 
            } ?: "N/A"
        }
    }
    
    private fun updateChart(predictions: List<PredictionEntity>) {
        // Count predictions per crop
        val cropCounts = predictions
            .groupingBy { it.predictedCrop }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(5) // Top 5 crops
        
        val entries = cropCounts.mapIndexed { index, (_, count) ->
            BarEntry(index.toFloat(), count.toFloat())
        }
        
        val labels = cropCounts.map { it.first.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() } }
        
        val dataSet = BarDataSet(entries, "Crop Predictions").apply {
            colors = listOf(
                resources.getColor(R.color.green, null),
                resources.getColor(R.color.blue, null),
                resources.getColor(R.color.orange, null),
                resources.getColor(R.color.purple, null),
                resources.getColor(R.color.red, null)
            )
            valueTextSize = 12f
        }
        
        binding.chartCropDistribution.apply {
            data = BarData(dataSet)
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelCount = labels.size
            invalidate()
            animateY(1000)
        }
    }
    
    private fun setupListeners() {
        // Search functionality
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchPredictions(it) }
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.searchPredictions(it) }
                return true
            }
        })
        
        // Filter buttons
        binding.chipAll.setOnClickListener {
            viewModel.filterByTimeRange(HistoryViewModel.TimeRange.ALL)
        }
        
        binding.chipWeek.setOnClickListener {
            viewModel.filterByTimeRange(HistoryViewModel.TimeRange.WEEK)
        }
        
        binding.chipMonth.setOnClickListener {
            viewModel.filterByTimeRange(HistoryViewModel.TimeRange.MONTH)
        }
        
        binding.chipYear.setOnClickListener {
            viewModel.filterByTimeRange(HistoryViewModel.TimeRange.YEAR)
        }
        
        // Export buttons
        binding.btnExportCsv.setOnClickListener {
            viewModel.exportToCsv(requireContext())
        }
        
        binding.btnExportPdf.setOnClickListener {
            viewModel.exportToPdf(requireContext())
        }
        
        // Clear all button
        binding.btnClearAll.setOnClickListener {
            showClearAllConfirmation()
        }
        
        // Sync button
        binding.btnSync.setOnClickListener {
            viewModel.syncWithCloud()
        }
    }
    
    private fun showPredictionDetails(prediction: PredictionEntity) {
        val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
        
        val message = """
            Crop: ${prediction.predictedCrop.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }}
            Confidence: ${String.format("%.1f", prediction.confidence)}%
            District: ${prediction.district.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }}
            
            Environmental Data:
            • Temperature: ${prediction.temperature}°C
            • Humidity: ${prediction.humidity}%
            • Rainfall: ${prediction.rainfall} mm
            
            Soil Data:
            • Nitrogen: ${prediction.nitrogen} kg/ha
            • Phosphorus: ${prediction.phosphorus} kg/ha
            • Potassium: ${prediction.potassium} kg/ha
            • pH: ${prediction.ph}
            
            Date: ${dateFormat.format(java.util.Date(prediction.timestamp))}
        """.trimIndent()
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Prediction Details")
            .setMessage(message)
            .setPositiveButton("Close", null)
            .setNeutralButton("Share") { _, _ ->
                sharePrediction(prediction)
            }
            .show()
    }
    
    private fun showDeleteConfirmation(prediction: PredictionEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Prediction")
            .setMessage("Are you sure you want to delete this prediction?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deletePrediction(prediction)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showClearAllConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Clear All History")
            .setMessage("This will delete all prediction records. This action cannot be undone.")
            .setPositiveButton("Clear All") { _, _ ->
                viewModel.clearAllPredictions()
                showSnackbar("All predictions cleared")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun sharePrediction(prediction: PredictionEntity) {
        val shareText = """
            Kaasht Crop Prediction
            
            Recommended Crop: ${prediction.predictedCrop.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }}
            Confidence: ${String.format("%.1f", prediction.confidence)}%
            District: ${prediction.district.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }}
            
            Shared from Kaasht App
        """.trimIndent()
        
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
        }
        
        startActivity(android.content.Intent.createChooser(shareIntent, "Share Prediction"))
    }
    
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

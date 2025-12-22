package com.kaasht.croprecommendation.ui.weather

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.snackbar.Snackbar
import com.kaasht.croprecommendation.R
import com.kaasht.croprecommendation.data.model.Resource
import com.kaasht.croprecommendation.databinding.FragmentWeatherBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class WeatherFragment : Fragment() {
    
    private var _binding: FragmentWeatherBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: WeatherViewModel by viewModels()
    
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                viewModel.fetchCurrentLocationWeather()
            }
            else -> {
                showSnackbar("Location permission denied. Using default location.")
                viewModel.fetchWeatherForDistrict("lahore")
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeatherBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupObservers()
        setupListeners()
        checkLocationPermission()
    }
    
    private fun setupUI() {
        // Setup temperature chart
        setupChart(binding.chartTemperature, "Temperature (°C)")
        
        // Setup humidity chart
        setupChart(binding.chartHumidity, "Humidity (%)")
        
        // Setup wind speed chart
        setupChart(binding.chartWindSpeed, "Wind Speed (m/s)")
    }
    
    private fun setupChart(chart: LineChart, label: String) {
        chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setDrawGridBackground(false)
            setPinchZoom(true)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = resources.getColor(R.color.gray_light, null)
            }
            
            axisRight.isEnabled = false
            legend.isEnabled = true
        }
    }
    
    private fun setupObservers() {
        // Current weather observer
        viewModel.currentWeather.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.layoutWeatherContent.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutWeatherContent.visibility = View.VISIBLE
                    
                    resource.data?.let { weather ->
                        updateWeatherUI(weather)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showSnackbar("Failed to fetch weather: ${resource.message}")
                }
            }
        }
        
        // Weather forecast observer
        viewModel.weatherForecast.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { forecast ->
                        updateForecastCharts(forecast)
                    }
                }
                is Resource.Error -> {
                    Timber.e("Forecast error: ${resource.message}")
                }
                else -> { /* Loading state handled by current weather */ }
            }
        }
        
        // Location name observer
        viewModel.locationName.observe(viewLifecycleOwner) { location ->
            binding.tvLocation.text = location
        }
        
        // Last updated time observer
        viewModel.lastUpdated.observe(viewLifecycleOwner) { timestamp ->
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            binding.tvLastUpdated.text = "Updated: ${dateFormat.format(Date(timestamp))}"
        }
    }
    
    private fun updateWeatherUI(weather: com.kaasht.croprecommendation.data.model.WeatherData) {
        binding.apply {
            // Temperature
            tvTemperature.text = "${weather.temperature.toInt()}°C"
            tvFeelsLike.text = "Feels like ${weather.temperature.toInt()}°C"
            
            // Humidity
            tvHumidity.text = "${weather.humidity}%"
            
            // Rainfall
            tvRainfall.text = "${weather.rainfall} mm"
            
            // Weather condition
            tvCondition.text = weather.condition
            
            // Weather icon
            when {
                weather.condition.contains("rain", ignoreCase = true) -> {
                    ivWeatherIcon.setImageResource(R.drawable.ic_rain)
                }
                weather.condition.contains("cloud", ignoreCase = true) -> {
                    ivWeatherIcon.setImageResource(R.drawable.ic_cloudy)
                }
                weather.condition.contains("clear", ignoreCase = true) -> {
                    ivWeatherIcon.setImageResource(R.drawable.ic_sunny)
                }
                else -> {
                    ivWeatherIcon.setImageResource(R.drawable.ic_weather_default)
                }
            }
            
            // Additional info
            updateWeatherAdvice(weather)
        }
    }
    
    private fun updateWeatherAdvice(weather: com.kaasht.croprecommendation.data.model.WeatherData) {
        val advice = buildString {
            when {
                weather.temperature > 35 -> {
                    append("🌡️ High temperature warning. Ensure adequate irrigation.\n")
                }
                weather.temperature < 10 -> {
                    append("❄️ Low temperature. Protect sensitive crops from frost.\n")
                }
            }
            
            when {
                weather.humidity > 80 -> {
                    append("💧 High humidity. Monitor for fungal diseases.\n")
                }
                weather.humidity < 30 -> {
                    append("🏜️ Low humidity. Increase watering frequency.\n")
                }
            }
            
            if (weather.rainfall > 20) {
                append("☔ Heavy rainfall expected. Ensure proper drainage.\n")
            }
        }
        
        binding.tvWeatherAdvice.text = if (advice.isNotEmpty()) {
            advice.trim()
        } else {
            "✅ Weather conditions are favorable for farming."
        }
    }
    
    private fun updateForecastCharts(forecast: List<com.kaasht.croprecommendation.data.model.WeatherData>) {
        if (forecast.isEmpty()) return
        
        // Temperature data
        val tempEntries = forecast.mapIndexed { index, data ->
            Entry(index.toFloat(), data.temperature.toFloat())
        }
        updateChartData(binding.chartTemperature, tempEntries, "Temperature", 
            resources.getColor(R.color.red, null))
        
        // Humidity data
        val humidityEntries = forecast.mapIndexed { index, data ->
            Entry(index.toFloat(), data.humidity.toFloat())
        }
        updateChartData(binding.chartHumidity, humidityEntries, "Humidity", 
            resources.getColor(R.color.blue, null))
        
        // Wind speed (mock data - replace with actual if available)
        val windEntries = forecast.mapIndexed { index, _ ->
            Entry(index.toFloat(), (5..15).random().toFloat())
        }
        updateChartData(binding.chartWindSpeed, windEntries, "Wind Speed", 
            resources.getColor(R.color.green, null))
    }
    
    private fun updateChartData(
        chart: LineChart,
        entries: List<Entry>,
        label: String,
        color: Int
    ) {
        val dataSet = LineDataSet(entries, label).apply {
            this.color = color
            lineWidth = 2f
            circleRadius = 4f
            setCircleColor(color)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        
        chart.apply {
            data = LineData(dataSet)
            invalidate() // Refresh chart
            animateX(1000)
        }
    }
    
    private fun setupListeners() {
        // Refresh button
        binding.btnRefresh.setOnClickListener {
            viewModel.fetchCurrentLocationWeather()
        }
        
        // Share weather button
        binding.btnShare.setOnClickListener {
            shareWeatherInfo()
        }
        
        // 7-day forecast button
        binding.btnForecast.setOnClickListener {
            viewModel.fetchWeatherForecast()
        }
    }
    
    private fun checkLocationPermission() {
        when {
            hasLocationPermission() -> {
                viewModel.fetchCurrentLocationWeather()
            }
            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }
    
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun shareWeatherInfo() {
        val weather = viewModel.currentWeather.value?.data ?: return
        
        val shareText = """
            Current Weather - ${viewModel.locationName.value}
            
            Temperature: ${weather.temperature}°C
            Humidity: ${weather.humidity}%
            Rainfall: ${weather.rainfall} mm
            Condition: ${weather.condition}
            
            Shared from Kaasht App
        """.trimIndent()
        
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
        }
        
        startActivity(android.content.Intent.createChooser(shareIntent, "Share Weather"))
    }
    
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

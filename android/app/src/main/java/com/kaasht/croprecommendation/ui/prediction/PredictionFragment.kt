package com.kaasht.croprecommendation.ui.prediction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.kaasht.croprecommendation.R
import com.kaasht.croprecommendation.data.model.Resource
import com.kaasht.croprecommendation.databinding.FragmentPredictionBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PredictionFragment : Fragment() {
    
    private var _binding: FragmentPredictionBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PredictionViewModel by viewModels()
    
    private val districts = listOf(
        "attock", "rawalpindi", "isl", "jehlum", "chakwall", "sarjodha",
        "khushab", "mianwali", "bakar", "faisalabad", "tobataiksingh",
        "jhang", "gujrat", "m.b.din", "sialkot", "narowall", "gujranwala",
        "hafizabad", "shekupora", "nankana sahib", "lahore", "kasur",
        "okara", "sahiwal", "pakpatan", "multan", "lodhran", "khanewal",
        "vihari", "muzafargarh", "layyah", "d.g.khan", "rajanpur",
        "bahawalnagar", "ryk", "bwp"
    )
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPredictionBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupObservers()
        setupListeners()
    }
    
    private fun setupUI() {
        // Setup district spinner
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            districts.map { it.capitalize() }
        )
        binding.spinnerDistrict.adapter = adapter
        
        // Set default hints
        binding.tilNitrogen.hint = "Nitrogen (N) - kg/ha"
        binding.tilPhosphorus.hint = "Phosphorus (P) - kg/ha"
        binding.tilPotassium.hint = "Potassium (K) - kg/ha"
        binding.tilPh.hint = "Soil pH (0-14)"
    }
    
    private fun setupObservers() {
        // Weather data observer
        viewModel.weatherData.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressWeather.visibility = View.VISIBLE
                    binding.cardWeatherData.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressWeather.visibility = View.GONE
                    binding.cardWeatherData.visibility = View.VISIBLE
                    
                    resource.data?.let { weather ->
                        binding.tvTemperature.text = "${weather.temperature}°C"
                        binding.tvHumidity.text = "${weather.humidity}%"
                        binding.tvRainfall.text = "${weather.rainfall} mm"
                    }
                }
                is Resource.Error -> {
                    binding.progressWeather.visibility = View.GONE
                    showSnackbar("Weather fetch failed: ${resource.message}")
                }
            }
        }
        
        // Soil data observer
        viewModel.soilData.observe(viewLifecycleOwner) { soilHealth ->
            soilHealth?.let {
                binding.tvSoilStatus.text = "Soil Status: ${it.status}"
                binding.tvSoilStatus.setTextColor(
                    when (it.status) {
                        "Good" -> resources.getColor(R.color.green, null)
                        "Moderate" -> resources.getColor(R.color.orange, null)
                        else -> resources.getColor(R.color.red, null)
                    }
                )
            }
        }
        
        // Prediction result observer
        viewModel.predictionState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.btnPredict.isEnabled = false
                    binding.progressPrediction.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.btnPredict.isEnabled = true
                    binding.progressPrediction.visibility = View.GONE
                    
                    // Navigate to result screen with data
                    resource.data?.let { response ->
                        val action = PredictionFragmentDirections
                            .actionPredictionToResult(response)
                        findNavController().navigate(action)
                    }
                }
                is Resource.Error -> {
                    binding.btnPredict.isEnabled = true
                    binding.progressPrediction.visibility = View.GONE
                    showSnackbar("Prediction failed: ${resource.message}")
                }
            }
        }
        
        // Input validation observer
        viewModel.inputValidation.observe(viewLifecycleOwner) { validation ->
            if (!validation.isValid) {
                val errorMessage = validation.errors.joinToString("\n")
                showSnackbar(errorMessage)
            }
        }
    }
    
    private fun setupListeners() {
        // District selection
        binding.spinnerDistrict.onItemSelectedListener = 
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedDistrict = districts[position]
                    viewModel.district.value = selectedDistrict
                    
                    // Fetch weather data for selected district
                    viewModel.fetchWeatherData(selectedDistrict)
                }
                
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                    // Do nothing
                }
            }
        
        // Connect to sensor button
        binding.btnConnectSensor.setOnClickListener {
            findNavController().navigate(R.id.action_prediction_to_soilSensor)
        }
        
        // Manual input toggle
        binding.switchManualInput.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutManualInput.visibility = if (isChecked) View.VISIBLE else View.GONE
            binding.btnConnectSensor.visibility = if (isChecked) View.GONE else View.VISIBLE
        }
        
        // Predict button
        binding.btnPredict.setOnClickListener {
            collectInputData()
            viewModel.predictCrops()
        }
        
        // Reset button
        binding.btnReset.setOnClickListener {
            resetForm()
        }
        
        // Refresh weather button
        binding.btnRefreshWeather.setOnClickListener {
            val district = viewModel.district.value
            if (district != null) {
                viewModel.fetchWeatherData(district)
            } else {
                showSnackbar("Please select a district first")
            }
        }
    }
    
    private fun collectInputData() {
        viewModel.nitrogen.value = binding.etNitrogen.text.toString()
        viewModel.phosphorus.value = binding.etPhosphorus.text.toString()
        viewModel.potassium.value = binding.etPotassium.text.toString()
        viewModel.ph.value = binding.etPh.text.toString()
    }
    
    private fun resetForm() {
        binding.etNitrogen.text?.clear()
        binding.etPhosphorus.text?.clear()
        binding.etPotassium.text?.clear()
        binding.etPh.text?.clear()
        binding.spinnerDistrict.setSelection(0)
        binding.switchManualInput.isChecked = false
        
        binding.cardWeatherData.visibility = View.GONE
        binding.tvSoilStatus.text = ""
        
        viewModel.resetInputs()
        
        showSnackbar("Form reset")
    }
    
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

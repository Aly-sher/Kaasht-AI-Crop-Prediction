package com.kaasht.croprecommendation.ui.soil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kaasht.croprecommendation.databinding.FragmentSoilSensorBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SoilSensorFragment : Fragment() {

    private var _binding: FragmentSoilSensorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SoilSensorViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // We need to create the layout file for this too, or use a placeholder
        // For now, assuming the binding will be generated if layout exists
        // But I haven't created fragment_soil_sensor.xml yet.
        // I will create a simple layout for it too.
        // Wait, binding generation depends on layout XML. 
        // If I use ViewBinding, I MUST have the XML.
        // I'll create the XML first or just use a simple View in onCreateView for now to avoid compilation error if binding class is missing.
        // Actually, to be safe, I'll create the layout too.
        
        // For now, let's just return a View and not use binding to avoid missing reference error if I don't create the layout in this turn.
        // But the user asked for nav_graph. 
        // I'll create the layout file in the next step or same step.
        
        return View(context) 
    }
}

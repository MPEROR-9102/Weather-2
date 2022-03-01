package com.example.weather.settings

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.weather.R
import com.example.weather.UnitSystem
import com.example.weather.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    val viewModel by viewModels<SettingsViewModel>()

    private var connectivity = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        binding.apply {
            settingsToolbar.setupWithNavController(findNavController())
            imperialButton.setBackgroundColor(Color.BLACK)
            metricButton.setBackgroundColor(Color.BLACK)

            viewModel.preferences.observe(viewLifecycleOwner) {
                when (it.unitSystem) {
                    UnitSystem.IMPERIAL -> imperialButton.setBackgroundColor(Color.RED)
                    UnitSystem.METRIC -> metricButton.setBackgroundColor(Color.RED)
                }
            }

            viewModel.connectivityLiveData.observe(viewLifecycleOwner) {
                connectivity = it
            }

            unitToggleButton.addOnButtonCheckedListener { toggleButton, checkId, isChecked ->
                if (isChecked && connectivity) {
                    var unitSystem = UnitSystem.IMPERIAL
                    when (checkId) {
                        R.id.imperialButton -> {
                            imperialButton.setBackgroundColor(Color.RED)
                            metricButton.setBackgroundColor(Color.BLACK)
                        }
                        R.id.metricButton -> {
                            metricButton.setBackgroundColor(Color.RED)
                            imperialButton.setBackgroundColor(Color.BLACK)
                            unitSystem = UnitSystem.METRIC
                        }
                    }
                    viewModel.onUnitToggleButtonClicked(unitSystem)
                }
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
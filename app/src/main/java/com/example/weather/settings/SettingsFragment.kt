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
import com.example.weather.SpeedUnit
import com.example.weather.TemperatureUnit
import com.example.weather.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    val viewModel by viewModels<SettingsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        binding.apply {
            settingsToolbar.setupWithNavController(findNavController())
            fahrenheitButton.setBackgroundColor(Color.BLACK)
            celsiusButton.setBackgroundColor(Color.BLACK)
            milesButton.setBackgroundColor(Color.BLACK)
            kilometerButton.setBackgroundColor(Color.BLACK)

            viewModel.preferences.observe(viewLifecycleOwner) {
                when (it.temperatureUnit) {
                    TemperatureUnit.FAHRENHEIT -> fahrenheitButton.setBackgroundColor(Color.RED)
                    TemperatureUnit.CELSIUS -> celsiusButton.setBackgroundColor(Color.RED)
                }
                when (it.speedUnit) {
                    SpeedUnit.MILES -> milesButton.setBackgroundColor(Color.RED)
                    SpeedUnit.KILOMETERS -> kilometerButton.setBackgroundColor(Color.RED)
                }
            }

            temperatureToggleButton.addOnButtonCheckedListener { toggleButton, checkId, isChecked ->
                if (isChecked) {
                    var temperatureUnit = TemperatureUnit.FAHRENHEIT
                    when (checkId) {
                        R.id.fahrenheitButton -> {
                            fahrenheitButton.setBackgroundColor(Color.RED)
                            celsiusButton.setBackgroundColor(Color.BLACK)
                        }
                        R.id.celsiusButton -> {
                            celsiusButton.setBackgroundColor(Color.RED)
                            fahrenheitButton.setBackgroundColor(Color.BLACK)
                            temperatureUnit = TemperatureUnit.CELSIUS
                        }
                    }
                    viewModel.onTemperatureToggleButtonClicked(temperatureUnit)
                }
            }

            speedToggleButton.addOnButtonCheckedListener { toggleButton, checkId, isChecked ->
                if (isChecked) {
                    var speedUnit = SpeedUnit.MILES
                    when (checkId) {
                        R.id.milesButton -> {
                            milesButton.setBackgroundColor(Color.RED)
                            kilometerButton.setBackgroundColor(Color.BLACK)
                        }
                        R.id.kilometerButton -> {
                            kilometerButton.setBackgroundColor(Color.RED)
                            milesButton.setBackgroundColor(Color.BLACK)
                            speedUnit = SpeedUnit.KILOMETERS
                        }
                    }
                    viewModel.onSpeedToggleButtonClicked(speedUnit)
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
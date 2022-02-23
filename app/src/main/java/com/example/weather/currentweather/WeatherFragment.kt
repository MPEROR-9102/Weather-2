package com.example.weather.currentweather

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.weather.R
import com.example.weather.api.Status
import com.example.weather.databinding.FragmentWeatherBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class WeatherFragment : Fragment(R.layout.fragment_weather) {

    private val viewModel by viewModels<WeatherViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentWeatherBinding.bind(view)

        runBlocking {
            binding.currentWeatherToolbar.title =
                viewModel.preferencesFlow.first().currentLocation.ifBlank {
                    resources.getString(R.string.app_name)
                }
        }

        binding.apply {
            viewModel.allLocation.observe(viewLifecycleOwner) {
                noCityDataLayout.isVisible = it.isNullOrEmpty()
            }
            viewModel.preferences.observe(viewLifecycleOwner) {
                scrollLayout.isNestedScrollingEnabled = it.currentLocation.isNotBlank()
                swipeToRefresh.isEnabled = it.currentLocation.isNotBlank()
            }

            currentWeatherToolbar.setOnMenuItemClickListener { item ->
                return@setOnMenuItemClickListener when (item.itemId) {
                    R.id.citiesAction -> {
                        viewModel.onCitiesActionClicked()
                        true
                    }
                    else -> false
                }
            }
            swipeToRefresh.apply {
                setDistanceToTriggerSync(750)
                setOnRefreshListener {
                    viewModel.onRefreshed()
                }
            }
        }

        setFragmentResultListener("currentLocation") { _, bundle ->
            val location = bundle.get("location") as String
            viewModel.onCurrentLocationReceived(location)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.weatherEvents.collect { event ->
                when (event) {
                    WeatherViewModel.WeatherForecastEvents.ShowCitiesScreen -> {
                        val action =
                            WeatherFragmentDirections.actionWeatherFragment2ToLocationFragment()
                        findNavController().navigate(action)
                    }
                    is WeatherViewModel.WeatherForecastEvents.ShowCurrentLoadingStatus -> {
                        when (event.status) {
                            Status.LOADING -> binding.swipeToRefresh.isRefreshing = true
                            Status.SUCCESS -> binding.swipeToRefresh.isRefreshing = false
                            Status.ERROR -> binding.swipeToRefresh.isRefreshing = false
                        }
                    }
                    is WeatherViewModel.WeatherForecastEvents.DisplayWeatherData -> {
                        Snackbar.make(
                            requireView(),
                            "${event.locationData.name} - ${event.locationData.main.temp}",
                            Snackbar.LENGTH_SHORT
                        ).setBackgroundTint(Color.WHITE)
                            .setTextColor(resources.getColor(R.color.red))
                            .show()
                    }
                    is WeatherViewModel.WeatherForecastEvents.ShowUnableToLoadMessage -> {
                        Snackbar.make(requireView(), event.message, Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(resources.getColor(R.color.red))
                            .setTextColor(Color.WHITE)
                            .show()
                    }
                }
            }
        }
    }
}
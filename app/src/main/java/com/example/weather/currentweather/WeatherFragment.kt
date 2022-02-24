package com.example.weather.currentweather

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.weather.*
import com.example.weather.api.Status
import com.example.weather.databinding.FragmentWeatherBinding
import com.example.weather.location.SendType
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class WeatherFragment : Fragment(R.layout.fragment_weather) {

    private val viewModel by viewModels<WeatherViewModel>()

    @Inject
    lateinit var weatherReceiver: WeatherReceiver

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentWeatherBinding.bind(view)

        binding.apply {
            viewModel.preferences.observe(viewLifecycleOwner) {
                noCityDataLayout.isVisible = it.currentLocation.isBlank()
                scrollLayout.isNestedScrollingEnabled = it.currentLocation.isNotBlank()
            }
            viewModel.connectivityNLocation.observe(viewLifecycleOwner) { (connectivity, location) ->
                currentWeatherToolbar.title = location.ifBlank {
                    resources.getString(R.string.app_name)
                }
                swipeToRefresh.isEnabled = connectivity && location.isNotBlank()
            }

            currentWeatherToolbar.setOnMenuItemClickListener { item ->
                return@setOnMenuItemClickListener when (item.itemId) {
                    R.id.citiesAction -> {
                        viewModel.onCitiesActionClicked()
                        true
                    }
                    R.id.settingsAction -> {
                        // Yet to add
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
            val sendType = bundle.get("sendType") as SendType
            viewModel.onCurrentLocationReceived(sendType, location)
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
                        binding.apply {
                            when (event.status) {
                                Status.LOADING -> swipeToRefresh.isRefreshing = true
                                Status.SUCCESS -> swipeToRefresh.isRefreshing = false
                                Status.ERROR -> swipeToRefresh.isRefreshing = false
                            }
                        }
                    }
                    is WeatherViewModel.WeatherForecastEvents.DisplayWeatherData -> {
                        Toast.makeText(requireContext(), event.weatherData.name, Toast.LENGTH_SHORT)
                            .show()
                    }
                    is WeatherViewModel.WeatherForecastEvents.ShowWeatherMessage -> {
                        when (event.type) {
                            SnackBarType.RED -> {
                                Snackbar.make(
                                    requireView(),
                                    event.message,
                                    Snackbar.LENGTH_SHORT
                                )
                                    .setBackgroundTint(resources.getColor(R.color.red))
                                    .setTextColor(Color.WHITE)
                                    .show()
                            }
                            SnackBarType.WHITE -> {
                                Snackbar.make(
                                    requireView(),
                                    event.message,
                                    Snackbar.LENGTH_SHORT
                                )
                                    .setBackgroundTint(Color.WHITE)
                                    .setTextColor(resources.getColor(R.color.red))
                                    .show()
                            }
                        }
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            coreActivity.collect { event ->
                when (event) {
                    is CoreActivityEvent.RequestWeatherData -> {
                        viewModel.onInitialLoadRequestReceived(event.location)
                    }
                }
            }
        }
    }
}
package com.example.weather.currentweather

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.weather.*
import com.example.weather.api.Status
import com.example.weather.databinding.FragmentWeatherBinding
import com.example.weather.location.SendType
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WeatherFragment : Fragment(R.layout.fragment_weather) {

    private val viewModel by viewModels<WeatherViewModel>()

    @Inject
    lateinit var weatherReceiver: WeatherReceiver

    @Inject
    lateinit var hourlyForecastAdapter: HourlyForecastAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentWeatherBinding.bind(view)

        binding.apply {
            viewModel.connectivityNLocation.observe(viewLifecycleOwner) { (connectivity, location) ->
                currentWeatherToolbar.title = location.ifBlank {
                    resources.getString(R.string.app_name)
                }
                noCityDataLayout.isVisible = location.isBlank()
                scrollLayout.isNestedScrollingEnabled = location.isNotBlank()
                swipeToRefresh.isEnabled = connectivity && location.isNotBlank()
                mainLayout.isVisible = connectivity && location.isNotBlank()
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

            swipeToRefresh.setOnRefreshListener {
                viewModel.onRefreshed()
            }

            hourlyForecastRecyclerView.apply {
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                adapter = hourlyForecastAdapter
            }

            viewModel.currentWeatherData.observe(viewLifecycleOwner) { weatherData ->
                val timeZone = weatherData.timezone
                val currentDate = weatherData.current.date

                weatherData.current.apply {
                    dateTextView.text = formatDate(date, timeZone)
                    timeTextView.text = formatTime(date, timeZone)

                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.preferencesFlow.first().apply {
                            tempTextView.text = formatTempDisplay(
                                temp,
                                this.temperatureUnit
                            )
                            windTextView.text = formatWindSpeedDisplay(
                                wind_speed,
                                this.speedUnit
                            )
                            visibilityTextView.text = formatVisibilityDisplay(
                                visibility.toFloat(),
                                this.speedUnit
                            )
                        }
                    }

                    mainTextView.text = weather[0].main
                    Glide.with(requireView())
                        .load(iconUrl(weather[0].icon))
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(iconImageView)

                    sunProgressBar.progress = getSunProgress(date, sunrise, sunset, timeZone)
                    sunriseTextView.text = formatTime(sunrise, timeZone)
                    sunsetTextView.text = formatTime(sunset, timeZone)

                    humidityTextView.text = String.format("%1$1d%2$%", humidity)
                    windText.text = String.format("%1$1s Wind", getDirection(wind_deg))
                    pressureTextView.text = String.format("%1$1d hPa", pressure)
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
                    is WeatherViewModel.WeatherForecastEvents.LoadHourlyForecastData -> {
                        hourlyForecastAdapter.submitList(event.hourlyDataList)
                    }
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
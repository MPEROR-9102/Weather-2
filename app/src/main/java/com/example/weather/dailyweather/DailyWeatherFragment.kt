package com.example.weather.dailyweather

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.weather.R
import com.example.weather.currentweather.WeatherViewModel
import com.example.weather.databinding.FragmentDailyWeatherBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class DailyWeatherFragment : Fragment() {

    private var _binding: FragmentDailyWeatherBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WeatherViewModel by activityViewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDailyWeatherBinding.inflate(inflater, container, false)

        binding.apply {
            dailyForecastToolbar.title = String.format(
                "%s's Daily Forecast",
                viewModel.connectivityNLocation.value?.location
            )

            dailyForecastToolbar.setOnMenuItemClickListener { menuItem ->
                return@setOnMenuItemClickListener when (menuItem.itemId) {
                    R.id.detailsInfoAction -> {
                        viewModel.onInfoActionClicked()
                        true
                    }
                    else -> false
                }
            }

            val dailyForecastAdapter = DailyForecastAdapter()
            dailyForecastRecyclerView.apply {
                layoutManager = GridLayoutManager(requireContext(), 2)
                adapter = dailyForecastAdapter
            }
            dailyForecastAdapter.submitList(viewModel.currentWeatherData.value?.daily?.map { dailyData ->
                val timezone = viewModel.currentWeatherData.value?.timezone
                DailyData(
                    timeZone = timezone ?: "",
                    date = dailyData.date,
                    temp = (dailyData.temp.max + dailyData.temp.min) / 2,
                    clouds = dailyData.clouds,
                    uvi = dailyData.uvi,
                    pop = dailyData.pop,
                    iconId = dailyData.weather[0].icon,
                    main = dailyData.weather[0].main
                )
            })
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.dailyForecastEvents.collect { event ->
                when (event) {
                    WeatherViewModel.DailyForecastEvents.ShowInfoScreen -> {
                        findNavController().navigate(
                            DailyWeatherFragmentDirections.actionGlobalInfoDialogFragment()
                        )
                    }
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
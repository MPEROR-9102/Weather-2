package com.example.weather.currentweather

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.weather.*
import com.example.weather.currentweatherdata.HourlyData
import com.example.weather.databinding.HourlyViewLayoutBinding
import com.example.weather.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
class HourlyForecastAdapter @Inject constructor(
    @ApplicationScope applicationScope: CoroutineScope,
    preferenceManager: PreferenceManager
) :
    ListAdapter<HourlyData, HourlyForecastAdapter.HourlyForecastViewHolder>(
        DIFF_CONFIG
    ) {

    lateinit var temperatureUnit: TemperatureUnit

    init {
        applicationScope.launch {
            temperatureUnit = preferenceManager.preferencesFlow.first().temperatureUnit
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyForecastViewHolder {
        val binding = HourlyViewLayoutBinding.inflate(LayoutInflater.from(parent.context))
        return HourlyForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HourlyForecastViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HourlyForecastViewHolder(private val binding: HourlyViewLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(hourlyData: HourlyData) {
            binding.apply {
                hourlyData.apply {
                    hourTextView.text = formatHourlyTime(currentDate, hourlyDate, timeZone)
                    Glide.with(binding.root)
                        .load(iconUrl(iconId))
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(iconImageView)
                    tempTextView.text = formatTempDisplay(hourlyData.temp, temperatureUnit)
                }
            }
        }
    }

    companion object {
        val DIFF_CONFIG = object : DiffUtil.ItemCallback<HourlyData>() {
            override fun areItemsTheSame(
                oldItem: HourlyData,
                newItem: HourlyData
            ): Boolean =
                oldItem === newItem

            override fun areContentsTheSame(
                oldItem: HourlyData,
                newItem: HourlyData
            ): Boolean =
                oldItem == newItem
        }
    }
}
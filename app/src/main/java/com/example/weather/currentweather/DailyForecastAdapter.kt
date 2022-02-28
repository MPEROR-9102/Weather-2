package com.example.weather.currentweather

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weather.currentweatherdata.DailyData
import com.example.weather.databinding.DailyViewLayoutBinding
import com.example.weather.formatDate
import com.example.weather.iconUrl
import javax.inject.Inject
import javax.inject.Singleton

@RequiresApi(Build.VERSION_CODES.O)
@Singleton
class DailyForecastAdapter @Inject constructor() :
    ListAdapter<DailyData, DailyForecastAdapter.DailyForecastViewHolder>(DIFF_UTIL) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyForecastViewHolder {
        val binding =
            DailyViewLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DailyForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyForecastViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DailyForecastViewHolder(private val binding: DailyViewLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(dailyData: DailyData) {
            binding.apply {
                dailyData.apply {
                    dateTextView.text = formatDate(date, timeZone)
                    Glide.with(binding.root)
                        .load(iconUrl(iconId))
                        .into(iconImageView)
                    descriptionTextView.text = main
                }
            }
        }
    }

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<DailyData>() {
            override fun areItemsTheSame(oldItem: DailyData, newItem: DailyData): Boolean =
                oldItem === newItem

            override fun areContentsTheSame(oldItem: DailyData, newItem: DailyData): Boolean =
                oldItem == newItem
        }
    }
}
package com.example.weather.dailyweather

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.weather.databinding.DailyViewLayoutBinding
import com.example.weather.getClock
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
                    dayTextView.text = getClock(date, timeZone).dayOfWeek.name
                        .lowercase()
                        .capitalize()
                    tempTextView.text = String.format("%.0f°", temp)

                    cloudsTextView.text = String.format("%d%s", clouds, "%")
                    uviTextView.text = String.format("UVI • %.2f", uvi)
                    popTextView.text = String.format("POP • %d%s", (pop * 100).toInt(), "%")

                    mainTextView.text = main
                    Glide.with(binding.root)
                        .load(iconUrl(iconId))
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(iconImageView)
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
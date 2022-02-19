package com.example.weather.location

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.weather.api.LocationResponse
import com.example.weather.databinding.LocationViewLayoutBinding

class LocationAdapter(
    private val itemListener: ItemListener
) : ListAdapter<LocationResponse, LocationAdapter.LocationViewHolder>(DIFF_CONFIG) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding =
            LocationViewLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LocationViewHolder(private val binding: LocationViewLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.apply {
                setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        itemListener.onItemClick(getItem(position))
                    }
                }

                setOnLongClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        itemListener.onItemLongClick(getItem(position))
                    }
                    return@setOnLongClickListener true
                }
            }
        }

        fun bind(location: LocationResponse) {
            binding.apply {
                Glide.with(root)
                    .load("http://openweathermap.org/img/wn/${location.weather[0].icon}@2x.png")
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(iconImageView)
                locationTextView.text = location.name
                tempTextView.text = String.format("%.0f°", location.main.temp)
                mainTextView.text = location.weather[0].main
            }
        }
    }

    interface ItemListener {
        fun onItemClick(location: LocationResponse)
        fun onItemLongClick(location: LocationResponse)
    }

    companion object {
        val DIFF_CONFIG = object : DiffUtil.ItemCallback<LocationResponse>() {
            override fun areItemsTheSame(
                oldItem: LocationResponse,
                newItem: LocationResponse
            ): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(
                oldItem: LocationResponse,
                newItem: LocationResponse
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
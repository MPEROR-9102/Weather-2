package com.example.weather.location

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.weather.database.Location
import com.example.weather.databinding.LocationViewLayoutBinding

class LocationAdapter(
    private val itemListener: ItemListener
) : ListAdapter<Location, LocationAdapter.LocationViewHolder>(DIFF_CONFIG) {

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

        fun bind(location: Location) {
            binding.apply {
                Glide.with(root)
                    .load("http://openweathermap.org/img/wn/${location.iconId}@2x.png")
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(iconImageView)
                locationTextView.text = location.cityName
                tempTextView.text = String.format("%.0f°", location.temp)
                mainTextView.text = location.main
            }
        }
    }

    interface ItemListener {
        fun onItemClick(location: Location)
        fun onItemLongClick(location: Location)
    }

    companion object {
        val DIFF_CONFIG = object : DiffUtil.ItemCallback<Location>() {
            override fun areItemsTheSame(
                oldItem: Location,
                newItem: Location
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: Location,
                newItem: Location
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
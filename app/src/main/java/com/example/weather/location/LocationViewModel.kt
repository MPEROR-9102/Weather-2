package com.example.weather.location

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.example.weather.api.Resource
import com.example.weather.data.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(weatherRepository: WeatherRepository) : ViewModel() {

    private val cityName = MutableLiveData("Seattle")
    val currentWeather = cityName.switchMap {
        liveData {
            emit(Resource.loading(data = null))
            try {
                emit(Resource.success(data = weatherRepository.currentWeather(it)))
            } catch (exception: Exception) {
                emit(
                    Resource.error(
                        data = null,
                        message = exception.message ?: "Error loading data"
                    )
                )
            }
        }
    }
}
package com.example.weather.location

import androidx.lifecycle.*
import com.example.weather.api.Resource
import com.example.weather.data.LocationRepository
import com.example.weather.database.Location
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val weatherRepository: LocationRepository
) : ViewModel() {

    private val cityName = MutableLiveData("Seattle")
    val locationData = weatherRepository.getAllLocations()

    fun insertLocation(cityName: String) {
        viewModelScope.launch(IO) {
            weatherRepository.insertLocation(Location(cityName))
        }
    }

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
package com.example.weather.location

import androidx.lifecycle.*
import com.example.weather.api.LocationResponse
import com.example.weather.api.Resource
import com.example.weather.database.Location
import com.example.weather.locationdata.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val weatherRepository: LocationRepository
) : ViewModel() {

    private val cityNameData: MutableLiveData<String> = MutableLiveData()
    val currentLocation = cityNameData.switchMap {
        liveData {
            emit(Resource.loading(data = null))
            try {
                emit(Resource.success(data = weatherRepository.currentLocation(it)))
            } catch (exception: Exception) {
                emit(
                    Resource.error(
                        data = null,
                        message = exception.message ?: "Error Loading Data"
                    )
                )
            }
        }
    }
    val allLocation = weatherRepository.getAllLocations()
    private val locationList: MutableLiveData<List<Location>> = MutableLiveData()
    val locationListData = locationList.switchMap { locations ->
        liveData {
            emit(Resource.loading(data = null))
            try {
                val locationData: MutableList<LocationResponse> = mutableListOf()
                for (location in locations)
                    locationData.add(weatherRepository.currentLocation(location.cityName))
                emit(Resource.success(data = locationData))
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

    fun loadCity(cityName: String) {
        cityNameData.postValue(cityName)
    }

    fun loadLocationList(locations: List<Location>) {
        locationList.postValue(locations)
    }

    fun insertLocation(cityName: String) {
        viewModelScope.launch(IO) {
            weatherRepository.insertLocation(Location(cityName))
        }
    }

    fun deleteLocation(cityName: String) {
        viewModelScope.launch(IO) {
            weatherRepository.deleteLocation(cityName)
        }
    }

    fun deleteAllLocation() {
        viewModelScope.launch(IO) {
            weatherRepository.deleteAllLocation()
        }
    }
}
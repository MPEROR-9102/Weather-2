package com.example.weather.location

import androidx.lifecycle.*
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
    val locationData = weatherRepository.getAllLocations()

    fun loadCity(cityName: String) {
        cityNameData.postValue(cityName)
    }

    fun insertLocation(cityName: String) {
        viewModelScope.launch(IO) {
            weatherRepository.insertLocation(Location(cityName))
        }
    }

    fun deleteLocation(location: Location) {
        viewModelScope.launch(IO) {
            weatherRepository.deleteLocation(location)
        }
    }

    fun deleteAllLocation() {
        viewModelScope.launch(IO) {
            weatherRepository.deleteAllLocation()
        }
    }
}
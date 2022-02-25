package com.example.weather.location

import androidx.lifecycle.*
import com.example.weather.*
import com.example.weather.api.ApiHelper
import com.example.weather.api.Status
import com.example.weather.database.Location
import com.example.weather.database.LocationDao
import com.example.weather.di.ApplicationScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "LocationViewModel"

enum class SendType {
    ADD, UPDATE
}

data class SendTypeModel(
    var type: SendType,
    var location: String
)

@HiltViewModel
class LocationViewModel @Inject constructor(
    @ApplicationScope private val applicationScope: CoroutineScope,
    weatherReceiver: WeatherReceiver,
    private val apiHelper: ApiHelper,
    private val preferenceManager: PreferenceManager,
    private val locationDao: LocationDao
) : ViewModel() {

    private val locationEventChannel = Channel<LocationEvent>()
    val locationEvent = locationEventChannel.receiveAsFlow()

    private val allLocationFlow = locationDao.getAll()
    val allLocation = allLocationFlow.asLiveData()

    private var sendType = SendTypeModel(SendType.UPDATE, "")

    val connection = weatherReceiver.noConnectivity.switchMap {
        liveData {
            emit(!it)
        }
    }

    init {
//        Log.e(TAG, "Init: LocationViewModel")
    }

    private fun showLocationMessage(type: SnackBarType, message: String) =
        viewModelScope.launch {
            locationEventChannel.send(
                LocationEvent.ShowLocationMessage(type, message)
            )
        }

    fun onDeleteAllActionClicked() {
        deleteAllConfirmation()
    }

    private fun deleteAllConfirmation() =
        viewModelScope.launch {
            if (allLocationFlow.first().isNullOrEmpty()) {
                showLocationMessage(SnackBarType.RED, "No Cities To Delete")
            } else {
                locationEventChannel.send(
                    LocationEvent.ShowDeleteAllConfirmationScreen(
                        "Delete All Cities?"
                    )
                )
            }
        }

    fun onDeleteAllConfirmationClicked() {
        deleteAllLocations()
    }

    private fun deleteAllLocations() =
        viewModelScope.launch {
            locationDao.deleteAll()
            preferenceManager.updateCurrentLocation("")
            showLocationMessage(SnackBarType.WHITE, "All Cities Deleted")
        }

    fun onAddLocationClicked() {
        showLocationEntryScreen()
    }

    private fun showLocationEntryScreen() =
        viewModelScope.launch {
            locationEventChannel.send(LocationEvent.ShowLocationEntryScreen)
        }

    fun onLocationEntered(location: String) {
        if (location.isNotBlank()) {
            loadAndInsertLocationData(location)
        } else {
            showLocationMessage(SnackBarType.RED, "No Entry Found")
        }
    }

    private fun loadAndInsertLocationData(location: String) =
        applicationScope.launch {
            locationEventChannel.send(LocationEvent.ShowCurrentLoadingStatus(Status.LOADING))
            try {
                val locationData = apiHelper.currentLocation(location)
                locationEventChannel.send(LocationEvent.ShowCurrentLoadingStatus(Status.SUCCESS))
                if (locationDao.insert(
                        Location(
                            locationData.name,
                            locationData.main.temp,
                            locationData.weather[0].main,
                            locationData.weather[0].icon
                        )
                    ) == -1L
                ) {
                    showLocationMessage(SnackBarType.RED, "City Already Exists")
                } else {
                    sendType.type = SendType.ADD
                    preferenceManager.updateCurrentLocation(locationData.name)
                    locationEventChannel.send(
                        LocationEvent.ShowCurrentWeatherScreen(
                            SendType.ADD,
                            locationData.name
                        )
                    )
                }
            } catch (exception: Exception) {
                locationEventChannel.send(LocationEvent.ShowCurrentLoadingStatus(Status.ERROR))
                when (exception.message) {
                    "HTTP 404 Not Found" -> showLocationMessage(SnackBarType.RED, "City Not Found")
                    "timeout" -> showLocationMessage(SnackBarType.RED, "Took Too Long To Respond")
                    else -> showLocationMessage(SnackBarType.RED, "Something Went Wrong")
                }
            }
        }

    fun onLocationClicked(location: String) {
        updateCurrentLocation(location)
    }

    private fun updateCurrentLocation(location: String) =
        applicationScope.launch {
            preferenceManager.updateCurrentLocation(location)
            locationEventChannel.send(
                LocationEvent.ShowCurrentWeatherScreen(
                    SendType.UPDATE,
                    location
                )
            )
        }

    fun onLocationLongClicked(location: Location) {
        deleteConfirmation(location)
    }

    private fun deleteConfirmation(location: Location) =
        viewModelScope.launch {
            locationEventChannel.send(
                LocationEvent.ShowDeleteConfirmationScreen(
                    "Delete ${location.cityName}?",
                    location
                )
            )
        }

    fun onDeleteConfirmationClicked(location: Location) {
        deleteLocation(location)
    }

    private fun deleteLocation(location: Location) =
        viewModelScope.launch {
            locationDao.delete(location)
            if (location.cityName == preferenceManager.preferencesFlow.first().currentLocation) {
//                Log.d(TAG, "deleteLocation: Same")
                val locationList = allLocationFlow.first()
                if (locationList.isNullOrEmpty()) {
//                    Log.d(TAG, "deleteLocation: 1")
                    sendType.location = ""
                    preferenceManager.updateCurrentLocation("")
                } else {
//                    Log.d(TAG, "deleteLocation: 2")
                    val alternateLocation = locationList[0].cityName
                    preferenceManager.updateCurrentLocation(alternateLocation)
                    sendType.location = alternateLocation
//                    Log.d(TAG, "deleteLocation: ${locationList[0].cityName}")
                }
            } else {
//                Log.d(TAG, "deleteLocation: Not Same")
            }
            showLocationMessage(SnackBarType.WHITE, "${location.cityName} Deleted")
        }

    sealed class LocationEvent {
        data class ShowDeleteAllConfirmationScreen(val title: String) : LocationEvent()
        object ShowLocationEntryScreen : LocationEvent()
        data class ShowLocationMessage(val type: SnackBarType, val message: String) :
            LocationEvent()

        data class ShowCurrentLoadingStatus(val status: Status) : LocationEvent()
        data class ShowCurrentWeatherScreen(val sendType: SendType, val location: String) :
            LocationEvent()

        data class ShowDeleteConfirmationScreen(val title: String, val location: Location) :
            LocationEvent()
    }

    override fun onCleared() {
        super.onCleared()
        applicationScope.launch {
            if (sendType.type == SendType.UPDATE && sendType.location.isNotBlank()) {
                coreActivityChannel.send(CoreActivityEvent.RequestWeatherData(sendType.location))
            }
        }
    }
}
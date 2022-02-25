package com.example.weather.initialrequest

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import com.example.weather.api.ApiHelper
import com.example.weather.database.LocationDao
import com.example.weather.di.ApplicationScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "WeatherForecastServiceO"

@AndroidEntryPoint
class WeatherForecastServiceOne : JobService() {

    companion object {
        const val LOCATION_DATA_REQUEST_JOB_ID = 123
    }

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var locationDao: LocationDao

    @Inject
    lateinit var apiHelper: ApiHelper

    private var cancelled = false

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "onStartJob: Job Started")
        doWork(params)
        return true
    }

    private fun doWork(params: JobParameters?) =
        applicationScope.launch {
            val locationList = locationDao.getAll().first()
            if (!locationList.isNullOrEmpty()) {
                try {
                    for (location in locationList) {
                        if (cancelled)
                            return@launch
                        val locationData = apiHelper.currentLocation(location.cityName)
                        locationDao.update(
                            location.copy(
                                temp = locationData.main.temp,
                                main = locationData.weather[0].main,
                                iconId = locationData.weather[0].icon
                            )
                        )
                    }
                } catch (exception: Exception) {
                    Log.d(TAG, "doWork: Error Loading Location List Data")
                }
            } else {
                Log.d(TAG, "doWork: No Location List Data")
            }
            Log.d(TAG, "doWork: Job Completed")
            jobFinished(params, false)
        }

    override fun onStopJob(params: JobParameters?): Boolean {
        cancelled = true
        Log.d(TAG, "onStopJob: Job Cancelled")
        return false
    }
}
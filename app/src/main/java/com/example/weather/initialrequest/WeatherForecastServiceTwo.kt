package com.example.weather.initialrequest

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import com.example.weather.CoreActivityEvent
import com.example.weather.PreferenceManager
import com.example.weather.coreActivityChannel
import com.example.weather.di.ApplicationScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "WeatherForecastServiceT"

@AndroidEntryPoint
class WeatherForecastServiceTwo : JobService() {

    companion object {
        const val WEATHER_DATA_REQUEST_JOB_ID = 234
    }

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "onStartJob: Job Started")
        doWork(params)
        return true
    }

    private fun doWork(params: JobParameters?) =
        applicationScope.launch {
            val currentLocation = preferenceManager.preferencesFlow.first().currentLocation
            if (currentLocation.isNotBlank()) {
                coreActivityChannel.send(CoreActivityEvent.RequestWeatherData(currentLocation))
            } else {
                Log.d(TAG, "doWork: No Location Data")
            }
            Log.d(TAG, "doWork: Job Completed")
            jobFinished(params, false)
        }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "onStopJob: Job Cancelled")
        return false
    }
}
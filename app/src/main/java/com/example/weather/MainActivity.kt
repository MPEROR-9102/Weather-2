package com.example.weather

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.weather.datarequest.WeatherForecastServiceOne
import com.example.weather.datarequest.WeatherForecastServiceTwo
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

private const val TAG = "MainActivity"

val coreActivityChannel = Channel<CoreActivityEvent>()
val coreActivity = coreActivityChannel.receiveAsFlow()

sealed class CoreActivityEvent {
    data class RequestWeatherData(val location: String) : CoreActivityEvent()
}

enum class SnackBarType {
    RED, WHITE
}

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var weatherReceiver: WeatherReceiver
    private lateinit var jobScheduler: JobScheduler
    private lateinit var jobInfoOne: JobInfo.Builder
    private lateinit var jobInfoTwo: JobInfo.Builder
    private lateinit var connectivitySnackBar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobInfoOne = JobInfo.Builder(
            WeatherForecastServiceOne.LOCATION_DATA_REQUEST_JOB_ID,
            ComponentName(this, WeatherForecastServiceOne::class.java)
        )
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        jobInfoTwo = JobInfo.Builder(
            WeatherForecastServiceTwo.WEATHER_DATA_REQUEST_JOB_ID,
            ComponentName(this, WeatherForecastServiceTwo::class.java)
        )
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)

        val resultCode = jobScheduler.schedule(jobInfoTwo.build())
        if (resultCode == 1)
            Log.d(TAG, "onCreate: Job T Scheduled")
        else
            Log.d(TAG, "onCreate: Job T Scheduling Failed")

        connectivitySnackBar = Snackbar
            .make(
                findViewById(R.id.parentLayout),
                "No Internet Connection",
                Snackbar.LENGTH_INDEFINITE
            )
            .setBackgroundTint(resources.getColor(R.color.red))
            .setTextColor(Color.WHITE)
        weatherReceiver.noConnectivity.observe(this) {
            when (it) {
                true -> connectivitySnackBar.show()
                false -> connectivitySnackBar.dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(weatherReceiver, intentFilter)

        val resultCode = jobScheduler.schedule(jobInfoOne.build())
        if (resultCode == 1)
            Log.d(TAG, "onCreate: Job O Scheduled")
        else
            Log.d(TAG, "onCreate: Job O Scheduling Failed")
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(weatherReceiver)
        jobScheduler.cancel(WeatherForecastServiceOne.LOCATION_DATA_REQUEST_JOB_ID)
        Log.d(TAG, "onStop: Scheduled Job O Cancelled")
    }

    override fun onDestroy() {
        super.onDestroy()
        jobScheduler.cancel(WeatherForecastServiceTwo.WEATHER_DATA_REQUEST_JOB_ID)
        Log.d(TAG, "onDestroy: Scheduled Job T Cancelled")
    }
}
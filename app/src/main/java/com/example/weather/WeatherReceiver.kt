package com.example.weather

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherReceiver @Inject constructor() : BroadcastReceiver() {

    private val _noConnectivity: MutableLiveData<Boolean> = MutableLiveData()
    val noConnectivity: LiveData<Boolean> = _noConnectivity

    override fun onReceive(context: Context?, intent: Intent?) {
        _noConnectivity.postValue(
            intent?.getBooleanExtra(
                ConnectivityManager.EXTRA_NO_CONNECTIVITY,
                false
            )
        )
    }
}
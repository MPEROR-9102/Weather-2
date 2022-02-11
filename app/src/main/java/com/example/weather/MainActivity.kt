package com.example.weather

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val weatherReceiver = WeatherReceiver()
    private lateinit var connectivitySnackBar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectivitySnackBar = Snackbar
            .make(
                findViewById(R.id.parentLayout),
                "No Internet Connection",
                Snackbar.LENGTH_INDEFINITE
            )
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(weatherReceiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(weatherReceiver)
    }

    inner class WeatherReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val noConnectivity =
                intent?.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)

            if (noConnectivity == true) {
                connectivitySnackBar.show()
                findViewById<Button>(R.id.addLocationButton).isVisible = false
            } else {
                connectivitySnackBar.dismiss()
                findViewById<Button>(R.id.addLocationButton).isVisible = true
            }
        }
    }
}
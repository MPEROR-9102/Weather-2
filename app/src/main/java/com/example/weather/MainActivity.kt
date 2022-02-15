package com.example.weather

import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var weatherReceiver: WeatherReceiver
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
            .setBackgroundTint(resources.getColor(R.color.red))
            .setTextColor(Color.WHITE)

        weatherReceiver.noConnectivity.observe(this) {
            when (it) {
                true -> {
                    connectivitySnackBar.show()
                }
                false -> {
                    connectivitySnackBar.dismiss()
                }
            }
        }
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
}
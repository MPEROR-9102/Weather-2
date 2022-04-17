package com.example.weather.dailyweather.info

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.weather.databinding.InfoLayoutBinding

class InfoDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val infoView = InfoLayoutBinding.inflate(layoutInflater)
        infoView.apply {
            cloudinessTextView.text = "•  Cloudiness (in %)"
            uviTextView.text = "UVI  •  UV Index"
            popTextView.text = "POP  •  Probability of Precipitation (in %)"
        }
        return AlertDialog.Builder(requireContext())
            .setView(infoView.root)
            .setTitle("Info")
            .setPositiveButton("Got It", null)
            .create()
    }
}

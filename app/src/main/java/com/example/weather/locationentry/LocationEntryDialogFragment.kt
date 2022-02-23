package com.example.weather.locationentry

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.example.weather.databinding.EdittextLayoutBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocationEntryDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val cityView = EdittextLayoutBinding.inflate(layoutInflater, null, false)
        return AlertDialog.Builder(requireContext())
            .setTitle("Insert A City")
            .setView(cityView.root)
            .setPositiveButton("Add") { _, _ ->
                setFragmentResult(
                    "locationEntry",
                    bundleOf("addedLocation" to cityView.cityEditText.text.toString())
                )
            }
            .setNeutralButton("Cancel", null)
            .create()
    }
}
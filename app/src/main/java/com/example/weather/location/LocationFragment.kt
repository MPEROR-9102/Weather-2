package com.example.weather.location

import android.app.AlertDialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.R
import com.example.weather.api.Status
import com.example.weather.databinding.EdittextLayoutBinding
import com.example.weather.databinding.FragmentLocationBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocationFragment : Fragment() {

    private var _binding: FragmentLocationBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<LocationViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLocationBinding.inflate(inflater, container, false)

        binding.locationToolbar.apply {
            setupWithNavController(findNavController())
            setOnMenuItemClickListener {
                return@setOnMenuItemClickListener when (it.itemId) {
                    R.id.locationMenuDeleteAll -> {
                        if (!viewModel.allLocation.value.isNullOrEmpty())
                            showDeleteAllDialog()
                        else
                            Toast.makeText(
                                requireContext(),
                                "No Cities To Delete",
                                Toast.LENGTH_SHORT
                            ).show()
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        }

        val locationAdapter = LocationAdapter { location ->
            showDeleteDialog(location.name)
        }
        binding.apply {
            locationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            val itemDecoration = DividerItemDecoration(requireContext(), LinearLayout.VERTICAL)
            itemDecoration.setDrawable(ColorDrawable(requireContext().resources.getColor(R.color.white)))
            locationsRecyclerView.addItemDecoration(itemDecoration)
            locationsRecyclerView.adapter = locationAdapter

            addLocationButton.setOnClickListener {
                showLocationEntryDialog()
            }
        }

        viewModel.currentLocation.observe(viewLifecycleOwner) {
            it.let { resource ->
                when (resource.status) {
                    Status.LOADING -> { /* Initial load status */
                    }
                    Status.SUCCESS -> {
                        viewModel.insertLocation(resource.data!!.name)
                    }
                    Status.ERROR -> {
                        Snackbar
                            .make(
                                binding.addLocationButton,
                                "Location Not Found",
                                Snackbar.LENGTH_SHORT
                            )
                            .show()
                    }
                }
            }
        }

        viewModel.allLocation.observe(viewLifecycleOwner) { locationList ->
            binding.apply {
                if (locationList.isNotEmpty()) {
                    noCityDataTextView.isVisible = false
                    viewModel.loadLocationList(locationList)
                } else {
                    noCityDataTextView.isVisible = true
                    locationsRecyclerView.isVisible = false
                }
            }
        }

        viewModel.locationListData.observe(viewLifecycleOwner) {
            it?.let { resource ->
                binding.progressBar.apply {
                    when (resource.status) {
                        Status.LOADING -> {
                            isVisible = true
                        }
                        Status.SUCCESS -> {
                            isVisible = false
                            binding.locationsRecyclerView.isVisible = true
                            locationAdapter.submitList(resource.data)
                        }
                        Status.ERROR -> {
                            isVisible = false
                            binding.locationsRecyclerView.isVisible = false
                        }
                    }
                }
            }
        }

        return binding.root
    }

    private fun showLocationEntryDialog() {
        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Insert a City")
        val cityView = EdittextLayoutBinding.inflate(
            LayoutInflater.from(requireContext()),
            null, false
        )
        builder.setView(cityView.root)
            .setPositiveButton("Add") { _, _ ->
                if (cityView.cityEditText.text.isEmpty())
                    Toast.makeText(requireContext(), "No Entry Found!", Toast.LENGTH_SHORT).show()
                else {
                    viewModel.loadCity(cityView.cityEditText.text.toString())
                }
            }
            .setNeutralButton("Cancel") { _, _ -> }
            .show()
    }

    private fun showDeleteDialog(cityName: String) {
        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Delete $cityName?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteLocation(cityName)
            }
            .setNeutralButton("Cancel") { _, _ -> }
        builder.show()
    }

    private fun showDeleteAllDialog() {
        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Delete All Cities?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteAllLocation()
            }
            .setNeutralButton("Cancel") { _, _ -> }
        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
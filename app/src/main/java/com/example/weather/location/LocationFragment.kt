package com.example.weather.location

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.R
import com.example.weather.WeatherReceiver
import com.example.weather.api.LocationResponse
import com.example.weather.api.Status
import com.example.weather.databinding.EdittextLayoutBinding
import com.example.weather.databinding.FragmentLocationBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LocationFragment : Fragment(), LocationAdapter.ItemListener {

    private var _binding: FragmentLocationBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var weatherReceiver: WeatherReceiver
    private val viewModel by viewModels<LocationViewModel>()
    private lateinit var commonSnackBarWhite: Snackbar
    private lateinit var commonSnackBarRed: Snackbar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLocationBinding.inflate(inflater, container, false)

        binding.locationToolbar.apply {
            setupWithNavController(findNavController())
            setOnMenuItemClickListener {
                return@setOnMenuItemClickListener when (it.itemId) {
                    R.id.deleteAllAction -> {
                        if (!viewModel.allLocation.value.isNullOrEmpty()) {
                            showDeleteAllDialog()
                        } else {
                            commonSnackBarRed.setText("No Cities To Delete").show()
                        }
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        }

        val locationAdapter = LocationAdapter(this)
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
                binding.apply {
                    when (resource.status) {
                        Status.LOADING -> {
                            progressBar.isVisible = true
                        }
                        Status.SUCCESS -> {
                            viewModel.insertLocation(resource.data!!.name)
                                .observe(viewLifecycleOwner) { rowExists ->
                                    if (rowExists == -1L) {
                                        progressBar.isVisible = false
                                        commonSnackBarRed.setText("City Already Exists").show()
                                    } else {
                                        commonSnackBarWhite.setText("${resource.data.name} Added")
                                            .show()
                                    }
                                }
                        }
                        Status.ERROR -> {
                            progressBar.isVisible = false
                            var statusInfo = "City Not Found"
                            if (resource.message != "HTTP 404 Not Found") {
                                statusInfo = "Error Loading Cities"
                            }
                            commonSnackBarRed.setText(statusInfo).show()
                        }
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

        weatherReceiver.noConnectivity.observe(viewLifecycleOwner) {
            when (it) {
                true -> {
                    binding.addLocationButton.isVisible = false
                }
                false -> {
                    if (!binding.locationsRecyclerView.isVisible &&
                        !viewModel.allLocation.value.isNullOrEmpty()
                    ) {
                        viewModel.loadLocationList(viewModel.allLocation.value!!)
                    }
                    binding.addLocationButton.isVisible = true
                }
            }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        commonSnackBarWhite = Snackbar.make(requireView(), "", Snackbar.LENGTH_SHORT)
            .setBackgroundTint(Color.WHITE)
            .setTextColor(resources.getColor(R.color.red))
        commonSnackBarRed = Snackbar.make(requireView(), "", Snackbar.LENGTH_SHORT)
            .setBackgroundTint(resources.getColor(R.color.red))
            .setTextColor(Color.WHITE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(location: LocationResponse) {
        viewModel.onLocationSelected(location.name)
        setFragmentResult(
            "currentLocation",
            bundleOf("location" to location.name)
        )
        findNavController().popBackStack()
    }

    override fun onItemLongClick(location: LocationResponse) {
        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Delete ${location.name}?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteLocation(location.name)
                commonSnackBarWhite.setText("${location.name} Deleted").show()
            }
            .setNeutralButton("Cancel") { _, _ -> }
        builder.show()
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
                if (cityView.cityEditText.text.isEmpty()) {
                    commonSnackBarRed.setText("No Entry Found").show()
                } else {
                    viewModel.loadCity(cityView.cityEditText.text.toString())
                }
            }
            .setNeutralButton("Cancel") { _, _ -> }
            .show()
    }

    private fun showDeleteAllDialog() {
        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Delete All Cities?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteAllLocation()
                commonSnackBarWhite.setText("All Cities Deleted").show()
            }
            .setNeutralButton("Cancel") { _, _ -> }
        builder.show()
    }
}
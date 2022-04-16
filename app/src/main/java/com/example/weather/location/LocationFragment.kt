package com.example.weather.location

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.R
import com.example.weather.SnackBarType
import com.example.weather.api.Status
import com.example.weather.database.Location
import com.example.weather.databinding.FragmentLocationBinding
import com.example.weather.exhaustive
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class LocationFragment : Fragment(), LocationAdapter.ItemListener {

    private var _binding: FragmentLocationBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<LocationViewModel>()

    private var connectivity: Boolean = false
    private lateinit var locationAdapter: LocationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        activity?.findViewById<BottomNavigationView>(R.id.bottomNavBar)?.visibility =
            BottomNavigationView.GONE

        binding.apply {
            viewModel.connection.observe(viewLifecycleOwner) {
                connectivity = it
                addLocationButton.isVisible = it
            }

            locationToolbar.apply {
                setupWithNavController(findNavController())
                setOnMenuItemClickListener {
                    return@setOnMenuItemClickListener when (it.itemId) {
                        R.id.deleteAllAction -> {
                            viewModel.onDeleteAllActionClicked()
                            true
                        }
                        else -> false
                    }.exhaustive
                }
            }

            locationAdapter = LocationAdapter(this@LocationFragment)
            locationsRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = locationAdapter
                val itemDecoration = DividerItemDecoration(requireContext(), LinearLayout.VERTICAL)
                itemDecoration.setDrawable(ColorDrawable(requireContext().resources.getColor(R.color.white)))
                addItemDecoration(itemDecoration)
            }

            addLocationButton.setOnClickListener {
                viewModel.onAddLocationClicked()
            }

            viewModel.allLocation.observe(viewLifecycleOwner) {
                if (it.isNullOrEmpty()) {
                    noCityDataLayout.isVisible = true
                    locationsRecyclerView.isVisible = false
                } else {
                    noCityDataLayout.isVisible = false
                    locationAdapter.submitList(it)
                    binding.locationsRecyclerView.isVisible = true
                }
            }
        }

        setFragmentResultListener("locationEntry") { _, bundle ->
            val location = bundle.get("addedLocation") as String
            viewModel.onLocationEntered(location)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.locationEvent.collect { event ->
                when (event) {
                    is LocationViewModel.LocationEvent.ShowDeleteAllConfirmationScreen -> {
                        AlertDialog.Builder(requireContext())
                            .setTitle(event.title)
                            .setPositiveButton("Delete") { _, _ ->
                                viewModel.onDeleteAllConfirmationClicked()
                            }
                            .setNeutralButton("Cancel", null)
                            .show()
                    }
                    LocationViewModel.LocationEvent.ShowLocationEntryScreen -> {
                        val action =
                            LocationFragmentDirections.actionGlobalLocationEntryDialogFragment()
                        findNavController().navigate(action)
                    }
                    is LocationViewModel.LocationEvent.ShowLocationMessage -> {
                        when (event.type) {
                            SnackBarType.RED -> {
                                Snackbar.make(
                                    requireView(),
                                    event.message,
                                    Snackbar.LENGTH_SHORT
                                )
                                    .setBackgroundTint(resources.getColor(R.color.red))
                                    .setTextColor(Color.WHITE)
                                    .show()
                            }
                            SnackBarType.WHITE -> {
                                Snackbar.make(
                                    requireView(),
                                    event.message,
                                    Snackbar.LENGTH_SHORT
                                )
                                    .setBackgroundTint(Color.WHITE)
                                    .setTextColor(resources.getColor(R.color.red))
                                    .show()
                            }
                        }
                    }
                    is LocationViewModel.LocationEvent.ShowCurrentLoadingStatus -> {
                        binding.apply {
                            when (event.status) {
                                Status.LOADING -> progressBar.isVisible = true
                                Status.SUCCESS -> progressBar.isVisible = false
                                Status.ERROR -> progressBar.isVisible = false
                            }
                        }
                    }
                    is LocationViewModel.LocationEvent.ShowCurrentWeatherScreen -> {
                        setFragmentResult(
                            "currentLocation",
                            bundleOf("location" to event.location, "sendType" to event.sendType)
                        )
                        findNavController().popBackStack()
                    }
                    is LocationViewModel.LocationEvent.ShowDeleteConfirmationScreen -> {
                        AlertDialog.Builder(requireContext())
                            .setTitle(event.title)
                            .setPositiveButton("Delete") { _, _ ->
                                viewModel.onDeleteConfirmationClicked(event.location)
                            }
                            .setNeutralButton("Cancel", null)
                            .show()
                    }
                }
            }
        }

        return binding.root
    }

    override fun onItemClick(location: Location) {
        if (connectivity) {
            viewModel.onLocationClicked(location.cityName)
        }
    }

    override fun onItemLongClick(location: Location) {
        viewModel.onLocationLongClicked(location)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

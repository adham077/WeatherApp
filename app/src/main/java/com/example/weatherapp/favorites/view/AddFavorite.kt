package com.example.weatherapp.favorites.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.weatherapp.databinding.FragmentAddFavoriteBinding
import com.example.weatherapp.favorites.viewmodel.FavoriteViewModel
import com.example.weatherapp.favorites.viewmodel.FavoriteViewModelFactory
import com.example.weatherapp.home.viewmodel.HomeViewModel
import com.example.weatherapp.home.viewmodel.HomeViewModelFactory
import com.example.weatherapp.model.data.source.local.weather.WeatherLocalDataSource
import com.example.weatherapp.model.data.source.remote.weather.WeatherRemoteDataSource
import com.example.weatherapp.model.repository.weather.WeatherRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.snackbar.Snackbar
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

private typealias locationRes_t = (success: Boolean,lat: Double,long: Double) -> Unit

class AddFavorite : Fragment() {
    private  val mapView: MapView by lazy {
        binding.mapView
    }
    private lateinit var mapController: MapController
    private lateinit var binding : FragmentAddFavoriteBinding
    private var selectedLocation: GeoPoint? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var viewModel: FavoriteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val factory = FavoriteViewModelFactory(
            WeatherRepository.getInstance(
                WeatherLocalDataSource(requireContext()),
                WeatherRemoteDataSource(requireContext())
            )
        )
        viewModel = ViewModelProvider(this, factory)[FavoriteViewModel::class.java]

        if(checkPermissions()){
            getLocation{success, lat, long -> Unit
                if(success){
                    setupMap(lat, long)
                }
                else{
                    setupMap()
                }
            }
        }
        else{
            setupMap()
        }

        binding.btnSave.setOnClickListener{
            if(selectedLocation != null){
                val lat = selectedLocation!!.latitude
                val long =  selectedLocation!!.longitude
                Log.i("AddFavorite", "onViewCreated: lat = $lat, long = $long")
                viewModel.saveLocationWeather(
                    lat,
                    long
                )

                viewModel.saveLocationLiveData.observe(viewLifecycleOwner) {result->
                    if(result){
                        val action = AddFavoriteDirections.actionAddFavoriteFragmentToFavoriteFragment(senderID = "AddFavorite")
                        findNavController().navigate(action)
                        Toast.makeText(requireContext(), "Added to favorites successfully.", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(requireContext(), "Failed to add to Favorites.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }

        viewModel.saveLocationLiveData.observe(viewLifecycleOwner) {
            if(it){

            } else {
                Toast.makeText(
                    requireContext(),
                    "Failed to save location",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun setupMap(lat: Double = 29.991385, long: Double = 31.268972) {
        if (::mapController.isInitialized) return

        Configuration.getInstance().userAgentValue = requireContext().packageName
        mapController = mapView.controller as MapController
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapController.setZoom(11.5)
        mapController.setCenter(GeoPoint(lat, long))

        if (mapView.overlays.none { it is LongPressOverlay }) {
            mapView.overlays.add(LongPressOverlay())
        }
    }

    inner class LongPressOverlay : Overlay() {
        override fun onLongPress(event: MotionEvent, mapView: MapView?): Boolean {
            val point = mapView?.projection?.fromPixels(event.x.toInt(), event.y.toInt()) as? GeoPoint
            point?.let {
                selectedLocation = it
                mapView.overlays
                    .filterIsInstance<Marker>()
                    .forEach { mapView.overlays.remove(it) }

                Marker(mapView).apply {
                    position = it
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mapView.overlays.add(this)
                }
                mapController.animateTo(it)
            }
            return true
        }

    }

    private fun searchLocation(){

    }


    private  fun checkPermissions() : Boolean {
        var result = false
        if ((ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
              ||
            (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
             ) == PackageManager.PERMISSION_GRANTED))
         {
            result = true
        }
        return result
    }


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getLocation(callBack: locationRes_t?) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                fusedLocationProviderClient.removeLocationUpdates(this)
                val location = locationResult.lastLocation
                if (location != null) {
                    callBack?.invoke(true, location.latitude, location.longitude)
                } else {
                    callBack?.invoke(false, 0.0, 0.0)
                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }


}
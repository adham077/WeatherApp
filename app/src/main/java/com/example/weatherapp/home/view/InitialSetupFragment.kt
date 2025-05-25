package com.example.weatherapp.home.view

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.example.weatherapp.databinding.DialogMapSelectorBinding
import com.example.weatherapp.databinding.FragmentInitialSetupBinding
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

private typealias _onLocationDenied_t = () -> Unit
private typealias _onLocationGranted_t = () -> Unit
private typealias _locationRes_t = (success: Boolean,lat: Double,long: Double) -> Unit


class InitialSetupFragment : Fragment() {
    private lateinit var binding: FragmentInitialSetupBinding
    private  var selectedLocation : GeoPoint? = null
    private lateinit var sharedPreferences : SharedPreferences

    private lateinit var onLocationGranted: _onLocationGranted_t
    private lateinit var onLocationDenied: _onLocationDenied_t

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[ACCESS_COARSE_LOCATION] == true

        if (fineGranted || coarseGranted) {
            onLocationGranted()
        } else {
            onLocationDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentInitialSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("WeatherAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        if(sharedPreferences.getBoolean("isInitRun", false)){
            if(sharedPreferences.getBoolean("userPrefGps",false)){
                if(checkLocationPermission()){
                    getLocation {success,lat,long->
                        if(success){
                            editor.putFloat("latVal", lat.toFloat())
                            editor.putFloat("lonVal", long.toFloat())
                            editor.apply()
                            navigateToHomeFragment(true, lat, long)
                        }
                        else{
                            Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else{
                    requestLocationPermission(
                        {
                            getLocation {success,lat,long->
                                if(success){
                                    editor.putFloat("latVal", lat.toFloat())
                                    editor.putFloat("lonVal", long.toFloat())
                                    editor.apply()
                                    navigateToHomeFragment(true, lat, long)
                                }
                                else{
                                    Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        {
                            /*Do nothing*/
                        }
                    )
                }
            }
            else{
                navigateToHomeFragment(
                    false,
                    sharedPreferences.getFloat("userLat", 29.991385f).toDouble(),
                    sharedPreferences.getFloat("userLon", 31.268972f).toDouble()
                )
            }
        }

        binding.btnUseGps.setOnClickListener {
            if(checkLocationPermission()){
                editor.putBoolean("isInitRun", true)
                editor.putBoolean("userPrefGps",true)
                editor.apply()
                getLocation { success, lat, long ->
                    if(success){
                        editor.putFloat("userLat", lat.toFloat())
                        editor.putFloat("userLat", long.toFloat())
                        editor.apply()
                        navigateToHomeFragment(true, lat, long)
                    }
                    else{
                        Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else{
                val onGranted = {
                    editor.putBoolean("isInitRun", true)
                    editor.putBoolean("userPrefGps",true)

                    editor.apply()
                    getLocation { success, lat, long ->
                        if(success){
                            editor.putFloat("latVal", lat.toFloat())
                            editor.putFloat("lonVal", long.toFloat())
                            editor.apply()
                            navigateToHomeFragment(true, lat, long)
                        }
                        else{
                            Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                val onDenied = {
                    /*Do nothing*/
                }

                requestLocationPermission(onGranted, onDenied)
            }
        }

        binding.btnChooseMap.setOnClickListener {
            showMapSelectorDial()
        }
    }

    private fun checkLocationPermission() : Boolean{
        var result = false
        if ((ContextCompat.checkSelfPermission(requireContext(),
                ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
            ||
            (ContextCompat.checkSelfPermission(requireContext(),
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED))
        {
            result = true
        }
        return result
    }

    private fun requestLocationPermission(
        onLocationGranted : _onLocationGranted_t?,
        onLocationDenied : _onLocationDenied_t?
    ) {
        if (onLocationGranted != null) {
            this.onLocationGranted = onLocationGranted
        }
        if (onLocationDenied != null) {
            this.onLocationDenied = onLocationDenied
        }
        locationPermissionLauncher.launch(
            arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
        )
    }

    private fun navigateToHomeFragment(fromGps: Boolean,lat : Double, lon : Double) {
        val action = InitialSetupFragmentDirections.actionInitialSetupFragmentToHomeFragment("InitialSetupFragment",fromGps, lat.toFloat(), lon.toFloat())
        view?.findNavController()?.navigate(action)
    }

    private fun setupMap(lat : Double = 29.991385 , long : Double = 31.268972, mapView: MapView ) {
        Configuration.getInstance().userAgentValue = requireContext().packageName
        val mapController =  mapView.controller as MapController
        mapView.setMultiTouchControls(true)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapController.setZoom(11.5)
        val startPoint = GeoPoint(lat, long)
        mapController.setCenter(startPoint)

        val longPressOverlay = object : Overlay() {
            override fun onLongPress(event: MotionEvent, mapView: MapView?): Boolean {
                val point = mapView?.projection?.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                selectedLocation = point
                val overlays = mapView.overlays.toMutableList().apply {
                    removeAll { it is Marker }
                }
                val marker = Marker(mapView).apply {
                    position = point
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                overlays.add(marker)
                mapView.overlays.clear()
                mapView.overlays.addAll(overlays)
                mapController.animateTo(point)
                mapView.invalidate()
                return true
            }
        }

        mapView.overlays.add(longPressOverlay)
    }

    private fun showMapSelectorDial(){
        val diaBinding = DialogMapSelectorBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = Dialog(requireContext()).apply {
            setContentView(diaBinding.root)
            window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            window?.setBackgroundDrawableResource(android.R.color.transparent)

        }
        val mapView = diaBinding.mapView
        setupMap(mapView = mapView)
        diaBinding.btnClose.setOnClickListener {
            dialog.dismiss()
        }
        diaBinding.btnSelect.setOnClickListener {
            if(selectedLocation != null){
                navigateToHomeFragment(false, selectedLocation!!.latitude, selectedLocation!!.longitude)
                sharedPreferences.edit().apply {
                    putBoolean("isInitRun", true)
                    putBoolean("userPrefGps", false)
                    putFloat("userLat", selectedLocation!!.latitude.toFloat())
                    putFloat("userLon", selectedLocation!!.longitude.toFloat())
                    apply()
                }
                dialog.dismiss()
            }
            else {
                Toast.makeText(
                    requireContext(),
                    "Please select a location on the map",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        dialog.show()
    }

    @RequiresPermission(allOf = [ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION])
    private fun getLocation(callBack : _locationRes_t?) {

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val locationRequest = LocationRequest.Builder(0).apply {
            setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        }.build()

        val locationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.lastLocation
                if (location != null) {
                    if (callBack != null) callBack(true, location.latitude, location.longitude)
                } else {
                    if (callBack != null) callBack(false, 0.0, 0.0)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }
}
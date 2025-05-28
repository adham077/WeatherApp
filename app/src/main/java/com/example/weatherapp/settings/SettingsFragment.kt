package com.example.weatherapp.settings

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.example.weatherapp.databinding.DialogMapSelectorBinding
import com.example.weatherapp.databinding.FragmentSettingsBinding
import com.example.weatherapp.home.view._locationRes_t
import com.example.weatherapp.home.view._onLocationDenied_t
import com.example.weatherapp.home.view._onLocationGranted_t
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
import androidx.core.content.edit
import com.example.weatherapp.MainActivity
import com.example.weatherapp.utils.languagesMap
import com.example.weatherapp.utils.updateLocale

private typealias settings_onLocationDenied_t = () -> Unit
private typealias settings_onLocationGranted_t = () -> Unit
private typealias settings_locationRes_t = (success: Boolean,lat: Double,long: Double) -> Unit

class SettingsFragment : Fragment() {

    private lateinit var binding : FragmentSettingsBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var onLocationGranted: settings_onLocationGranted_t
    private lateinit var onLocationDenied: settings_onLocationDenied_t
    private  var selectedLocation : GeoPoint? = null
    private var initializingLanguageSpinner = true



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
        sharedPreferences = requireActivity().getSharedPreferences("WeatherAppPrefs", 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.switchGps.isChecked = sharedPreferences.getBoolean("userPrefGps", false)

        binding.switchGps.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                if(checkLocationPermission()){
                    getLocation {
                        success, lat, long ->
                        if(success){
                            sharedPreferences.edit().apply {
                                putBoolean("isInitRun", true)
                                putBoolean("userPrefGps", true)
                                putFloat("userLat", lat.toFloat())
                                putFloat("userLon", long.toFloat())
                                apply()
                            }
                        }
                        else{
                            binding.switchGps.isChecked = false
                            Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else{
                    requestLocationPermission(
                        {
                            getLocation {
                                    success, lat, long ->
                                if(success){
                                    sharedPreferences.edit().apply {
                                        putBoolean("isInitRun", true)
                                        putBoolean("userPrefGps", true)
                                        putFloat("userLat", lat.toFloat())
                                        putFloat("userLon", long.toFloat())
                                        apply()
                                    }
                                }
                                else{
                                    Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        {
                            binding.switchGps.isChecked = false
                        }
                    )
                }
            }
            else{
                sharedPreferences.edit().apply {
                    putBoolean("isInitRun", true)
                    putBoolean("userPrefGps", false)
                    apply()
                }
            }
        }

        binding.btnChooseLocation.setOnClickListener {
            showMapSelectorDial()
        }

        val speedUnits = listOf("km/h", "m/s", "mph")
        val speedUnitAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, speedUnits)
        speedUnitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerWindUnit.adapter = speedUnitAdapter
        binding.spinnerWindUnit.setSelection(
            speedUnits.indexOf(sharedPreferences.getString("windSpeedUnit", "km/h"))
        )
        binding.spinnerWindUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedUnit = speedUnits[position]
                sharedPreferences.edit().putString("windSpeedUnit", selectedUnit).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        val tempUnits = listOf("Celsius", "Fahrenheit", "Kelvin")
        val tempUnitAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tempUnits)
        tempUnitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTempUnit.adapter = tempUnitAdapter
        binding.spinnerTempUnit.setSelection(
            tempUnits.indexOf(sharedPreferences.getString("tempUnit", "Celsius"))
        )
        binding.spinnerTempUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedUnit = tempUnits[position]
                sharedPreferences.edit().putString("tempUnit", selectedUnit).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                /*Do nothing*/
            }
        }

        val pressureUnits = listOf("hPa", "mmHg", "inHg")
        val pressureUnitAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, pressureUnits)
        pressureUnitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPressureUnit.adapter = pressureUnitAdapter
        binding.spinnerPressureUnit.setSelection(
            pressureUnits.indexOf(sharedPreferences.getString("pressureUnit", "hPa"))
        )
        binding.spinnerPressureUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedUnit = pressureUnits[position]
                sharedPreferences.edit().putString("pressureUnit", selectedUnit).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        val visiblityUnits = listOf("km", "miles", "meters")
        val visibilityUnitAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, visiblityUnits)
        visibilityUnitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerVisibilityUnit.adapter = visibilityUnitAdapter
        binding.spinnerVisibilityUnit.setSelection(
            visiblityUnits.indexOf(sharedPreferences.getString("visibilityUnit", "km"))
        )
        binding.spinnerVisibilityUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedUnit = visiblityUnits[position]
                sharedPreferences.edit().putString("visibilityUnit", selectedUnit).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        val elevationUnits = listOf("meters", "feet")
        val elevationUnitAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, elevationUnits)
        elevationUnitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerElevationUnit.adapter = elevationUnitAdapter
        binding.spinnerElevationUnit.setSelection(
            elevationUnits.indexOf(sharedPreferences.getString("elevationUnit", "meters"))
        )
        binding.spinnerElevationUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedUnit = elevationUnits[position]
                sharedPreferences.edit().putString("elevationUnit", selectedUnit).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        val languages = listOf<String>("English","Arabic","German")
        val languageAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLanguage.adapter = languageAdapter

        binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (initializingLanguageSpinner) {
                    initializingLanguageSpinner = false
                    return
                }

                val selectedLanguage = languages[position]
                val currentLanguage = sharedPreferences.getString("language", "English")

                if (selectedLanguage != currentLanguage) {
                    sharedPreferences.edit {
                        putString("language", selectedLanguage)
                    }
                    restartApp()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerLanguage.setSelection(
            languages.indexOf(sharedPreferences.getString("language", "English"))
        )
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
        onLocationGranted : settings_onLocationGranted_t?,
        onLocationDenied : settings_onLocationDenied_t?
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

    @RequiresPermission(allOf = [ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION])
    private fun getLocation(callBack : settings_locationRes_t?) {

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

    private fun restartApp() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        requireActivity().finishAffinity()

        requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

}
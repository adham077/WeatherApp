package com.example.weatherapp.alerts.view

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.weatherapp.R
import com.example.weatherapp.alerts.viewmodel.AlertViewModel
import com.example.weatherapp.alerts.viewmodel.AlertViewModelFactory
import com.example.weatherapp.alerts.worker.AlertsWorker
import com.example.weatherapp.databinding.DialogAddAlertBinding
import com.example.weatherapp.databinding.DialogMapSelectorBinding
import com.example.weatherapp.databinding.FragmentAlertsBinding
import com.example.weatherapp.home.view.InitialSetupFragmentDirections
import com.example.weatherapp.home.view._locationRes_t
import com.example.weatherapp.home.view._onLocationDenied_t
import com.example.weatherapp.home.view._onLocationGranted_t
import com.example.weatherapp.home.viewmodel.HomeViewModel
import com.example.weatherapp.home.viewmodel.HomeViewModelFactory
import com.example.weatherapp.model.data.source.local.weather.WeatherLocalDataSource
import com.example.weatherapp.model.data.source.remote.weather.WeatherRemoteDataSource
import com.example.weatherapp.model.pojo.WeatherAlertEntity
import com.example.weatherapp.model.repository.weather.WeatherRepository
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit


class AlertsFragment : Fragment() {

    lateinit var binding : FragmentAlertsBinding
    lateinit var viewModel: AlertViewModel
    private var selectedLocation: GeoPoint? = null
    private var selectedTimeMillis: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModelFactory = AlertViewModelFactory(
            WeatherRepository.getInstance(
                WeatherLocalDataSource(requireContext()),
                WeatherRemoteDataSource(requireContext())
            )
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[AlertViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fabAddAlert.setOnClickListener {
            showMapDial()
        }

    }

    private fun showMapDial(){
        val diaBinding = DialogAddAlertBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = Dialog(requireContext()).apply {
            setContentView(diaBinding.root)
            window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
        val mapView = diaBinding.mapView
        setupMap(mapView = mapView)
        diaBinding.etDateTime.setOnClickListener {
            showDateTimePicker(dialog)
        }

        diaBinding.btnSaveAlert.setOnClickListener {
            val lat = selectedLocation?.latitude  ?: 0.0
            val long = selectedLocation?.longitude ?: 0.0
            val dateTime = diaBinding.etDateTime.text.toString()
            if(lat != 0.0 && long != 0.0 && dateTime.isNotEmpty()) {
                val delayMillis = selectedTimeMillis - System.currentTimeMillis()

                viewModel.getWeather(lat,long)
                viewModel.weatherTimed.observe(viewLifecycleOwner) {
                    weather->
                    if(weather == null){
                        Toast.makeText(requireContext(), "Failed to fetch weather data", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        val myWeatherEntity = WeatherAlertEntity(
                            timeAndDate = dateTime,
                            response = weather
                        )
                        viewModel.insertWeatherAlert(
                            myWeatherEntity
                        )
                        viewModel.insertedWeatherAlertStatus.observe(viewLifecycleOwner) {
                            result->
                            if(result){
                                val myItemId = myWeatherEntity.id
                                val myNotificationId = myWeatherEntity.id
                                val workRequest = OneTimeWorkRequestBuilder<AlertsWorker>()
                                    .setInputData(
                                        workDataOf(
                                            "NOTIFICATION_ID" to myNotificationId,
                                            "ITEM_ID" to myItemId
                                        )
                                    )
                                    .addTag("alert_${myItemId}")
                                    .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                                    .build()

                                WorkManager.getInstance(requireContext()).enqueue(workRequest)
                                dialog.dismiss()
                                Toast.makeText(requireContext(), "Alert saved successfully", Toast.LENGTH_SHORT).show()
                            }
                            else{
                                Toast.makeText(requireContext(), "Failed to save alert", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            else{
                Toast.makeText(requireContext(), "Please select a valid location and date/time", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
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

    private fun showDateTimePicker(dialog: Dialog) {
        val calendar = Calendar.getInstance()
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setTheme(R.style.ThemeOverlay_App_DatePicker)
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()
        val timePicker = MaterialTimePicker.Builder()
            .setTitleText("Select Time")
            .setTheme(R.style.ThemeOverlay_App_TimePicker)
            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendar.get(Calendar.MINUTE))
            .build()

        datePicker.addOnPositiveButtonClickListener {
            calendar.timeInMillis = it
            timePicker.show(parentFragmentManager, "TIME_PICKER")
        }

        timePicker.addOnPositiveButtonClickListener {
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            calendar.set(Calendar.MINUTE, timePicker.minute)

            selectedTimeMillis = calendar.timeInMillis

            val formatter = SimpleDateFormat("EEE, MMM d yyyy - h:mm a", Locale.getDefault())
            dialog.findViewById<TextInputEditText>(R.id.etDateTime).setText(
                formatter.format(calendar.time)
            )
        }
        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }
}
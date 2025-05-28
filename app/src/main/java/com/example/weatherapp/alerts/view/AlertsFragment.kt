package com.example.weatherapp.alerts.view


import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.R
import com.example.weatherapp.alerts.receiver.AlertsReceiver
import com.example.weatherapp.alerts.receiver.NotificationReceiver
import com.example.weatherapp.alerts.viewmodel.AlertViewModel
import com.example.weatherapp.alerts.viewmodel.AlertViewModelFactory
import com.example.weatherapp.databinding.DialogAddAlertBinding
import com.example.weatherapp.databinding.FragmentAlertsBinding
import com.example.weatherapp.model.data.source.local.weather.WeatherLocalDataSource
import com.example.weatherapp.model.data.source.remote.weather.WeatherRemoteDataSource
import com.example.weatherapp.model.pojo.WeatherAlertEntity
import com.example.weatherapp.model.repository.weather.WeatherRepository
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


class AlertsFragment : Fragment() {

    lateinit var binding : FragmentAlertsBinding
    lateinit var viewModel: AlertViewModel
    private var selectedLocation: GeoPoint? = null
    private var selectedTimeMillis: Long = 0
    private lateinit var alertsAdapter: AlertsAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
        }
        else {
        }
    }

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

        val onDeletePressed : (Int) -> Unit = {result->
            viewModel.deleteWeatherAlert(result)
            cancelAlarm(result)
        }



        viewModel.getAllAlerts()
        viewModel.allWeatherAlerts.observe(viewLifecycleOwner) {result->
            alertsAdapter = AlertsAdapter(
                items = result,
                onItemDeleted = onDeletePressed
            )
            binding.alertsRecycler.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = alertsAdapter
            }
        }

        binding.fabAddAlert.setOnClickListener {
            if(checkNotificationPermissions()){
                showMapDial()
            }
            else{
                requestNotificationPermission()
                if(checkNotificationPermissions()){
                    showMapDial()
                }
                else{
                    Toast.makeText(requireContext(), "Please allow notification permissions to set alerts", Toast.LENGTH_SHORT).show()
                }
            }
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
                                val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    if (alarmManager.canScheduleExactAlarms()) {
                                        viewModel.getLastWeatherAlertId()
                                        viewModel.lastAlertId.observe(viewLifecycleOwner) {
                                            scheduleExactAlarm(alarmManager, selectedTimeMillis, it!!)
                                            alertsAdapter.addItem(myWeatherEntity)
                                        }
                                    } else {
                                        Toast.makeText(requireContext(), "Please allow exact alarm permission in settings.", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    scheduleExactAlarm(alarmManager, selectedTimeMillis, myWeatherEntity.id)
                                }

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

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun checkNotificationPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun scheduleExactAlarm(alarmManager: AlarmManager, timeMillis: Long, alarmId: Int) {
        val alarmIntent = Intent(requireContext(), AlertsReceiver::class.java).apply {
            putExtra("ITEM_ID", alarmId)
        }

        Log.i("ALARM_ACTIVITY","ITEM_ID $alarmId")

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            alarmId,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
    }

    private fun cancelAlarm(id : Int){
        val intent = Intent(requireContext(), NotificationReceiver::class.java).apply {
            action = "ALARM_ACTION"
            putExtra("ITEM_ID", id)
            putExtra("ALARM_ID", id)
        }

        requireContext().sendBroadcast(intent)
    }

}
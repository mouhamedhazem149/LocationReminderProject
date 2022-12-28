package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.*
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.LocationRequiringFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.base.locationRequest
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.settings.SettingsFragment.Companion.getRadiusSettings
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : LocationRequiringFragment(),
    OnMapReadyCallback {

    private lateinit var map: GoogleMap

    /**
     * livedata for user selected marker and the circle around it
     */
    private val selectedMarker = MutableLiveData<Marker?>(null)
    private val rangeCircle = MutableLiveData<Circle?>(null)

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    override fun onStart() {
        super.onStart()

        // check if there's a selected marker and draw it
        selectedMarker.value?.let { marker ->
            rangeCircle.value?.let { circle ->
                if (getRadiusSettings(requireContext()).toDouble() != circle.radius) {

                    val markerOptions = MarkerOptions()
                        .position(marker.position)
                        .title(marker.title)

                    map.handleMarkger(markerOptions)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        // layout inflation and bindings
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        // observing if user has made an input or not and change visiblity
        // of select button accordingly
        selectedMarker.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it != null) {
                binding.saveLocationButtton.visibility = View.VISIBLE
            } else {
                binding.saveLocationButtton.visibility = View.GONE
            }
        })

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        //        TODO: add the map setup implementation
        setupMap()

//        TODO: call this function after the user confirms on the selected location
        binding.saveLocationButtton.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence

        selectedMarker.value?.let { marker ->
            _viewModel.currentReminder.value?.let { reminderItem ->
                reminderItem.location = marker.title
                reminderItem.latitude = marker.position.latitude
                reminderItem.longitude = marker.position.longitude

                _viewModel.navigationCommand.value = NavigationCommand.Back
            }
        }
    }

    private fun setupMap(){
        //        TODO: add the map setup implementation
        //find the map fragment
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_view) as SupportMapFragment?

        // async getting and loading of the map
        lifecycleScope.launch {
            mapFragment?.getMapAsync { onMapReady(it) }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // if the user gave the permission to access location, setup the map
        if (canWorkWithLocation()){
            setupMap()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
//        TODO: zoom to the user location after taking his permission

        // check if user has grant permission
        if (!canWorkWithLocation()
            || (checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PermissionChecker.PERMISSION_GRANTED
            && checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PermissionChecker.PERMISSION_GRANTED
        )) {
            // if user hasn't granted permission ask for permission
            checkLocationPermissionsAndServices()

        } else {

            // if user granted the permission, setup the map

            // ui settings
            map.isMyLocationEnabled = true
            map.uiSettings.isZoomControlsEnabled = true

//        TODO: add style to the map

            map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )

            // get user current location, add a default marker and zoom into location
            val locationProvider = LocationServices
                .getFusedLocationProviderClient(requireContext())

            // get current location
            locationProvider
                .lastLocation
                .addOnCompleteListener { task ->
                    // if completed and result not null then update
                    if (task.result != null) {
                        task.result?.let {
                            currentLocationUpdated(it)
                        }
                    } else {

                        // else check locaiton permission and settings again and
                        // ask for location update

                        locationProvider.requestLocationUpdates(
                            locationRequest,
                            object : LocationCallback() {
                                override fun onLocationResult(locationResult: LocationResult?) {
                                    super.onLocationResult(locationResult)

                                    if (locationResult == null) {
                                        return
                                    }

                                    for (location in locationResult.getLocations()) {
                                        if (location != null) {
                                            currentLocationUpdated(location)
                                        }
                                    }

                                    locationProvider.removeLocationUpdates(this)
                                }
                            }, Looper.getMainLooper()
                        )
                    }
                }

//        TODO: put a marker to location that the user selected
            map.setMapLongClick()
            map.setPoiClick()
            map.setMapClick()

        }
    }

    //add a default marker and zoom into current location
    private fun currentLocationUpdated(newLocation : Location) {
        val zoom = 15f
        val location = LatLng(newLocation.latitude, newLocation.longitude)

        //zoom into location
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                location,
                zoom
            )
        )

        // add marker
        map.handleMarkger(
            MarkerOptions()
                .position(location)
                .title("Current Location")
        )

        // add a default circle to mark user's current location
        map.addCircle(
            CircleOptions()
                .center(LatLng(location.latitude, location.longitude))
                .radius(100.0)
                .strokeWidth(5f)
                .strokeColor(R.color.black)
        )
    }

    private fun GoogleMap.handleMarkger(markerOptions: MarkerOptions) {

        finalizeMarkerOptions(markerOptions)

        selectedMarker.value?.remove()
        rangeCircle.value?.remove()

        selectedMarker.value = addMarker(markerOptions)

        val radius = getRadiusSettings(requireContext())
        rangeCircle.value = addCircle(
            CircleOptions()
                .center(selectedMarker.value!!.position)
                .radius(radius.toDouble())
                .strokeWidth(2f)
                .strokeColor(R.color.primaryDarkColor)
                .fillColor(R.color.primaryLightColor)
        )

        selectedMarker.value!!.showInfoWindow()
    }

    private fun finalizeMarkerOptions(markerOptions: MarkerOptions) {
        val snippet = String.format(
            Locale.getDefault(),
            getString(R.string.lat_long_snippet),
            markerOptions.position.latitude,
            markerOptions.position.longitude
        )
        markerOptions.snippet(snippet)

        if (markerOptions.title == getString(R.string.dropped_pin)) {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        }
    }

    /**
     *  on long click add marker
     */
    private fun GoogleMap.setMapLongClick() {

        setOnMapLongClickListener { latLng ->
            handleMarkger(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
            )
        }
    }

    private fun GoogleMap.setMapClick() {

        setOnMapClickListener { latLng ->
            handleMarkger(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
            )
        }
    }

    private fun GoogleMap.setPoiClick() {

        setOnPoiClickListener { poi ->
            handleMarkger(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
        }
    }
}

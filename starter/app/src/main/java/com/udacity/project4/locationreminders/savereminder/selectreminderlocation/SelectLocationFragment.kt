package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.settings.SettingsFragment.Companion.getRadiusSettings
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(),
    OnMapReadyCallback{

    private lateinit var map: GoogleMap

    private val REQUEST_LOCATION_PERMISSION = 1

    private val selectedMarker = MutableLiveData<Marker?>(null)
    private val rangeCircle = MutableLiveData<Circle?>(null)

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    override fun onStart() {

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
        super.onStart()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

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

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_view) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

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

        selectedMarker.value?.let {
            _viewModel.reminderSelectedLocationStr.value =it.title

            _viewModel.latitude.value = it.position.latitude
            _viewModel.longitude.value = it.position.longitude

//            _viewModel.showSnackBar.value = String.format(
//                Locale.getDefault(),
//                getString(R.string.select_poi_succeed),
//                it.title
//            )

            _viewModel.navigationCommand.value = NavigationCommand.Back
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

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

//        TODO: zoom to the user location after taking his permission
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {

            map.isMyLocationEnabled = true
            map.uiSettings.isZoomControlsEnabled = true

            val zoom = 15f
            LocationServices
                .getFusedLocationProviderClient(requireContext())
                .lastLocation
                .addOnCompleteListener { task ->
                    val location = task.result!!

                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(location.latitude, location.longitude),
                            zoom
                        )
                    )
                }

//        TODO: add style to the map

            map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )

//        TODO: put a marker to location that the user selected
            map.setMapLongClick()
            map.setPoiClick()

        }
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

    private fun GoogleMap.setMapLongClick() {
        setOnMapLongClickListener { latLng ->

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

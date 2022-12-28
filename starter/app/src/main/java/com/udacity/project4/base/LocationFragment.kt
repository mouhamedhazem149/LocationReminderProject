package com.udacity.project4.base

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity.*
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R


// base fragment for location permissions and service control handling
abstract class LocationRequiringFragment : BaseFragment() {

    // on Starting of the fragment, check for the permissions and service
    override fun onStart() {
        super.onStart()
        checkLocationPermissionsAndServices()
    }

    // check user's location permission and related services
    protected fun checkLocationPermissionsAndServices() {
        // check if location permission granted
        if (isLocationPermissionGranted()) {

            // if granted check the location services
            checkDeviceLocationSettings()
        } else {

            // if not granted ask user for the permissions
            requestForegroundAndBackgroundLocationPermissions()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //handle the result of the resloution intent
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            // if ok nothing needs to be done here
            when (resultCode) {
                RESULT_OK -> {

                }
                RESULT_CANCELED -> {
                    // if cancelled send information to user
                    onLocationServiceError()
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    // handle the user input for permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onLocationPermissionsResult(requestCode, grantResults)
    }

    fun onLocationPermissionsResult(
        requestCode: Int,
        grantResults: IntArray
    ) {
        // if user didn't grant the permissions
        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED)
        ) {
            // then show message giving rationale on needing to have the permission
            // and giving option to go to settings and change it
            // and prevent user from proceeding by navigating back

            view?.let {
                Snackbar.make(
                    it,
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_LONG
                )
                    .setAction(R.string.settings) {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }.show()
            }

            // can navigate back or exit of the program to prevent progression
            // without permission,however, wasn't very intuiative
            // _viewModel.navigationCommand.postValue(NavigationCommand.Back)

        } else {
            // if user gave the permission, then go to check if the services are enabled
            checkDeviceLocationSettings()
        }
    }

    // check if permissions are granted ACCESS_FINE_LOCATION,
    // and ACCESS_BACKGROUND_LOCATION for Q or higher
    @TargetApi(29)
    fun isLocationPermissionGranted(): Boolean {

        val foregroundLocationApproved = (
                PermissionChecker.PERMISSION_GRANTED ==
                        checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ))

        val backgroundPermissionApproved =
            if (runningQOrLater()) {
                PermissionChecker.PERMISSION_GRANTED ==
                        checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    // request permissions from the user
    @TargetApi(29)
    protected fun requestForegroundAndBackgroundLocationPermissions() {
        if (isLocationPermissionGranted())
            return

        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val resultCode = when {
            runningQOrLater() -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }

        requestPermissions(
            permissionsArray,
            resultCode
        )
    }

    // check location services settings
    // from https://developer.android.com/training/location/change-location-settings#location-request
    protected fun checkDeviceLocationSettings(resolve: Boolean = true) {

        // get location settings respone task to check settings
        getLocationSettingsResponseBuilder()
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException && resolve) {
                    try {

                        //from :: https://stackoverflow.com/questions/40110823/start-resolution-for-result-in-a-fragment
                        startIntentSenderForResult(
                            exception.resolution.intentSender,
                            REQUEST_TURN_DEVICE_LOCATION_ON,
                            null,
                            0,
                            0,
                            0,
                            null
                        )

                    } catch (sendEx: IntentSender.SendIntentException) {
                        onLocationServiceError()
                        Log.d(
                            "TAG",
                            "Error getting location settings resolution: " + sendEx.message
                        )
                    }
                } else {
                    onLocationServiceError()
                }
            }
    }

    private fun getLocationSettingsResponseBuilder(): Task<LocationSettingsResponse> {
        // get a location update request to minimize risk of null in
        // location fragment getting lastlocation
        val builder =
            LocationSettingsRequest
                .Builder()
                .addLocationRequest(locationRequest)

        val settingsClient =
            LocationServices
                .getSettingsClient(requireActivity())

        return settingsClient.checkLocationSettings(builder.build())
    }

    // if there's location service error send message to user
    private fun onLocationServiceError() {
        view?.let {
            Snackbar.make(
                it,
                R.string.location_required_error, Snackbar.LENGTH_LONG
            ).setAction(R.string.retry_string) {
                checkDeviceLocationSettings()
            }.show()
        }

        // can navigate back or exit of the program to prevent progression
        // without permission,however, wasn't very intuiative
        //_viewModel.navigationCommand.postValue(NavigationCommand.Back)
    }

    //function to quick check both location service and settings
    fun canWorkWithLocation(): Boolean {

        val locationManager: LocationManager? =
            requireContext().getSystemService(LOCATION_SERVICE) as LocationManager?

        locationManager?.let {
            try {
                val gps_enabled =
                    locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val network_enabled =
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                return isLocationPermissionGranted() && gps_enabled && network_enabled
            } catch (ex: Exception) {
                return false
            }
        }
        return false
    }
}
// generic location request pritorizing power effieciency over accuracy
val locationRequest : LocationRequest
get() = LocationRequest.create().apply {
    priority = LocationRequest.PRIORITY_LOW_POWER
}

// function to check if user running Android Q or higher to handle specific permissions
fun runningQOrLater() : Boolean = android.os.Build.VERSION.SDK_INT >=
        android.os.Build.VERSION_CODES.Q

// some permissions request numbers
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34


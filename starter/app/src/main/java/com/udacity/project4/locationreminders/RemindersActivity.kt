package com.udacity.project4.locationreminders

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.firebase.ui.auth.BuildConfig
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.authentication.AuthenticationState
import com.udacity.project4.authentication.FirebaseUserLiveData
import kotlinx.android.synthetic.main.activity_reminders.*

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q

    override fun onStart() {
        super.onStart()
        //checkPermissionsAndStartGeofencing()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)

        FirebaseUserLiveData().observe(this, Observer {
            when (it) {
                AuthenticationState.AUTHENTICATED -> {
                }
                AuthenticationState.UNAUTHENTICATED -> {
                    nav_host_fragment.startActivity(
                        Intent(
                            this,
                            AuthenticationActivity::class.java
                        )
                    )
                    finish()
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
//            checkDeviceLocationSettingsAndStartGeofence(false)
//        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings ->{
                (nav_host_fragment as NavHostFragment).navController.navigate(R.id.settingsFragment)
            }
            android.R.id.home -> {
                (nav_host_fragment as NavHostFragment).navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

//    private fun checkPermissionsAndStartGeofencing() {
//        if (isPermissionGranted()) {
//            checkDeviceLocationSettingsAndStartGeofence()
//        } else {
//            requestForegroundAndBackgroundLocationPermissions()
//        }
//    }
//
//    @TargetApi(29)
//    private fun isPermissionGranted(): Boolean {
//        val foregroundLocationApproved = (
//                PackageManager.PERMISSION_GRANTED ==
//                        ActivityCompat.checkSelfPermission(this,
//                            Manifest.permission.ACCESS_FINE_LOCATION))
//        val backgroundPermissionApproved =
//            if (runningQOrLater) {
//                PackageManager.PERMISSION_GRANTED ==
//                        ActivityCompat.checkSelfPermission(
//                            this,
//                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
//                        )
//            } else {
//                true
//            }
//        return foregroundLocationApproved && backgroundPermissionApproved
//    }
//
//    @TargetApi(29 )
//    private fun requestForegroundAndBackgroundLocationPermissions() {
//        if (isPermissionGranted())
//            return
//        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
//        val resultCode = when {
//            runningQOrLater -> {
//                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
//                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
//            }
//            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
//        }
//        ActivityCompat.requestPermissions(
//            this,
//            permissionsArray,
//            resultCode
//        )
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
//        if (
//            grantResults.isEmpty() ||
//            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
//            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
//                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
//                    PackageManager.PERMISSION_DENIED))
//        {
//            Snackbar.make(
//                findViewById(R.id.reminders_layout),
//                R.string.permission_denied_explanation,
//                Snackbar.LENGTH_INDEFINITE
//            )
//                .setAction(R.string.settings) {
//                    startActivity(Intent().apply {
//                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
//                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                    })
//                }.show()
//        } else {
//            checkDeviceLocationSettingsAndStartGeofence()
//        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }
//
//    private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {
//        val locationRequest = LocationRequest.create().apply {
//            priority = LocationRequest.PRIORITY_LOW_POWER
//        }
//        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
//        val settingsClient = LocationServices.getSettingsClient(this)
//        val locationSettingsResponseTask =
//            settingsClient.checkLocationSettings(builder.build())
//
//        locationSettingsResponseTask.addOnFailureListener { exception ->
//            if (exception is ResolvableApiException && resolve){
//                try {
//                    exception.startResolutionForResult(this,
//                        REQUEST_TURN_DEVICE_LOCATION_ON)
//                } catch (sendEx: IntentSender.SendIntentException) {
//                    Log.d("TAG", "Error getting location settings resolution: " + sendEx.message)
//                }
//            } else {
//                Snackbar.make(
//                    findViewById(R.id.content),
//                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
//                ).setAction(android.R.string.ok) {
//                    checkDeviceLocationSettingsAndStartGeofence()
//                }.show()
//            }
//        }
//
//    }

}

package com.udacity.project4.locationreminders.geofence

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.settings.SettingsFragment


fun getGeofencePendingIntent(context: Context): PendingIntent {
    val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
    intent.action = GeofenceBroadcastReceiver.ACTION_GEOFENCE_EVENT
    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}

fun GeofencingClient.addGeofenceForReminder(currentGeofenceData: ReminderDataItem) {

    val radius = SettingsFragment.getRadiusSettings(applicationContext)

    val geofence = Geofence.Builder()
        .setRequestId(currentGeofenceData.id)
        .setCircularRegion(
            currentGeofenceData.latitude!!,
            currentGeofenceData.longitude!!,
            radius.toFloat()
        )
        .setExpirationDuration(Geofence.NEVER_EXPIRE)
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
        .build()

    val geofencingRequest = GeofencingRequest.Builder()
        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        .addGeofence(geofence)
        .build()

    if (ActivityCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        addGeofences(
            geofencingRequest,
            getGeofencePendingIntent(applicationContext)
        )
    }
}

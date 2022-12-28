package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.geofence.addGeofenceForReminder
import com.udacity.project4.locationreminders.geofence.removeGeofenceForReminderIds
import com.udacity.project4.locationreminders.reminderslist.AsReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.settings.SettingsFragment.Companion.getRadiusSettings
import kotlinx.coroutines.launch

class SaveReminderViewModel(val app: Application, val dataSource: ReminderDataSource) :
    BaseViewModel(app) {

    // serves as a private store for the reminder model being added / edited
    private val _currentReminder = MutableLiveData<ReminderDataItem>(
        ReminderDataItem(
            null,
            null,
            null,
            null,
            null,
            null
        )
    )

    /**
     * public getter
     */
    val currentReminder: LiveData<ReminderDataItem?>
        get() = _currentReminder

    /**
     * function used to set reminder without being public exposed, to conform to encapsulation principle
     */
    fun updateCurrentReminder(reminderItem: ReminderDataItem) {
        _currentReminder.value = reminderItem
    }

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        // set new null reminder item with new id that is auto generated
        updateCurrentReminder(
            ReminderDataItem(
                null,
                null,
                null,
                null,
                null,
                null
            )
        )
    }

    /**
     * set radius from settings, then request save of current reminder
     */
    fun SaveCurrentReminder() {
        val radius = getRadiusSettings(app)

        currentReminder.value?.let {
            it.radius = radius.toDouble()
            validateAndSaveReminder(it)
        }
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun validateAndSaveReminder(reminderData: ReminderDataItem) {
        if (validateEnteredData(reminderData)) {
            saveReminder(reminderData)
        }
    }

    /**
     * Save the reminder to the data source and remove old geofence if there's any
     * (like if it's edit not delete) with same id and add the new geofence request
     * if already added, will be updated
     */
    fun saveReminder(reminderData: ReminderDataItem) {
        showLoading.value = true
        viewModelScope.launch {
            try {
                dataSource.saveReminder(
                    reminderData.AsReminderDTO()
                )

                showLoading.value = false
                showToast.value = app.getString(R.string.reminder_saved)
                navigationCommand.value = NavigationCommand.Back

                LocationServices
                    .getGeofencingClient(app)
                    .addGeofenceForReminder(reminderData)

            } catch (ex: Exception) {

            }
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    fun validateEnteredData(reminderData: ReminderDataItem): Boolean {

        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.description.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_description
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        return true
    }
}
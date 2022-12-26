package com.udacity.project4.locationreminders.savereminder

import android.app.Application
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
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch

class SaveReminderViewModel(val app: Application, val dataSource: ReminderDataSource) :
    BaseViewModel(app) {
    val reminderTitle = MutableLiveData<String?>()
    val reminderDescription = MutableLiveData<String?>()
    val reminderSelectedLocationStr = MutableLiveData<String?>()
    val selectedPOI = MutableLiveData<PointOfInterest?>()
    val latitude = MutableLiveData<Double?>()
    val longitude = MutableLiveData<Double?>()

    val savedReminder = MutableLiveData<ReminderDataItem?>(null)

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        reminderSelectedLocationStr.value = null
        selectedPOI.value = null
        latitude.value = null
        longitude.value = null
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource and return it if succeeded
     */

    fun SaveCurrentReminder() {

        val reminderItem = ReminderDataItem(
            reminderTitle.value,
            reminderDescription.value,
            reminderSelectedLocationStr.value,
            latitude.value,
            longitude.value
        )

        if (validateEnteredData(reminderItem)) {
            saveReminder(reminderItem)
        } else{
            savedReminder.value = null
        }
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
//    fun validateAndSaveReminder(reminderData: ReminderDataItem) {
//        if (validateEnteredData(reminderData)) {
//            saveReminder(reminderData)
//        }
//    }

    /**
     * Save the reminder to the data source
     */
    fun saveReminder(reminderData: ReminderDataItem) {
        showLoading.value = true

        viewModelScope.launch {
            try {
                dataSource.saveReminder(
                    ReminderDTO(
                        reminderData.title,
                        reminderData.description,
                        reminderData.location,
                        reminderData.latitude,
                        reminderData.longitude,
                        reminderData.id
                    )
                )
                showLoading.value = false
                showToast.value = app.getString(R.string.reminder_saved)
                navigationCommand.value = NavigationCommand.Back

                LocationServices
                    .getGeofencingClient(app)
                    .addGeofenceForReminder(reminderData)

                savedReminder.postValue(reminderData)
            } catch (ex: Exception) {
                savedReminder.postValue(null)
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

        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        return true
    }
}
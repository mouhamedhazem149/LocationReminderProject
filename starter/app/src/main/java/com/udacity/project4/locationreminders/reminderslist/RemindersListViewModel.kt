package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.geofence.removeGeofenceForReminderIds
import com.udacity.project4.settings.SettingsFragment
import com.udacity.project4.utils.SingleLiveEvent
import kotlinx.coroutines.launch

class RemindersListViewModel(
    val app: Application,
    private val dataSource: ReminderDataSource
) : BaseViewModel(app) {
    // list that holds the reminder data to be displayed on the UI
    val remindersList = MutableLiveData<List<ReminderDataItem>>()

    // liveData for selected reminder, singleLiveEvent was used here as i just need to observe
    // changes related to setting the value
    val selectedReminder: SingleLiveEvent<ReminderDataItem?> = SingleLiveEvent()

    /**
     * Get all the reminders from the DataSource and add them to the remindersList to be shown on the UI,
     * or show error if any
     */
    fun loadReminders() {
        showLoading.value = true
        viewModelScope.launch {
            //interacting with the dataSource has to be through a coroutine
            val result = dataSource.getReminders()
            showLoading.postValue(false)
            when (result) {
                is Result.Success<*> -> {
                    val dataList = ArrayList<ReminderDataItem>()
                    dataList.addAll((result.data as List<ReminderDTO>).map { reminder ->
                        //map the reminder data from the DB to the be ready to be displayed on the UI
                        ReminderDataItem(
                            reminder.title,
                            reminder.description,
                            reminder.location,
                            reminder.latitude,
                            reminder.longitude,
                            reminder.radius,
                            reminder.id
                        )
                    })
                    remindersList.value = dataList
                }
                is Result.Error ->
                    showSnackBar.value = result.message
            }
            //check if no data has to be shown
            invalidateShowNoData()
        }
    }

    // handle data related to created geofences and data in DB according to setting set by the user
    // (keep everything as default)
    fun onHandleSignOut() {
        when (SettingsFragment.getSignoutSettings(app)) {
            // if want to keep everything nothing is done
            app.getString(R.string.keepAll_settings_option) -> {

            }
            // if want to preserve DB and remove geofencerequest only
            app.getString(R.string.keepReminders_settings_option) -> {
                viewModelScope.launch {
                    LocationServices.getGeofencingClient(app).removeGeofenceForReminderIds()
                }
            }
            // if want to remove everything
            app.getString(R.string.deleteAll_settings_option) -> {
                viewModelScope.launch {
                    dataSource.deleteAllReminders()
                    LocationServices.getGeofencingClient(app).removeGeofenceForReminderIds()
                }
            }
        }
    }

    /**
     * Inform the user that there's not any data if the remindersList is empty
     */
    private fun invalidateShowNoData() {
        showNoData.value = remindersList.value == null || remindersList.value!!.isEmpty()
    }
}
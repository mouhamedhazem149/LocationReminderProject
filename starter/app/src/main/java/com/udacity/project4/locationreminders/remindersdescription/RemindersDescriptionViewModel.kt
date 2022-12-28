package com.udacity.project4.locationreminders.remindersdescription

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.geofence.removeGeofenceForReminderIds
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch

/**
 * ViewModel for ReminderDescriptionActivity with just one function to Delete the reminder,
 * and retrieved with koin using DI
 */
class RemindersDescriptionViewModel(
    val app: Application,
    val dataSource: ReminderDataSource)
    : BaseViewModel(app) {

    // the current displayed Reminder that is private with public getter to ensure encapsulation
    private val _currentReminder = MutableLiveData<ReminderDataItem?>(null)
    val currentReminder: LiveData<ReminderDataItem?>
        get() = _currentReminder

    // the setter method for the current reminder
    fun updateCurrentReminder(reminderItem: ReminderDataItem) {
        _currentReminder.value = reminderItem
    }

    // used to delete the _currentReminder
    fun deleteCurrentReminder() {
        currentReminder.value?.let {
            deleteReminder(it)
        }
    }

    fun deleteReminder(reminderData: ReminderDataItem) {
        viewModelScope.launch {
            try {
                dataSource.deleteReminder(reminderData.id)

                showToast.value = app.getString(R.string.reminder_deleted)
                navigationCommand.value = NavigationCommand.Back

                LocationServices
                    .getGeofencingClient(app)
                    .removeGeofenceForReminderIds(reminderData.id)

            } catch (ex: Exception) {
                Log.i("DeleteReminders", "Error deletion " + ex.message)
            }
        }
    }
}
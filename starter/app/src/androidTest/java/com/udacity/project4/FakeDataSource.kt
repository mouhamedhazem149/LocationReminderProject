package com.udacity.project4

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.Result.*

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(val reminders : MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {

    private var shouldReturnError = false
    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

//    TODO: Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {

        if (shouldReturnError){
            return Error("Error Loading Reminders")
        }else{
            return Success(reminders)
        }

       // TODO("Return the reminders")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        if (shouldReturnError){
            // implement Error ?
        } else {
            reminders.add(reminder)
        }
        //TODO("save the reminder")
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {

        if (shouldReturnError) {
            return Error("Exception Thrown")
        } else {
            for (reminder in reminders) {
                if (reminder.id == id) {
                    return Success(reminder)
                }
            }
            return Error("No Reminder Found")
        }

        //TODO("return the reminder with the id")
    }

    override suspend fun deleteAllReminders() {
        if (shouldReturnError) {
            throw Exception("Failed to save reminder")
        } else {
            reminders.clear()
            //TODO("delete all the reminders")
        }
    }

}
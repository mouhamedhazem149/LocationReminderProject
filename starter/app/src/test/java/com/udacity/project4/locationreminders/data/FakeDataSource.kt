package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(val reminders : MutableList<ReminderDTO> = mutableListOf()) : ReminderDataSource {

    private var shouldReturnError = false
    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

//    TODO: Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {

        if (shouldReturnError) {
            return Result.Error("Test exception")
        } else {
            return Result.Success(reminders)
        }
        // TODO("Return the reminders")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
        //TODO("save the reminder")
    }

    override suspend fun deleteReminder(id: String) {
        reminders.removeIf {
            it.id == id
        }
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {

        if (shouldReturnError) {
            return Result.Error("Test exception")
        } else {
            val result = reminders.find { it.id == id }

            if (result != null) {
                return Result.Success(result)
            } else {
                return Result.Error("Reminder not found!")
            }
        }

        //TODO("return the reminder with the id")
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
        //TODO("delete all the reminders")
    }
}
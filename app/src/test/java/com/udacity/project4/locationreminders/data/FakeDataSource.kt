package com.udacity.project4.locationreminders.data

import android.provider.Settings.System.getString
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var tasks: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        tasks?.let {
            return Result.Success(it)
        }
        return Result.Error("Error: There are no reminders")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        tasks?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val isFound = tasks?.firstOrNull {
            it.id == id
        }

        return if(isFound == null)
            Result.Error("Error: Reminder not found")
        else
            Result.Success(isFound)
    }

    override suspend fun deleteAllReminders() {
        tasks?.clear()
    }

}
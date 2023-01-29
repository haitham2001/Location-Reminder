package com.udacity.project4.locationreminders.data

import android.provider.Settings.System.getString
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private var tasks: MutableList<ReminderDTO> = mutableListOf()) : ReminderDataSource {

    var shouldReturnError = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        try{
            if(shouldReturnError)
                throw Exception("Exception: Failed to load data")
            tasks.let {
                return Result.Success(it)
            }
        } catch (ex: Exception) {
            return Result.Error(ex.localizedMessage)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        tasks.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        try {
            if (shouldReturnError)
                throw Exception("Exception: Failed to load data")

            val isFound = tasks.firstOrNull {
                it.id == id
            }

            return if (isFound == null)
                Result.Error("Error: Reminder not found")
            else
                Result.Success(isFound)
        } catch(ex: Exception) {
            return Result.Error(ex.localizedMessage)
        }
    }

    override suspend fun deleteAllReminders() {
        tasks.clear()
    }

}
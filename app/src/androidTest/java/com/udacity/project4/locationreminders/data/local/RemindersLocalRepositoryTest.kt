package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.InstrumentationRegistry
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
    private lateinit var database: RemindersDatabase
    private lateinit var dao: RemindersDao
    private lateinit var repository: RemindersLocalRepository

    @Before
    fun init(){
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.reminderDao()
        repository = RemindersLocalRepository(dao)
    }

    @After
    fun closeDataBase(){
        database.close()
    }

    @Test
    fun insertAll_addItemsInDatabase_allItemsAreInDatabase() = runBlocking {
        // GIVEN:
        val reminderItems = listOf(
            ReminderDTO("title" , "description" , "location" , 102.0 , 105.0 , "1"),
            ReminderDTO("title" , "description" , "location" , 15.0 , 18.0 , "2"),
            ReminderDTO("title" , "description" , "location" , 120.0 , 150.0 , "3")
        )

        // WHEN:
        for(item in reminderItems)
            database.reminderDao().saveReminder(item)

        // THEN:
        assertThat(database.reminderDao().getReminders(),`is`(reminderItems))
    }

    @Test
    fun emptyDatabase_dataNotFound_showAnErrorMessage() = runBlocking {
        // GIVEN:
        val reminderItem = repository.getReminder("-1")

        // WHEN:
        val error =  (reminderItem) as Result.Error

        // THEN:
        assertThat(error.message, `is`("Reminder not found!"))
    }
}
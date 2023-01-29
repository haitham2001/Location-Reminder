package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var dataSource: FakeDataSource

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    var coroutineRule = MainCoroutineRule()

    private val validData = ReminderDataItem("title","Desc","loc",10.0,12.0,"1")
    private val inValidData = ReminderDataItem(null,null,null,null,null,"0")

    // A setup before every test
    @Before
    fun setupViewModel() {
        stopKoin()
        dataSource = FakeDataSource()
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    // Tests if the validEnteredData works if the data is true
    @Test
    fun validateEnteredData_ValidReminder_True() = assertThat(viewModel.validateEnteredData(validData), CoreMatchers.`is`(true))

    // Tests if the validEnteredData works if the data is false
    @Test
    fun validateEnteredData_InValidReminder_False() = assertThat(viewModel.validateEnteredData(inValidData), CoreMatchers.`is`(false))

    // Tests if the loading works properly
    @Test
    fun saveReminder_CheckLoading_True(){
        coroutineRule.pauseDispatcher()
        viewModel.validateAndSaveReminder(validData)
        assertThat(viewModel.showLoading.getOrAwaitValue(),`is`(true))
        coroutineRule.resumeDispatcher()
    }
}
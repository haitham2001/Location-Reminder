package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var viewModel: RemindersListViewModel
    private lateinit var dataSource: FakeDataSource

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    var mainCoroutine = MainCoroutineRule()

    // Clean up after every test
    @Before
    fun setupViewModel() {
        stopKoin()
        dataSource = FakeDataSource()
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    // Checks the loading
    @Test
    fun loadReminders_checkLoading() {

        // WHEN
        mainCoroutine.pauseDispatcher()
        viewModel.loadReminders()

        // THEN: Check if it is loading
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutine.resumeDispatcher()

        // Check if it is not loading
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    // Checks if data is null and if it is, it should print an error
    @Test
    fun loadReminders_ReturnError() {
        dataSource.shouldReturnError = true
        viewModel.loadReminders()
        assertThat(viewModel.showSnackBar.getOrAwaitValue(), `is`("Exception: Failed to load data"))
    }
}
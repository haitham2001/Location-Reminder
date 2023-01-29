package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val idlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(idlingResource)
    }

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(idlingResource)
    }

    @Test
    fun saveReminderFragment_noTitleEntered_showSnackBar() = runBlocking {
        // GIVEN:
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        idlingResource.monitorActivity(scenario)
        val reminderItem = ReminderDTO("title" , "description" , "location" , 12.0 , 15.0 , "2")

        // WHEN:
        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        onView(withId(R.id.reminderDescription)).perform(
            ViewActions.typeText(reminderItem.description),
            ViewActions.closeSoftKeyboard()
        )

        onView(withId(R.id.selectLocation)).perform(ViewActions.click())
        onView(withId(R.id.map)).perform(ViewActions.longClick())
        onView(withId(R.id.saveLocationBtn)).perform(ViewActions.click())
        onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        // THEN:
        onView(withId(R.id.snackbar_text))
            .check(matches(withText("Please enter title")))

        scenario.close()
    }

    @Test
    fun reminderListFragment_successfullySavedReminder_showToast(){
        // GIVEN:
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        idlingResource.monitorActivity(scenario)
        val reminderItem = ReminderDTO("title" , "description" , "location" , 12.0 , 15.0 , "2")

        // WHEN:
        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        onView(withId(R.id.reminderTitle)).perform(
            ViewActions.typeText(reminderItem.title),
            ViewActions.closeSoftKeyboard()
        )

        onView(withId(R.id.reminderDescription)).perform(
            ViewActions.typeText(reminderItem.description),
            ViewActions.closeSoftKeyboard()
        )

        onView(withId(R.id.selectLocation)).perform(ViewActions.click())
        onView(withId(R.id.map)).perform(ViewActions.longClick())
        onView(withId(R.id.saveLocationBtn)).perform(ViewActions.click())
        onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        // THEN:
        var activity: Activity? = null
        scenario.onActivity {
            activity = it
        }

        onView(withText(R.string.reminder_saved)).inRoot(
            RootMatchers.withDecorView(
                not(`is`(activity!!.window.decorView))
            )
        ).check(matches(isDisplayed()))

        scenario.close()
    }
}

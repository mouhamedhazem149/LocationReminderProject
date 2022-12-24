package com.udacity.project4

import android.app.Application
import android.util.Log
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.android.synthetic.main.activity_reminders.*
import kotlinx.coroutines.runBlocking
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

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
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

//    TODO: add End to End testing to the app

    @Test
    fun saveOneReminder() = runBlocking {

        // start up Tasks screen
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        val toAddTitle = "testTitle"
        val toAddDescription = "testDescription"

        dataBindingIdlingResource.monitorActivity(activityScenario)

        // navigate to Add reminder
        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        onView(withId(R.id.savereminder_layout)).check(matches(isDisplayed()))

        onView(withId(R.id.reminderTitle))
            .perform(typeText(toAddTitle), closeSoftKeyboard())

        onView(withId(R.id.reminderDescription))
            .perform(typeText(toAddDescription), closeSoftKeyboard())

        // navigate to select location
        onView(withId(R.id.selectLocation)).perform(ViewActions.click())
        onView(withId(R.id.selectLocation_layout)).check(matches(isDisplayed()))

        onView(withId(R.id.map_view)).perform(ViewActions.longClick())
        onView(withId(R.id.save_location_buttton)).perform(ViewActions.click())

        // Save Reminder
        onView(withId(R.id.savereminder_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        // check if it's added to list
        onView(withId(R.id.refreshLayout)).check(matches(isDisplayed()))
        onView(withId(R.id.item_title)).check(matches(withText(toAddTitle)))
        onView(withId(R.id.item_description)).check(matches(withText(toAddDescription)))

        activityScenario.close()
    }

}

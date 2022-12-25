package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.FakeDataSource
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.android.synthetic.main.fragment_reminders.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    private lateinit var appContext: Application
    private lateinit var dataSource : FakeDataSource

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()

        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as FakeDataSource
                )
            }
            single { FakeDataSource() }
        }

        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }

        dataSource = get()

        runBlocking {
            dataSource.deleteAllReminders()
        }
    }

    //    TODO: test the navigation of the fragments.
    @Test
    fun clickAddReminderButton_navigateToSaveReminderFragment() {

        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on the add button
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - Verify that we navigate to the saveReminder screen
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    //    TODO: test the displayed data on the UI.
    @Test
    fun onEmptyReminderListFragment_HomeUiDataShowNoData() {

        // GIVEN - no Reminders
        // already deleted in before
        runBlockingTest {
            dataSource.deleteAllReminders()
        }

        // WHEN - in reminders list fragment
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // THEN - Verify that noData is shown
        onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(isDisplayed()))
        onView(withId(R.id.noDataTextView)).check(
            ViewAssertions.matches(
                withText(
                    appContext.getString(
                        R.string.no_data
                    )
                )
            )
        )
    }
    @Test
    fun onSomeRemindersInReminderListFragment_HomeUiDataShowReminders() {

        // GIVEN - Reminderlistfragment with some reminders
        val reminder1 = ReminderDTO("title1", "description1", "location1", 1.0, 1.0)
        val reminder2 = ReminderDTO("title2", "description2", "location2", 1.0, 1.0)
        val reminder3 = ReminderDTO("title3", "description3", "location3", 1.0, 1.0)
        val reminder4 = ReminderDTO("title4", "description4", "location4", 1.0, 1.0)

        val toSaveReminders = listOf(reminder1, reminder2, reminder3, reminder4)

        runBlockingTest {
            for (reminder in toSaveReminders) {
                dataSource.saveReminder(reminder)
            }
        }

        // WHEN - in ui
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withId(R.id.refreshLayout)).check(ViewAssertions.matches(isDisplayed()))

        // THEN - Verify reminders are loaded
        onView(withId(R.id.reminderssRecyclerView)).check(ViewAssertions.matches(isDisplayed()))
        for (reminder in toSaveReminders) {

            onView(withText(reminder.title)).check(ViewAssertions.matches(isDisplayed()))
            onView(withText(reminder.description)).check(ViewAssertions.matches(isDisplayed()))
            onView(withText(reminder.location)).check(ViewAssertions.matches(isDisplayed()))

        }
    }

    //    TODO: add testing for the error messages.
    @Test
    fun reminderListFragmentonErrorLoadingListShowSnackBar() {
        // given error loading reminders
        dataSource.setReturnError(true)

        // WHEN - in ui
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withId(R.id.refreshLayout)).check(ViewAssertions.matches(isDisplayed()))

        // THEN - Verify reminders are loaded
        onView(withId(R.id.snackbar_text)).check(ViewAssertions.matches(withText("Error Loading Reminders")))
    }
}
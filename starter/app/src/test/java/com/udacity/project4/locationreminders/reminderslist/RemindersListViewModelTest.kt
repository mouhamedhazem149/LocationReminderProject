package com.udacity.project4.locationreminders.reminderslist

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.MyApp
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var fakeDatasource: FakeDataSource

    @Before
    fun setupViewModel() {
        fakeDatasource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(MyApp(), fakeDatasource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun noRemindersLoadRemindersShowNoDataTrue() = runBlockingTest {
        // Given no Data
        fakeDatasource.deleteAllReminders()

        //when load reminders
        remindersListViewModel.loadReminders()

        //Then showNoData should be true
        assertThat(remindersListViewModel.remindersList.value?.count(), `is`(0))
        assertThat(remindersListViewModel.showNoData.value, `is`(true))
    }

    @Test
    fun errorLoadRemindersErrorMessage() = runBlockingTest {
        // Given error
        fakeDatasource.setReturnError(true)

        //when load reminders
        remindersListViewModel.loadReminders()

        //Then showNoData should be true
        assertThat(remindersListViewModel.showSnackBar.value, `is`("Test exception"))
        assertThat(remindersListViewModel.showNoData.value, `is`(true))
    }

    @Test
    fun saveRemindersLoadRemindersShowNoDataFalse() = runBlockingTest {
        // Given Data
        val reminder1 = ReminderDTO("title1", "description1", "location1", 1.0, 1.0,100.0)
        val reminder2 = ReminderDTO("title2", "description2", "location2", 1.0, 1.0,100.0)
        val reminder3 = ReminderDTO("title3", "description3", "location3", 1.0, 1.0,100.0)
        val reminder4 = ReminderDTO("title4", "description4", "location4", 1.0, 1.0,100.0)

        val toSaveReminders = listOf(reminder1, reminder2, reminder3, reminder4)

        for (reminder in toSaveReminders) {
            fakeDatasource.saveReminder(reminder)
        }

        //when load reminders
        remindersListViewModel.loadReminders()

        //Then showNoData should be true
        assertThat(remindersListViewModel.showNoData.value, `is`(false))
        assertThat(remindersListViewModel.remindersList.value, notNullValue())

        assertThat(remindersListViewModel.remindersList.value!!.count(),`is`(toSaveReminders.count()))
        for (reminder in remindersListViewModel.remindersList.value!!) {
            val toSaveReminder = toSaveReminders.find { it.id == reminder.id }

            assertThat(toSaveReminder, notNullValue())

            toSaveReminder?.let {
                assertThat(toSaveReminder.id, `is`(reminder.id))
                assertThat(toSaveReminder.title, `is`(reminder.title))
                assertThat(toSaveReminder.description, `is`(reminder.description))
                assertThat(toSaveReminder.location, `is`(reminder.location))
                assertThat(toSaveReminder.latitude, `is`(reminder.latitude))
                assertThat(toSaveReminder.longitude, `is`(reminder.longitude))
            }
        }

    }
}
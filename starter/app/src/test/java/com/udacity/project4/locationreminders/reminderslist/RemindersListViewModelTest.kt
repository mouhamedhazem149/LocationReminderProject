package com.udacity.project4.locationreminders.reminderslist

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RuntimeEnvironment.application

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var fakeDatasource: FakeDataSource

    @Before
    fun setupViewModel() {
        fakeDatasource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(application, fakeDatasource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun noRemindersShowNoDate() {
        // Given no Data
        runBlocking {
            fakeDatasource.deleteAllReminders()
        }

        //when load reminders
        remindersListViewModel.loadReminders()

        //Then showNoData should be true
        assertThat(remindersListViewModel.remindersList.value?.count(), `is`(0))
        assertThat(remindersListViewModel.showNoData.value, `is`(true))
    }

    @Test
    fun anyRemindersShowNoDatefalse() {
        // Given Data

        val reminder1 = ReminderDTO(
            "test1",
            "test Desc",
            "location 1",
            1.0,
            1.0
        )

        val reminder2 = ReminderDTO(
            "test2",
            "test Desc",
            "location 2",
            2.0,
            2.0
        )

        val reminder3 = ReminderDTO(
            "test3",
            "test Desc",
            "location 3",
            3.0,
            3.0
        )

        runBlocking {
            fakeDatasource.saveReminder(reminder1)
            fakeDatasource.saveReminder(reminder2)
            fakeDatasource.saveReminder(reminder3)
        }

        //when load reminders
        remindersListViewModel.loadReminders()

        //Then showNoData should be true
        assertThat(remindersListViewModel.showNoData.value, `is`(false))
    }

    @Test
    fun errorRemindersShowNoData() {
        // Given error
        fakeDatasource.setReturnError(true)

        //when load reminders
        remindersListViewModel.loadReminders()

        //Then showNoData should be true
        assertThat(remindersListViewModel.showSnackBar.value, `is`("Exception Thrown"))
    }
}
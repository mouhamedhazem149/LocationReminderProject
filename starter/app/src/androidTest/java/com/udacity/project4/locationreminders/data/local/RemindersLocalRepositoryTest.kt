package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    TODO: Add testing implementation to the RemindersLocalRepository.kt

    private lateinit var localRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localRepository =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveReminderAndGetReminder() = runBlocking {

        // GIVEN
        val reminder = ReminderDTO("title", "description", "location", 1.0, 1.0)
        localRepository.saveReminder(reminder)

        // WHEN
        val loaded = localRepository.getReminder(reminder.id)

        // THEN
        loaded as Result.Success
        assertThat(loaded,CoreMatchers.notNullValue())

        assertThat(loaded.data.id, `is`(reminder.id))
        assertThat(loaded.data.title, `is`(reminder.title))
        assertThat(loaded.data.description, `is`(reminder.description))
        assertThat(loaded.data.location, `is`(reminder.location))
        assertThat(loaded.data.latitude, `is`(reminder.latitude))
        assertThat(loaded.data.longitude, `is`(reminder.longitude))

    }

    @Test
    fun saveRemindersAndGetReminders() = runBlocking {

        // Given
        val reminder1 = ReminderDTO("title1", "description1", "location1", 1.0, 1.0)
        val reminder2 = ReminderDTO("title2", "description2", "location2", 1.0, 1.0)
        val reminder3 = ReminderDTO("title3", "description3", "location3", 1.0, 1.0)
        val reminder4 = ReminderDTO("title4", "description4", "location4", 1.0, 1.0)

        val toSaveReminders = listOf(reminder1, reminder2, reminder3, reminder4)

        for (reminder in toSaveReminders) {
            localRepository.saveReminder(reminder)
        }

        // When
        val reminders = localRepository.getReminders()

        // THEN

        reminders as Result.Success
        assertThat(reminders,CoreMatchers.notNullValue())

        assertThat(reminders.data.count(),`is`(toSaveReminders.count()))
        for (reminder in reminders.data) {
            val toSaveReminder = toSaveReminders.find { it.id == reminder.id }

            assertThat(toSaveReminder, CoreMatchers.notNullValue())

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

    @Test
    fun saveRemindersAndDeleteReminders() = runBlocking {

        // Given
        val reminder1 = ReminderDTO("title1", "description1", "location1", 1.0, 1.0)
        val reminder2 = ReminderDTO("title2", "description2", "location2", 1.0, 1.0)
        val reminder3 = ReminderDTO("title3", "description3", "location3", 1.0, 1.0)
        val reminder4 = ReminderDTO("title4", "description4", "location4", 1.0, 1.0)

        val toSaveReminders = listOf(reminder1, reminder2, reminder3, reminder4)

        for (reminder in toSaveReminders) {
            localRepository.saveReminder(reminder)
        }

        // When
        localRepository.deleteAllReminders()
        val reminders = localRepository.getReminders()

        // THEN
        reminders as Result.Success
        assertThat(reminders,CoreMatchers.notNullValue())

        assertThat(reminders.data.count(),`is`(0))
    }

}
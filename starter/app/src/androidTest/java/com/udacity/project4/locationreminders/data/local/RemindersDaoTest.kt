package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

//    TODO: Add testing implementation to the RemindersDao.kt

    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveReminderAndGetReminderById() = runBlockingTest {

        // GIVEN
        val reminder = ReminderDTO("title", "description", "location", 1.0, 1.0, 100.0)
        database.reminderDao().saveReminder(reminder)

        // WHEN
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // THEN
        assertThat(loaded!!, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
        assertThat(loaded.radius, `is`(reminder.radius))
    }

    @Test
    fun saveRemindersAndGetReminders() = runBlockingTest {

        // Given
        val reminder1 = ReminderDTO("title1", "description1", "location1", 1.0, 1.0, 100.0)
        val reminder2 = ReminderDTO("title2", "description2", "location2", 1.0, 1.0, 100.0)
        val reminder3 = ReminderDTO("title3", "description3", "location3", 1.0, 1.0, 100.0)
        val reminder4 = ReminderDTO("title4", "description4", "location4", 1.0, 1.0, 100.0)

        val toSaveReminders = listOf(reminder1, reminder2, reminder3, reminder4)

        for (reminder in toSaveReminders) {
            database.reminderDao().saveReminder(reminder)
        }

        // When
        val reminders = database.reminderDao().getReminders()

        // THEN

        assertThat(reminders.count(), `is`(toSaveReminders.count()))
        for (reminder in reminders) {
            val toSaveReminder = toSaveReminders.find { it.id == reminder.id }

            assertThat(toSaveReminder, notNullValue())

            toSaveReminder?.let {
                assertThat(toSaveReminder.id, `is`(reminder.id))
                assertThat(toSaveReminder.title, `is`(reminder.title))
                assertThat(toSaveReminder.description, `is`(reminder.description))
                assertThat(toSaveReminder.location, `is`(reminder.location))
                assertThat(toSaveReminder.latitude, `is`(reminder.latitude))
                assertThat(toSaveReminder.longitude, `is`(reminder.longitude))
                assertThat(toSaveReminder.radius, `is`(reminder.radius))
            }
        }
    }

    @Test
    fun saveRemindersAndDeleteReminders() = runBlockingTest {

        // Given
        val reminder1 = ReminderDTO("title1", "description1", "location1", 1.0, 1.0, 100.0)
        val reminder2 = ReminderDTO("title2", "description2", "location2", 1.0, 1.0, 100.0)
        val reminder3 = ReminderDTO("title3", "description3", "location3", 1.0, 1.0, 100.0)
        val reminder4 = ReminderDTO("title4", "description4", "location4", 1.0, 1.0, 100.0)

        val toSaveReminders = listOf(reminder1, reminder2, reminder3, reminder4)

        for (reminder in toSaveReminders) {
            database.reminderDao().saveReminder(reminder)
        }

        // When
        database.reminderDao().deleteAllReminders()
        val reminders = database.reminderDao().getReminders()

        // THEN
        assertThat(reminders.count(), `is`(0))
    }

    @Test
    fun saveRemindersAndDeleteReminder() = runBlockingTest {

        // Given
        val reminder1 = ReminderDTO("title1", "description1", "location1", 1.0, 1.0, 100.0)
        val reminder2 = ReminderDTO("title2", "description2", "location2", 1.0, 1.0, 100.0)
        val reminder3 = ReminderDTO("title3", "description3", "location3", 1.0, 1.0, 100.0)
        val reminder4 = ReminderDTO("title4", "description4", "location4", 1.0, 1.0, 100.0)

        val toSaveReminders = listOf(reminder1, reminder2, reminder3, reminder4)
        val toDeleteReminder = reminder1

        for (reminder in toSaveReminders) {
            database.reminderDao().saveReminder(reminder)
        }

        // When
        database.reminderDao().deleteReminder(toDeleteReminder.id)

        // THEN

        for (reminder in toSaveReminders) {
            val savedReminder = database.reminderDao().getReminderById(reminder.id)

            if (reminder == toDeleteReminder) {
                assertThat(savedReminder, nullValue())
            } else {
                assertThat(savedReminder, notNullValue())

                savedReminder?.let {
                    assertThat(it.id, `is`(reminder.id))
                    assertThat(it.title, `is`(reminder.title))
                    assertThat(it.description, `is`(reminder.description))
                    assertThat(it.location, `is`(reminder.location))
                    assertThat(it.latitude, `is`(reminder.latitude))
                    assertThat(it.longitude, `is`(reminder.longitude))
                    assertThat(it.radius, `is`(reminder.radius))
                }
            }
        }
    }
}

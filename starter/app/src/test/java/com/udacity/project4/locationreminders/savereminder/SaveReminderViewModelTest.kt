package com.udacity.project4.locationreminders.savereminder

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.MyApp
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    //TODO: provide testing to the SaveReminderView and its live data objects

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var fakeDatasource: FakeDataSource

    @Before
    fun setupViewModel() {
        fakeDatasource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDatasource)
    }

    @After
    fun tearDown() {
        fakeDatasource.setReturnError(false)
        stopKoin()
    }

    @Test
    fun validReminderSaveCurrentReminderSuccess() = runBlockingTest {
        //Given
        val toSave = ReminderDataItem(
            "testTitle",
            "testDescription",
            "testLocation",
            1.0,
            1.0,
            100.0
        )
        saveReminderViewModel.updateCurrentReminder(toSave)

        //When
        saveReminderViewModel.SaveCurrentReminder()
        val saveResult = fakeDatasource.getReminder(toSave.id) as Result.Success<ReminderDTO>?

        //Then
        assertThat(saveResult, notNullValue())

        saveResult?.let {

            assertThat(toSave.id, `is`(it.data.id))
            assertThat(toSave.title, `is`(it.data.title))
            assertThat(toSave.description, `is`(it.data.description))
            assertThat(toSave.latitude, `is`(it.data.latitude))
            assertThat(toSave.longitude, `is`(it.data.longitude))
            assertThat(toSave.location, `is`(it.data.location))
            assertThat(toSave.radius, `is`(it.data.radius))
        }
    }

    @Test
    fun nullTitleSaveCurrentReminderNullSaveResult() = runBlockingTest {
        //Given
        val toSave = ReminderDataItem(
            null,
            "testDescription",
            "testLocation",
            1.0,
            1.0,
            100.0
        )
        saveReminderViewModel.updateCurrentReminder(toSave)

        //When
        saveReminderViewModel.SaveCurrentReminder()
        val saveResult = fakeDatasource.getReminder(toSave.id) as Result.Error

        //Then
        assertThat(saveResult,`is`(Result.Error("Reminder not found!")))
        assertThat(
            saveReminderViewModel.showSnackBarInt.value,
            `is`(R.string.err_enter_title)
        )
    }

    @Test
    fun nullDescriptionSaveCurrentReminderNullSaveResult() = runBlockingTest {
        //Given
        val toSave = ReminderDataItem(
            "testTitle",
            null,
            "testLocation",
            1.0,
            1.0,
            100.0
        )
        saveReminderViewModel.updateCurrentReminder(toSave)

        //When
        saveReminderViewModel.SaveCurrentReminder()
        val saveResult = fakeDatasource.getReminder(toSave.id) as Result.Error

        //Then
        assertThat(saveResult,`is`(Result.Error("Reminder not found!")))
        assertThat(
            saveReminderViewModel.showSnackBarInt.value,
            `is`(R.string.err_enter_description)
        )
    }

    @Test
    fun nullLocationSaveCurrentReminderNullSaveResult() = runBlockingTest {
        //Given
        val toSave = ReminderDataItem(
            "testTitle",
            "testDescription",
            null,
            1.0,
            1.0,
            100.0
        )
        saveReminderViewModel.updateCurrentReminder(toSave)

        //When
        saveReminderViewModel.SaveCurrentReminder()
        val saveResult = fakeDatasource.getReminder(toSave.id) as Result.Error

        //Then
        assertThat(saveResult,`is`(Result.Error("Reminder not found!")))
        assertThat(
            saveReminderViewModel.showSnackBarInt.value,
            `is`(R.string.err_select_location)
        )
    }

    @Test
    fun dbErrorSaveCurrentReminderNullSaveResult() = runBlockingTest{
        //Given
        val toSave = ReminderDataItem(
            "testTitle",
            "testDescription",
            "testLocation",
            1.0,
            1.0,
            100.0
        )
        saveReminderViewModel.updateCurrentReminder(toSave)

        fakeDatasource.setReturnError(true)

        //When
        saveReminderViewModel.SaveCurrentReminder()
        val saveResult = fakeDatasource.getReminder(toSave.id) as Result.Error

        //Then
        assertThat(saveResult,`is`(Result.Error("Test exception")))
    }

    @Test
    fun onClearTestAllValuesNull(){
        //

        //When viewModel Cleared
        saveReminderViewModel.onClear()

        //Then
        assertThat(saveReminderViewModel.currentReminder.value, CoreMatchers.notNullValue())
        assertThat(saveReminderViewModel.currentReminder.value!!.title, CoreMatchers.nullValue())
        assertThat(saveReminderViewModel.currentReminder.value!!.description, CoreMatchers.nullValue())
        assertThat(saveReminderViewModel.currentReminder.value!!.latitude, CoreMatchers.nullValue())
        assertThat(saveReminderViewModel.currentReminder.value!!.longitude, CoreMatchers.nullValue())
        assertThat(saveReminderViewModel.currentReminder.value!!.location, CoreMatchers.nullValue())
        assertThat(saveReminderViewModel.currentReminder.value!!.radius, CoreMatchers.nullValue())
    }
}
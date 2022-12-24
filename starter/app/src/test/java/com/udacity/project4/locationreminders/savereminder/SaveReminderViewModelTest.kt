package com.udacity.project4.locationreminders.savereminder

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RuntimeEnvironment

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    //TODO: provide testing to the SaveReminderView and its live data objects

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var fakeDatasource: FakeDataSource

    @Before
    fun setupViewModel() {
        fakeDatasource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(RuntimeEnvironment.application, fakeDatasource)
    }

    @After
    fun tearDown() {
        fakeDatasource.setReturnError(false)
        stopKoin()
    }

    @Test
    fun validReminderSaveCurrentReminderSuccess() {
        //Given
        saveReminderViewModel.reminderTitle.value = "testTitle"
        saveReminderViewModel.reminderDescription.value = "testDescription"
        saveReminderViewModel.reminderSelectedLocationStr.value = "testLocation"
        saveReminderViewModel.latitude.value = 1.0
        saveReminderViewModel.longitude.value = 1.0

        //When

        val saveResult = saveReminderViewModel.SaveCurrentReminder()

        //Then
        lateinit var dbItem: ReminderDTO

        runBlocking {
            saveResult?.let {
                val result =
                    fakeDatasource.getReminder(saveResult.id) as Result.Success<ReminderDTO>
                dbItem = result.data
            }
        }

        if (saveResult != null) {
            assertThat(dbItem,CoreMatchers.notNullValue())

            assertThat(dbItem.id, `is`(saveResult.id))
            assertThat(dbItem.title, `is`(saveResult.title))
            assertThat(dbItem.description, `is`(saveResult.description))
            assertThat(dbItem.latitude, `is`(saveResult.latitude))
            assertThat(dbItem.longitude, `is`(saveResult.longitude))
            assertThat(dbItem.location, `is`(saveResult.location))
        }

        assertThat(saveReminderViewModel.showLoading.value,
            `is`(false))

        assertThat(
            saveReminderViewModel.showToast.value,
            `is`(InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string.reminder_saved))
        )

    }

    @Test
    fun nullTitleSaveCurrentReminderNullSaveResult() {
        //Given
        saveReminderViewModel.reminderTitle.value = null
        saveReminderViewModel.reminderDescription.value = "testDescription"
        saveReminderViewModel.reminderSelectedLocationStr.value = "testLocation"
        saveReminderViewModel.latitude.value = 1.0
        saveReminderViewModel.longitude.value = 1.0

        //When

        val saveResult = saveReminderViewModel.SaveCurrentReminder()

        //Then

        assertThat(saveResult, CoreMatchers.nullValue())
        assertThat(
            saveReminderViewModel.showSnackBarInt.value,
            `is`(R.string.err_enter_title)
        )
    }

    @Test
    fun nullLocationSaveCurrentReminderNullSaveResult(){
        //Given
        saveReminderViewModel.reminderTitle.value = "testTitle"
        saveReminderViewModel.reminderDescription.value = "testDescription"
        saveReminderViewModel.reminderSelectedLocationStr.value = null
        saveReminderViewModel.latitude.value = 1.0
        saveReminderViewModel.longitude.value = 1.0

        //When

        val saveResult = saveReminderViewModel.SaveCurrentReminder()

        //Then

        assertThat(saveResult,CoreMatchers.nullValue())
        assertThat(
            saveReminderViewModel.showSnackBarInt.value,
            `is`(R.string.err_select_location)
        )
    }

    @Test
    fun dbErrorSaveCurrentReminderNullSaveResult(){
        //Given
        saveReminderViewModel.reminderTitle.value = "testTitle"
        saveReminderViewModel.reminderDescription.value = "testDescription"
        saveReminderViewModel.reminderSelectedLocationStr.value = "testLocation"
        saveReminderViewModel.latitude.value = 1.0
        saveReminderViewModel.longitude.value = 1.0

        fakeDatasource.setReturnError(true)

        //When

        val saveResult = saveReminderViewModel.SaveCurrentReminder()

        //Then

        assertThat(saveResult,CoreMatchers.nullValue())
    }

    @Test
    fun onClearTest(){
        //

        //When viewModel Cleared
        saveReminderViewModel.onClear()

        //Then
        assertThat(saveReminderViewModel.reminderTitle.value, CoreMatchers.nullValue())
        assertThat(saveReminderViewModel.reminderDescription.value, CoreMatchers.nullValue())
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.value, CoreMatchers.nullValue())
        assertThat(saveReminderViewModel.selectedPOI.value, CoreMatchers.nullValue())
        assertThat(saveReminderViewModel.latitude.value, CoreMatchers.nullValue())
        assertThat(saveReminderViewModel.longitude.value, CoreMatchers.nullValue())
    }
}
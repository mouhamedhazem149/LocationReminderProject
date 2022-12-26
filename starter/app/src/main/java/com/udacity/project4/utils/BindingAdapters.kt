package com.udacity.project4.utils

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.base.BaseRecyclerViewAdapter
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.settings.SettingsFragment

object BindingAdapters {

    /**
     * Use binding adapter to set the recycler view data using livedata object
     */
    @Suppress("UNCHECKED_CAST")
    @BindingAdapter("android:liveData")
    @JvmStatic
    fun <T> setRecyclerViewData(recyclerView: RecyclerView, items: LiveData<List<T>>?) {
        items?.value?.let { itemList ->
            (recyclerView.adapter as? BaseRecyclerViewAdapter<T>)?.apply {
                clear()
                addData(itemList)
            }
        }
    }

    /**
     * Use this binding adapter to show and hide the views using boolean variables
     */
    @BindingAdapter("android:fadeVisible")
    @JvmStatic
    fun setFadeVisible(view: View, visible: Boolean? = true) {
        if (view.tag == null) {
            view.tag = true
            view.visibility = if (visible == true) View.VISIBLE else View.GONE
        } else {
            view.animate().cancel()
            if (visible == true) {
                if (view.visibility == View.GONE)
                    view.fadeIn()
            } else {
                if (view.visibility == View.VISIBLE)
                    view.fadeOut()
            }
        }
    }

    @BindingAdapter("marker")
    @JvmStatic
    fun MapView.marker(reminderItem: ReminderDataItem) {

        onCreate(null)
        getMapAsync {

            val location = LatLng(reminderItem.latitude!!, reminderItem.longitude!!)

            it.uiSettings.isZoomControlsEnabled = true

            it.addMarker(
                MarkerOptions()
                    .title(reminderItem.location)
                    .position(location)
            )

            val radius = SettingsFragment.getRadiusSettings(context)

            it.addCircle(
                CircleOptions()
                    .center(location)
                    .radius(radius.toDouble())
                    .strokeWidth(2f)
                    .strokeColor(R.color.primaryDarkColor)
                    .fillColor(R.color.primaryLightColor)
            )

            it.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.map_style
                )
            )

            it.moveCamera(CameraUpdateFactory.newLatLngZoom(location,15f))
            onResume()
        }
    }
}
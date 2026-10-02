package com.example.smartagriculture.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartagriculture.calendar.CalendarScheduler
import com.example.smartagriculture.database.AppDatabase
import com.example.smartagriculture.database.CropActivityEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val cropActivityDao = AppDatabase.getDatabase(application).cropActivityDao()
    private val scheduler = CalendarScheduler(application)

    private val _activities = MutableStateFlow<List<CropActivityEntity>>(emptyList())
    val activities: StateFlow<List<CropActivityEntity>> = _activities.asStateFlow()

    init {
        loadActivities()
    }

    private fun loadActivities() {
        viewModelScope.launch {
            cropActivityDao.getAllActivities().collect { list ->
                _activities.value = list
            }
        }
    }

    fun addActivity(
        cropName: String,
        activityTitle: String,
        activityType: String,
        scheduledDate: Long
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val activity = CropActivityEntity(
                cropName = cropName,
                activityTitle = activityTitle,
                activityType = activityType,
                scheduledDate = scheduledDate,
                isCompleted = false
            )
            val newId = cropActivityDao.insertActivity(activity)

            // Schedule WorkManager reminder
            scheduler.scheduleReminder(
                activityId = newId,
                cropName = cropName,
                activityTitle = activityTitle,
                activityType = activityType,
                scheduledTimeMillis = scheduledDate
            )
        }
    }

    fun toggleComplete(activity: CropActivityEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = activity.copy(isCompleted = !activity.isCompleted)
            cropActivityDao.updateActivity(updated)
            if (updated.isCompleted) {
                scheduler.cancelReminder(activity.id)
            }
        }
    }

    fun deleteActivity(activity: CropActivityEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            cropActivityDao.deleteActivity(activity)
            scheduler.cancelReminder(activity.id)
        }
    }
}

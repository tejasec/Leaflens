package com.example.smartagriculture.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartagriculture.calendar.CalendarScheduler
import com.example.smartagriculture.database.AppDatabase
import com.example.smartagriculture.database.CropActivityEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val cropActivityDao = AppDatabase.getDatabase(application).cropActivityDao()
    private val scheduler = CalendarScheduler(application)

    private val _activities = MutableStateFlow<List<CropActivityEntity>>(emptyList())
    val activities: StateFlow<List<CropActivityEntity>> = _activities.asStateFlow()

    private val _filterDailyOnly = MutableStateFlow(false)
    val filterDailyOnly: StateFlow<Boolean> = _filterDailyOnly.asStateFlow()

    val filteredActivities: StateFlow<List<CropActivityEntity>> = combine(
        _activities,
        _filterDailyOnly
    ) { list, dailyOnly ->
        if (dailyOnly) list.filter { it.isDaily } else list
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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

    fun setFilterDailyOnly(dailyOnly: Boolean) {
        _filterDailyOnly.value = dailyOnly
    }

    fun addActivity(
        cropName: String,
        activityTitle: String,
        activityType: String,
        scheduledDate: Long,
        isDaily: Boolean = false
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val activity = CropActivityEntity(
                cropName = cropName,
                activityTitle = activityTitle,
                activityType = activityType,
                scheduledDate = scheduledDate,
                isCompleted = false,
                isDaily = isDaily
            )
            val newId = cropActivityDao.insertActivity(activity)

            // Schedule WorkManager reminder
            scheduler.scheduleReminder(
                activityId = newId,
                cropName = cropName,
                activityTitle = activityTitle,
                activityType = activityType,
                scheduledTimeMillis = scheduledDate,
                isDaily = isDaily
            )
        }
    }

    fun updateActivity(
        activity: CropActivityEntity,
        newCropName: String,
        newTitle: String,
        newType: String,
        newScheduledDate: Long,
        newIsDaily: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = activity.copy(
                cropName = newCropName,
                activityTitle = newTitle,
                activityType = newType,
                scheduledDate = newScheduledDate,
                isDaily = newIsDaily
            )
            cropActivityDao.updateActivity(updated)

            scheduler.cancelReminder(activity.id)
            if (!updated.isCompleted) {
                scheduler.scheduleReminder(
                    activityId = updated.id,
                    cropName = newCropName,
                    activityTitle = newTitle,
                    activityType = newType,
                    scheduledTimeMillis = newScheduledDate,
                    isDaily = newIsDaily
                )
            }
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

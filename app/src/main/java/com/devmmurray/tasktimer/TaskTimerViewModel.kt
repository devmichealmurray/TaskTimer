package com.devmmurray.tasktimer

import android.app.Application
import android.content.ContentValues
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val TAG = "TaskTimerViewModel"

class TaskTimerViewModel(application: Application) : AndroidViewModel(application) {

    private val contentObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            loadTasks()
        }
    }

    private var currentTiming: Timing? = null
    private val databaseCursor = MutableLiveData<Cursor>()
    val cursor: LiveData<Cursor>
        get() = databaseCursor

    init {
        Log.d(TAG, "TaskTimerViewModel created")
        getApplication<Application>()
            .contentResolver
            .registerContentObserver(TasksContract.CONTENT_URI, true, contentObserver)
        currentTiming = retrieveTiming()
        loadTasks()
    }

    private val taskTiming = MutableLiveData<String>()
    val timing: LiveData<String>
        get() = taskTiming

    private fun loadTasks() {
        val projection = arrayOf(
            TasksContract.Columns.ID,
            TasksContract.Columns.TASK_NAME,
            TasksContract.Columns.TASK_DESCRIPTION,
            TasksContract.Columns.TASK_SORT_ORDER
        )
        val sortOrder =
            "${TasksContract.Columns.TASK_SORT_ORDER}, ${TasksContract.Columns.TASK_NAME}"

        viewModelScope.launch {
            val cursor = getApplication<Application>()
                .contentResolver
                .query(
                    TasksContract.CONTENT_URI,
                    projection,
                    null,
                    null,
                    sortOrder
                )
            databaseCursor.postValue(cursor)
        }
    }

    fun saveTask(task: Task): Task {
        val values = ContentValues()

        if (task.name.isNotEmpty()) {
            values.put(TasksContract.Columns.TASK_NAME, task.name)
            values.put(TasksContract.Columns.TASK_DESCRIPTION, task.description)
            values.put(TasksContract.Columns.TASK_SORT_ORDER, task.sortOrder)

            if (task.id == 0L) {
                viewModelScope.launch {
                    val uri = getApplication<Application>()
                        .contentResolver
                        ?.insert(TasksContract.CONTENT_URI, values)
                    if (uri != null) {
                        task.id = TasksContract.getId(uri)
                    }
                }
            } else {
                viewModelScope.launch {
                    getApplication<Application>()
                        .contentResolver
                        ?.update(
                            TasksContract.buildUriFromId(task.id),
                            values, null, null
                        )
                }
            }
        }
        return task
    }

    fun deleteTask(taskId: Long) {
        this.viewModelScope.launch {
            getApplication<Application>()
                .contentResolver
                ?.delete(
                    TasksContract
                        .buildUriFromId(taskId), null, null
                )
        }
    }

    fun timeTask(task: Task) {
        Log.d(TAG, ".timeTask Called")
        val timingRecord = currentTiming

        if (timingRecord == null) {
            currentTiming = Timing(task.id)
            saveTiming(currentTiming!!)
        } else {
            timingRecord.setDuration()
            saveTiming(timingRecord)
            if (task.id == timingRecord.taskId) {

                currentTiming = null
            } else {
                currentTiming = Timing(task.id)
                saveTiming(currentTiming!!)
            }
        }
        // Update the LiveData
        taskTiming.value = if (currentTiming != null) task.name else null
    }

    private fun saveTiming(currentTiming: Timing) {
        Log.d(TAG, ".saveTiming Called")
        val inserting = (currentTiming.duration == 0L)
        val values = ContentValues().apply {
            if (inserting) {
                put(TimingsContract.Columns.TIMINGS_TASK_ID, currentTiming.taskId)
                put(TimingsContract.Columns.TIMINGS_START_TIME, currentTiming.startTime)
            }
            put(TimingsContract.Columns.TIMINGS_DURATION, currentTiming.duration)
        }

        GlobalScope.launch {
            if (inserting) {
                val uri = getApplication<Application>()
                    .contentResolver.insert(TimingsContract.CONTENT_URI, values)
                if (uri != null) {
                    currentTiming.id = TimingsContract.getId(uri)
                }
            } else {
                getApplication<Application>()
                    .contentResolver
                    .update(
                        TimingsContract.buildUriFromId(currentTiming.id),
                        values,
                        null,
                        null
                    )
            }
        }
    }

    private fun retrieveTiming(): Timing? {
        Log.d(TAG, ".retrieveTiming Called")
        val timing: Timing?

        val timingCursor: Cursor? = getApplication<Application>()
            .contentResolver
            .query(CurrentTimingContract.CONTENT_URI,
                null,
                null,
                null,
                null
            )

        if (timingCursor != null && timingCursor.moveToFirst()) {
            val id = timingCursor
                .getLong(timingCursor.getColumnIndex((CurrentTimingContract.Columns.TIMING_ID)))
            val taskId = timingCursor
                .getLong(timingCursor.getColumnIndex((CurrentTimingContract.Columns.TASK_ID)))
            val startTime = timingCursor
                .getLong(timingCursor.getColumnIndex((CurrentTimingContract.Columns.START_TIME)))
            val name = timingCursor
                .getString(timingCursor.getColumnIndex((CurrentTimingContract.Columns.TASK_NAME)))
            timing = Timing(taskId, startTime, id)
            taskTiming.value = name
        } else {
            timing = null
        }
        timingCursor?.close()
        Log.d(TAG, ".retrieveTiming Returning")
        return timing
    }

    override fun onCleared() {
        getApplication<Application>()
            .contentResolver
            .unregisterContentObserver(contentObserver)
    }
}
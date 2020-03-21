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
import kotlinx.coroutines.launch

private const val TAG = "TaskTimerViewModel"

class TaskTimerViewModel(application: Application) : AndroidViewModel(application) {
    private val contentObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            loadTasks()
        }
    }

    private val databaseCursor = MutableLiveData<Cursor>()
    val cursor: LiveData<Cursor>
        get() = databaseCursor

    init {
        Log.d(TAG, "TaskTimerViewModel created")
        getApplication<Application>()
            .contentResolver
            .registerContentObserver(TasksContract.CONTENT_URI, true, contentObserver)
        loadTasks()
    }

    private fun loadTasks() {
        val projection = arrayOf(
            TasksContract.Columns.ID,
            TasksContract.Columns.TASK_NAME,
            TasksContract.Columns.TASK_DESCRIPTION,
            TasksContract.Columns.TASK_SORT_ORDER
        )
        val sortOrder =
            "${TasksContract.Columns.TASK_SORT_ORDER}, ${TasksContract.Columns.TASK_NAME}"

        this.viewModelScope.launch {
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

    override fun onCleared() {
        getApplication<Application>()
            .contentResolver
            .unregisterContentObserver(contentObserver)
    }
}
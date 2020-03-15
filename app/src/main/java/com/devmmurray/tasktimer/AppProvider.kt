package com.devmmurray.tasktimer

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.util.Log

/**
 * Provider for the TaskTimer App. This is the only class that knows
 * about [AppDatabase]
 */

private const val TAG = "AppProvider"
private const val CONTENT_AUTHORITY = "com.devmmurray.tasktimer.provider"
private const val TASKS = 100
private const val TASKS_ID = 101
private const val TIMINGS = 200
private const val TIMINGS_ID = 201
private const val TASK_DURATIONS = 400
private const val TASK_DURATIONS_ID = 401

val CONTENT_AUTHORITY_URI: Uri = Uri.parse("content://$CONTENT_AUTHORITY")

class AppProvider : ContentProvider() {
    private val uriMatcher by lazy { buildUriMatcher() }

    private fun buildUriMatcher(): UriMatcher {
        Log.d(TAG, "buildUriMatcher starts")
        val matcher = UriMatcher(UriMatcher.NO_MATCH)
        matcher.addURI(CONTENT_AUTHORITY, TasksContract.TABLE_NAME, TASKS)
        matcher.addURI(CONTENT_AUTHORITY, "${TasksContract.TABLE_NAME}/#", TASKS_ID)
//        matcher.addURI(CONTENT_AUTHORITY, TimingsContract.TABLE_NAME, TIMINGS)
//        matcher.addURI(CONTENT_AUTHORITY, "${TimingsContract.TABLE_NAME}/#", TIMINGS_ID)
//        matcher.addURI(CONTENT_AUTHORITY, DurationsContract.TABLE_NAME, TASK_DURATIONS)
//        matcher.addURI(CONTENT_AUTHORITY, "${DurationsContract.TABLE_NAME}/#", TASK_DURATIONS_ID)

        return matcher
    }


    override fun onCreate(): Boolean {
        return true
    }

    override fun getType(uri: Uri): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun query(
        uri: Uri, projection: Array<out String>?, selection: String?,
        selectionArgs: Array<out String>?, sortOrder: String?
    ): Cursor? {

        Log.d(TAG, ".query: called with URI $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, ".query: match is $match")

        val queryBuilder = SQLiteQueryBuilder()

        when (match) {
            TASKS -> queryBuilder.tables = TasksContract.TABLE_NAME
            TASKS_ID -> {
                queryBuilder.tables = TasksContract.TABLE_NAME
                val taskId = TasksContract.getId(uri)
                queryBuilder.appendWhereEscapeString("${TasksContract.Columns.ID} = $taskId")
            }
//            TIMINGS -> queryBuilder.tables = TimingsContract.TABLE_NAME
//            TIMINGS_ID -> {
//                queryBuilder.tables = TimingsContract.TABLE_NAME
//                val timingsId = TimingsContract.getId(uri)
//                queryBuilder.appendWhereEscapeString("${TimingsContract.Columns.ID} = $timingsId")
//            }
//            TASK_DURATIONS -> queryBuilder.tables = DurationsContract.TABLE_NAME
//            TASK_DURATIONS_ID -> {
//                queryBuilder.tables = DurationsContract.TABLE_NAME
//                val durationId = DurationsContract.getId(uri)
//                queryBuilder.appendWhereEscapeString("${DurationsContract.Columns.ID} = $durationId")
//            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        val context = context ?: throw NullPointerException("Context can't be null here!")
        val db = AppDatabase.getInstance(context).readableDatabase
        val cursor =
            queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder)
        Log.d(TAG, ".query: rows in returned cursor = ${cursor.count}")

        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues): Uri? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(
        uri: Uri,
        values: ContentValues,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
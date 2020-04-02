package com.devmmurray.tasktimer

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.util.Log

/**
 * Provider for the TaskTimer App. This is the only class that knows
 * about [AppDatabase]
 */

private const val TAG = "AppProvider"
const val CONTENT_AUTHORITY = "com.devmmurray.tasktimer.provider"
private const val TASKS = 100
private const val TASKS_ID = 101
private const val TIMINGS = 200
private const val TIMINGS_ID = 201
private const val CURRENT_TIMING = 300
private const val TASK_DURATIONS = 400
private const val TASK_DURATIONS_ID = 401

val CONTENT_AUTHORITY_URI: Uri = Uri.parse("content://$CONTENT_AUTHORITY")

class AppProvider : ContentProvider() {
    private val uriMatcher: UriMatcher by lazy { buildUriMatcher() }

    private fun buildUriMatcher(): UriMatcher {
        Log.d(TAG, "buildUriMatcher starts")
        val matcher = UriMatcher(UriMatcher.NO_MATCH)
        matcher.addURI(CONTENT_AUTHORITY, TasksContract.TABLE_NAME, TASKS)
        matcher.addURI(CONTENT_AUTHORITY, "${TasksContract.TABLE_NAME}/#", TASKS_ID)
        matcher.addURI(CONTENT_AUTHORITY, TimingsContract.TABLE_NAME, TIMINGS)
        matcher.addURI(CONTENT_AUTHORITY, "${TimingsContract.TABLE_NAME}/#", TIMINGS_ID)
        matcher.addURI(CONTENT_AUTHORITY, CurrentTimingContract.TABLE_NAME, CURRENT_TIMING)
//        matcher.addURI(CONTENT_AUTHORITY, DurationsContract.TABLE_NAME, TASK_DURATIONS)
//        matcher.addURI(CONTENT_AUTHORITY, "${DurationsContract.TABLE_NAME}/#", TASK_DURATIONS_ID)

        return matcher
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            TASKS -> TasksContract.CONTENT_TYPE
            TASKS_ID -> TasksContract.CONTENT_ITEM_TYPE
            TIMINGS -> TimingsContract.CONTENT_TYPE
            TIMINGS_ID -> TimingsContract.CONTENT_ITEM_TYPE
            CURRENT_TIMING -> CurrentTimingContract.CONTENT_ITEM_TYPE
//            TASK_DURATIONS -> DurationsContract.CONTENT_TYPE
//            TASK_DURATIONS_ID -> DurationsContract.CONTENT_ITEM_TYPE
            else -> throw java.lang.IllegalArgumentException("Unknown Uri: $uri")
        }
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
                queryBuilder.appendWhere("${TasksContract.Columns.ID} = ")
                queryBuilder.appendWhereEscapeString("$taskId")
            }
            TIMINGS -> queryBuilder.tables = TimingsContract.TABLE_NAME
            TIMINGS_ID -> {
                queryBuilder.tables = TimingsContract.TABLE_NAME
                val timingsId = TimingsContract.getId(uri)
                queryBuilder.appendWhere("${TimingsContract.Columns.ID} = ")
                queryBuilder.appendWhereEscapeString("$timingsId")
            }
            CURRENT_TIMING -> {
                queryBuilder.tables = CurrentTimingContract.TABLE_NAME
            }
//            TASK_DURATIONS -> queryBuilder.tables = DurationsContract.TABLE_NAME
//            TASK_DURATIONS_ID -> {
//                queryBuilder.tables = DurationsContract.TABLE_NAME
//                val durationId = DurationsContract.getId(uri)
//                queryBuilder.appendWhere("${DurationsContract.Columns.ID} = ")
//                queryBuilder.appendWhereEscapeString("$durationId")
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

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        Log.d(TAG, ".insert: called with URI $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, ".insert: match is $match")

        val recordId: Long
        val returnUri: Uri
        val context = context ?: throw NullPointerException("Context can't be null here!")
        when (match) {
            TASKS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                recordId = db.insert(TasksContract.TABLE_NAME, null, values)
                if (recordId != -1L) {
                    returnUri = TasksContract.buildUriFromId(recordId)
                } else {
                    throw SQLException("Failed to insert. URI: $uri")
                }
            }
            TIMINGS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                recordId = db.insert(TimingsContract.TABLE_NAME, null, values)
                if (recordId != -1L) {
                    returnUri = TimingsContract.buildUriFromId(recordId)
                } else {
                    throw SQLException("Failed to insert. URI: $uri")
                }
            }
            else -> throw IllegalArgumentException("Unknown Uri: $uri")
        }
        if (recordId > 0) context.contentResolver?.notifyChange(uri, null)
        Log.d(TAG, "Exiting Insert, Returning $returnUri")
        return returnUri
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {

        Log.d(TAG, ".update: called with URI $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, ".update: match is $match")

        val count: Int
        var selectionCriteria: String

        val context = context ?: throw NullPointerException("Context can't be null here!")
        when (match) {
            TASKS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                count = db.update(TasksContract.TABLE_NAME, values, selection, selectionArgs)
            }
            TASKS_ID -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                val id = TasksContract.getId(uri)
                selectionCriteria = "${TasksContract.Columns.ID} = $id"
                if (selection != null && selectionCriteria.isNotEmpty()) {
                    selectionCriteria += " AND ($selection)"
                }
                count =
                    db.update(TasksContract.TABLE_NAME, values, selectionCriteria, selectionArgs)
            }
            TIMINGS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                count = db.update(TimingsContract.TABLE_NAME, values, selection, selectionArgs)
            }
            TIMINGS_ID -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                val id = TimingsContract.getId(uri)
                selectionCriteria = "${TimingsContract.Columns.ID} = $id"
                if (selection != null && selectionCriteria.isNotEmpty()) {
                    selectionCriteria += " AND ($selection)"
                }
                count =
                    db.update(TimingsContract.TABLE_NAME, values, selectionCriteria, selectionArgs)
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        if (count > 0) context.contentResolver?.notifyChange(uri, null)
        Log.d(TAG, "Exiting update, return $count")
        return count
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        Log.d(TAG, ".delete: called with URI $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, ".delete: match is $match")

        val count: Int
        var selectionCriteria: String

        val context = context ?: throw NullPointerException("Context can't be null here!")
        when (match) {
            TASKS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                count = db.delete(TasksContract.TABLE_NAME, selection, selectionArgs)
            }
            TASKS_ID -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                val id = TasksContract.getId(uri)
                selectionCriteria = "${TasksContract.Columns.ID} = $id"
                if (selection != null && selectionCriteria.isNotEmpty()) {
                    selectionCriteria += " AND ($selection)"
                }
                count =
                    db.delete(TasksContract.TABLE_NAME, selectionCriteria, selectionArgs)
            }
            TIMINGS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                count = db.delete(TimingsContract.TABLE_NAME, selection, selectionArgs)
            }
            TIMINGS_ID -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                val id = TimingsContract.getId(uri)
                selectionCriteria = "${TimingsContract.Columns.ID} = $id"
                if (selection != null && selectionCriteria.isNotEmpty()) {
                    selectionCriteria += " AND ($selection)"
                }
                count =
                    db.delete(TimingsContract.TABLE_NAME, selectionCriteria, selectionArgs)
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        if (count > 0) context.contentResolver?.notifyChange(uri, null)
        Log.d(TAG, "Exiting .delete, return $count")
        return count    }

}
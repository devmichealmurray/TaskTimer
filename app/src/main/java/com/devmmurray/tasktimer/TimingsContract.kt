package com.devmmurray.tasktimer

import android.content.ContentUris
import android.net.Uri
import android.provider.BaseColumns

object TimingsContract {
    internal const val TABLE_NAME = "Timings"

    /**
     * URI to access the TIMINGS table
     */

    val CONTENT_URI: Uri = Uri.withAppendedPath(CONTENT_AUTHORITY_URI, TABLE_NAME)

    const val CONTENT_TYPE = "vmd.android.cursor.dir/vmd.$CONTENT_AUTHORITY.$TABLE_NAME"
    const val CONTENT_ITEM_TYPE = "vmd.android.cursor.item/vmd.$CONTENT_AUTHORITY.$TABLE_NAME"

    object Columns {
        const val ID = BaseColumns._ID
        const val TIMINGS_TASK_ID = "TaskId"
        const val TIMINGS_START_TIME = "StartTime"
        const val TIMINGS_DURATION = "duration"
    }

    fun getId(uri: Uri): Long {
        return ContentUris.parseId(uri)
    }

    fun buildUriFromId(id: Long): Uri {
        return ContentUris.withAppendedId(CONTENT_URI, id)
    }
}
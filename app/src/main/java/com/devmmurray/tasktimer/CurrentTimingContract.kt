package com.devmmurray.tasktimer

import android.content.ContentUris
import android.net.Uri
import android.provider.BaseColumns

object CurrentTimingContract {
    internal const val TABLE_NAME = "vwCurrentTiming"

    /**
     * URI to access the CurrentTiming view
     */

    val CONTENT_URI: Uri = Uri.withAppendedPath(CONTENT_AUTHORITY_URI, TABLE_NAME)

    const val CONTENT_TYPE = "vmd.android.cursor.dir/vmd.$CONTENT_AUTHORITY.$TABLE_NAME"
    const val CONTENT_ITEM_TYPE = "vmd.android.cursor.item/vmd.$CONTENT_AUTHORITY.$TABLE_NAME"

    object Columns {
        const val TIMING_ID = TimingsContract.Columns.ID
        const val TASK_ID = TimingsContract.Columns.TIMINGS_TASK_ID
        const val START_TIME = TimingsContract.Columns.TIMINGS_START_TIME
        const val TASK_NAME = TasksContract.Columns.TASK_NAME
    }
}
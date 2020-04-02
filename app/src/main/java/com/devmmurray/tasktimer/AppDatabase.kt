package com.devmmurray.tasktimer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.lang.IllegalStateException

/**
 * ONLY [AppProvider] should use this class
 */

private const val TAG = "AppDatabase"
private const val DATABASE_NAME = "TaskTimer.db"
private const val DATABASE_VERSION = 3

internal class AppDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object : SingletonHolder<AppDatabase, Context>(::AppDatabase)

    init {
        Log.d(TAG, "AppDatabase initialising")
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "onCreate: Starts")
        val sSQL = """CREATE TABLE ${TasksContract.TABLE_NAME} (
            ${TasksContract.Columns.ID} INTEGER PRIMARY KEY NOT NULL,
            ${TasksContract.Columns.TASK_NAME} TEXT NOT NULL,
            ${TasksContract.Columns.TASK_DESCRIPTION} TEXT,
            ${TasksContract.Columns.TASK_SORT_ORDER} INTEGER);
        """.replaceIndent(" ")
        Log.d(TAG, sSQL)
        db.execSQL(sSQL)

        addTimingsTable(db)
        addCurrentTimingView(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "onUpgrade Starts")
        when (oldVersion) {
            1 -> {
                addTimingsTable(db)
                addCurrentTimingView(db)
            }
            2 -> {
                addCurrentTimingView(db)
            }
            else -> throw IllegalStateException("onUpgrade() with unknown newVersion: $newVersion")
        }
    }

    private fun addTimingsTable(db: SQLiteDatabase) {
        val sSQLTiming = """CREATE TABLE ${TimingsContract.TABLE_NAME} (
            ${TimingsContract.Columns.ID} INTEGER PRIMARY KEY NOT NULL,
            ${TimingsContract.Columns.TIMINGS_TASK_ID} INTEGER NOT NULL,
            ${TimingsContract.Columns.TIMINGS_START_TIME} INTEGER,
            ${TimingsContract.Columns.TIMINGS_DURATION} INTEGER);
        """.replaceIndent(" ")
        Log.d(TAG, sSQLTiming)
        db.execSQL(sSQLTiming)

        val sSQLTrigger = """CREATE TRIGGER Remove_Task
            AFTER DELETE ON ${TasksContract.TABLE_NAME}
            FOR EACH ROW
            BEGIN
            DELETE FROM ${TimingsContract.TABLE_NAME}
            WHERE ${TimingsContract.Columns.TIMINGS_TASK_ID} = OLD.${TasksContract.Columns.ID};
            END;""".replaceIndent(" ")
        Log.d(TAG, sSQLTrigger)
        db.execSQL(sSQLTrigger)
    }

    private fun addCurrentTimingView(db: SQLiteDatabase) {
        val sSQLTimingView = """CREATE VIEW ${CurrentTimingContract.TABLE_NAME}
        AS SELECT ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.ID},
            ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMINGS_TASK_ID},
            ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMINGS_START_TIME},
            ${TasksContract.TABLE_NAME}.${TasksContract.Columns.TASK_NAME}
        FROM ${TimingsContract.TABLE_NAME}
        JOIN ${TasksContract.TABLE_NAME}
        ON ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMINGS_TASK_ID} = ${TasksContract.TABLE_NAME}.${TasksContract.Columns.ID}
        WHERE ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMINGS_DURATION} = 0
        ORDER BY ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMINGS_START_TIME} DESC;
    """.replaceIndent(" ")
        Log.d(TAG, sSQLTimingView)
        db.execSQL(sSQLTimingView)
    }
}
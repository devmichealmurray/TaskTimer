package com.devmmurray.tasktimer

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val projection = arrayOf(
            TasksContract.Columns.TASK_NAME,
            TasksContract.Columns.TASK_SORT_ORDER
        )
        val sortColumn = TasksContract.Columns.TASK_SORT_ORDER
        val cursor = contentResolver.query(
            TasksContract.CONTENT_URI,
            null,
            null,
            null,
            sortColumn
        )
        Log.d(TAG, "***************************")
        cursor.use {
            if (it != null) {
                while (it.moveToNext()) {
                    with(cursor) {
                        val id = this?.getLong(0)
                        val name = this?.getString(1)
                        val description = this?.getString(2)
                        val sortOrder = this?.getString(3)
                        val result =
                            "ID: $id. Name: $name Description: $description Sort Order: $sortOrder"
                        Log.d(TAG, "onCreate: Reading Data $result")

                    }
                }
            }
        }
        Log.d(TAG, "***************************")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}

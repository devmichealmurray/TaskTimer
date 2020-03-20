package com.devmmurray.tasktimer

import android.database.Cursor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.task_list_items.*

class TaskViewHolder(override val containerView: View) :
    RecyclerView.ViewHolder(containerView),
    LayoutContainer {}

private const val TAG = "RecyclerViewAdapter"

class CursorRecyclerViewAdapter(private var cursor: Cursor?) :
    RecyclerView.Adapter<TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        Log.d(TAG, ".onCreateViewHolder: new view requested")
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.task_list_items, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        Log.d(TAG, ".onBindViewHolder starts")
        val cursor: Cursor? = cursor // Avoids SmartCast issues
        if (cursor == null || cursor.count == 0) {
            Log.d(TAG, "onBindViewHolder: providing instructions")
            holder.tli_name.setText(R.string.instructions_heading)
            holder.tli_description.setText(R.string.instructions)
            holder.tli_edit.visibility = View.GONE
            holder.tli_delete.visibility = View.GONE
        } else {
            if (!cursor.moveToPosition(position)) {
                throw IllegalStateException("Could Not Move To Position: $position")
            }
            val task = Task(
                cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_NAME)),
                cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_DESCRIPTION)),
                cursor.getInt(cursor.getColumnIndex(TasksContract.Columns.TASK_SORT_ORDER))
            )
            //ID is not set in constructor
            task.id = cursor.getLong(cursor.getColumnIndex(TasksContract.Columns.ID))
            holder.tli_name.text = task.name
            holder.tli_description.text = task.description
            holder.tli_edit.visibility = View.VISIBLE
            holder.tli_delete.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        val cursor: Cursor? = cursor
        return if (cursor == null || cursor.count == 0) 1 else cursor.count
    }

    /**
     * Swap in a new Cursor, returning the old Cursor
     * The returned old Cursor is *NOT* closed
     *
     * @param newCursor the new cursor to be used
     * @return Returns the previously set Cursor, or null if there was not one.
     * If the given new Cursor is the same instance as the previously set Cursor,
     * null is also returned
     */

    fun swapCursor(newCursor: Cursor?): Cursor? {
        if (newCursor == cursor) {
            return null
        }
        val numItems = itemCount
        val oldCursor = cursor
        cursor = newCursor
        if (newCursor != null) {
            // notify the observers about the new cursor
            notifyDataSetChanged()
        } else {
            // notify the observers about the lack of a data set
            notifyItemRangeRemoved(0, numItems)
        }
        return oldCursor
    }
}
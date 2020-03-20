package com.devmmurray.tasktimer

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_add_edit.*

private const val TAG = "Add/Edit Fragment"
private const val ARG_TASK = "Task"

class AddEditFragment : Fragment() {
    private var task: Task? = null
    private var listener: OnSaveClicked? = null

    interface OnSaveClicked {
        fun onSaveClicked()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, ".onCreate Starts")
        super.onCreate(savedInstanceState)
        task = arguments?.getParcelable(ARG_TASK)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, ".onCreateView Starts")
        return inflater.inflate(R.layout.fragment_add_edit, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            val task = task
            if (task != null) {
                addedit_name.setText(task.name)
                addedit_description.setText(task.description)
                addedit_sortorder.setText(task.sortOrder.toString()) // Integer.toString(task.sortOrder)
            } else {
                Log.d(TAG, "onViewCreated: No Arguments; adding new record")
            }
        }
    }

    // Update database if at least one field is changed
    private fun saveTask() {
        val sortOrder = if (addedit_sortorder.text.isNotEmpty())
            Integer.parseInt(addedit_sortorder.text.toString()) else 0
        val values = ContentValues()
        val task = task
        if (task != null) {
            if (addedit_name.text.toString() != task.name) {
                values.put(TasksContract.Columns.TASK_NAME, addedit_name.text.toString())
            }
            if (addedit_description.text.toString() != task.description) {
                values.put(TasksContract.Columns.TASK_DESCRIPTION, addedit_description.text.toString())
            }
            if (sortOrder != task.sortOrder) {
                values.put(TasksContract.Columns.TASK_SORT_ORDER, sortOrder)
            }
            if (values.size() != 0) {
                activity?.contentResolver?.update(TasksContract.buildUriFromId(task.id),
                    values, null, null)
            }
        } else {
            if (addedit_name.text.isNotEmpty()) {
                values.put(TasksContract.Columns.TASK_NAME, addedit_name.text.toString())
                if (addedit_description.text.isNotEmpty()) {
                    values.put(TasksContract.Columns.TASK_DESCRIPTION,
                        addedit_description.text.toString())
                }
                values.put(TasksContract.Columns.TASK_SORT_ORDER, sortOrder)
                activity?.contentResolver?.insert(TasksContract.CONTENT_URI, values)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, ".onActivityCreated called")
        super.onActivityCreated(savedInstanceState)

        if (listener is AppCompatActivity) {
            val actionBar = (listener as AppCompatActivity?)?.supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }

        addedit_save.setOnClickListener {
            saveTask()
            listener?.onSaveClicked()
        }
    }

    override fun onAttach(context: Context) {
        Log.d(TAG, ".onAttach Starts")
        super.onAttach(context)
        if (context is OnSaveClicked) {
            listener = context
        } else {
            throw RuntimeException("$context must implement onSaveClicked")
        }
    }

    override fun onDetach() {
        Log.d(TAG, ".onDetach Starts")
        super.onDetach()
        listener = null
    }

    companion object {
        @JvmStatic
        fun newInstance(task: Task?) =
            AddEditFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TASK, task)
                }
            }
    }
}

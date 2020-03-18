package com.devmmurray.tasktimer

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_add_edit.*

private const val TAG = "Add/Edit Fragment"
private const val ARG_TASK = "Task"

class AddEditFragment : Fragment() {
    private var task: Task? = null
    private var listener: OnSaveClicked? = null

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, ".onActivityCreated called")
        super.onActivityCreated(savedInstanceState)

        addedit_save.setOnClickListener {
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

    interface OnSaveClicked {
        fun onSaveClicked()
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

package com.devmmurray.tasktimer

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.devmmurray.tasktimer.R.mipmap.ic_launcher
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_main.*

private const val TAG = "MainActivity"
private const val DIALOG_ID_CANCEL_EDIT = 1

class MainActivity : AppCompatActivity(),
    AddEditFragment.OnSaveClicked,
    MainActivityFragment.OnTaskEdit,
    AppDialog.DialogEvents {

    // Whether or not the activity is in 2-pane mode
    private var mTwoPane = false
    private var aboutDialog: AlertDialog? = null

    private val viewModel by lazy {
        ViewModelProvider(this)
            .get(TaskTimerViewModel::class.java)
    }

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        mTwoPane = resources
            .configuration
            .orientation == Configuration.ORIENTATION_LANDSCAPE
        val fragment = findFragmentById(R.id.task_details_container)
        if (fragment != null) {
            showEditPane()
        } else {
            task_details_container.visibility = if (mTwoPane) View.INVISIBLE else View.GONE
            mainFragment.view?.visibility = View.VISIBLE
        }

        viewModel.timing.observe(this, Observer<String> { timing ->
                current_task.text =
                    if (timing != null) {
                        getString(R.string.timing_message, timing)
                    } else {
                        getString(R.string.no_task_message)
                    }
            })

    }

    private fun showEditPane() {
        task_details_container.visibility = View.VISIBLE
        mainFragment.view?.visibility = if (mTwoPane) View.VISIBLE else View.GONE
    }

    private fun removeEditPane(fragment: Fragment? = null) {
        Log.d(TAG, "removeEditPane called")
        if (fragment != null) removeFragment(fragment)

        // set visibility of right-hand pane
        task_details_container.visibility = if (mTwoPane) View.INVISIBLE else View.GONE
        // and show the left hand pane
        mainFragment.view?.visibility = View.VISIBLE

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onSaveClicked() {
        Log.d(TAG, ".onSaveClicked called")
        removeEditPane(findFragmentById(R.id.task_details_container))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menumain_addtask -> taskEditRequest(null)
            R.id.menumain_settings -> {
                val dialog = SettingsDialog()
                dialog.show(supportFragmentManager, null)
            }
            R.id.menumain_showAbout -> showAboutDialog()
            android.R.id.home -> {
                val fragment = findFragmentById(R.id.task_details_container)
                if ((fragment is AddEditFragment) && fragment.isDirty()) {
                    showConfirmationDialog(
                        DIALOG_ID_CANCEL_EDIT,
                        getString(R.string.cancelEditDiag_message),
                        R.string.cancelEditDiag_positive_caption,
                        R.string.cancelEditDiag_negative_caption
                    )
                } else {
                    removeEditPane(fragment)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("InflateParams")
    private fun showAboutDialog() {
        val messageView = layoutInflater.inflate(R.layout.about, null, false)
        val builder = AlertDialog.Builder(this)

        builder.setTitle(R.string.app_name)
        builder.setIcon(ic_launcher)

        builder.setPositiveButton(R.string.ok) { _, _ ->
            if (aboutDialog != null && aboutDialog?.isShowing == true) {
                aboutDialog?.dismiss()
            }
        }

        aboutDialog = builder.setView(messageView).create()
        aboutDialog?.setCanceledOnTouchOutside(true)

        messageView.setOnClickListener {
            if (aboutDialog != null && aboutDialog?.isShowing == true) {
                aboutDialog?.dismiss()
            }
        }

        val aboutVersion = messageView.findViewById(R.id.about_version) as TextView
        aboutVersion.text = BuildConfig.VERSION_NAME
        aboutDialog?.show()
    }


    override fun onTaskEdit(task: Task) {
        taskEditRequest(task)
    }

    private fun taskEditRequest(task: Task?) {
        Log.d(TAG, "taskEdit Request start")
        // create add/edit task fragment
        val newFragment = AddEditFragment.newInstance(task)
        // fragment manager attempts to replace R.id.fragment with the newFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.task_details_container, newFragment)
            .commit()
        showEditPane()
    }

    override fun onBackPressed() {
        val fragment = findFragmentById(R.id.task_details_container)
        if (fragment == null || mTwoPane) {
            super.onBackPressed()
        } else {
            if ((fragment is AddEditFragment) && fragment.isDirty()) {
                showConfirmationDialog(
                    DIALOG_ID_CANCEL_EDIT,
                    getString(R.string.cancelEditDiag_message),
                    R.string.cancelEditDiag_positive_caption,
                    R.string.cancelEditDiag_negative_caption
                )
            } else {
                removeEditPane(fragment)
            }
        }
    }

    override fun onPositiveDialogResult(dialogID: Int, args: Bundle) {
        Log.d(TAG, "onPositiveDialogResult called. DialogID: $dialogID")
        if (dialogID == DIALOG_ID_CANCEL_EDIT) {
            removeEditPane(findFragmentById(R.id.task_details_container))
        }
    }

    override fun onStop() {
        super.onStop()
        if (aboutDialog?.isShowing == true) {
            aboutDialog?.dismiss()
        }
    }
}

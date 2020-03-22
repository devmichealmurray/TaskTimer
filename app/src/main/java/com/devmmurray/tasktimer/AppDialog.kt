package com.devmmurray.tasktimer

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDialogFragment
import java.lang.ClassCastException
import java.lang.IllegalArgumentException

private const val TAG ="AppDialog"

const val DIALOG_ID = "ID"
const val DIALOG_MESSAGE = "MESSAGE"
const val DIALOG_POSITIVE_RID = "POSITIVE RID"
const val DIALOG_NEGATIVE_RID = "NEGATIVE RID"

class AppDialog: AppCompatDialogFragment() {
    private var dialogEvents: DialogEvents? = null

    /**
     *  Dialog's callback interface, to notify or user selected results
     *  (deletion confirmed, etc..)
     */

    internal interface DialogEvents {
        fun onPositiveDialogResult(dialogID: Int, args: Bundle)
//        fun onNegativeDialogResult(dialogID: Int, args: Bundle)
//        fun onDialogCancelled(dialogID: Int)
    }

    override fun onAttach(context: Context) {
        Log.d(TAG, ".onAttach called: Context $context")
        super.onAttach(context)
        // Activities/Fragments containing this fragment must implement it callbacks
        dialogEvents = try {
            parentFragment as DialogEvents
        } catch (e: TypeCastException) {
            try {
                context as DialogEvents
            } catch (e: ClassCastException) {
                throw ClassCastException("Activity $context must implement AppDialog.DialogEvents interface")
            }
        } catch (e: ClassCastException) {
            throw ClassCastException("Fragment $parentFragment must implement AppDialog.DialogEvents interface")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(TAG, ".onCreateDialog Called")
        val builder = AlertDialog.Builder(context)
        /**
         * Fix "smart cast to Bundle is impossible because 'arguments' is mutable property
         * that could have changed by this time"
         */
        val arguments = arguments

        val dialogID: Int
        val messageString: String?
        var positiveStringId: Int
        var negativeStringID: Int

        if (arguments != null) {
            dialogID = arguments.getInt(DIALOG_ID)
            messageString = arguments.getString(DIALOG_MESSAGE)

            if (dialogID == 0 || messageString == null) {
                throw IllegalArgumentException("DIALOG_ID and/or DIALOG_MESSAGE not present in Bundle")
            }

            positiveStringId = arguments.getInt(DIALOG_POSITIVE_RID)
            if (positiveStringId == 0) {
                positiveStringId = R.string.ok
            }
            negativeStringID = arguments.getInt(DIALOG_NEGATIVE_RID)
            if (negativeStringID == 0) {
                negativeStringID = R.string.cancel
            }
        } else {
            throw IllegalArgumentException("Must pass DIALOG_ID and DIALOG_MESSAGE in the Bundle")
        }

        return builder.setMessage(messageString)
            .setPositiveButton(positiveStringId) { dialogInterface, which ->
                // callback positive result function
                dialogEvents?.onPositiveDialogResult(dialogID, arguments)
            }
            .setNegativeButton(negativeStringID) { dialogInterface, which ->
                // callback negative result function
//                dialogEvents?.onNegativeDialogResult(dialogID, arguments)
            }
            .create()
    }

    override fun onDetach() {
        Log.d(TAG, ".onDetach called")
        super.onDetach()
        // Reset the active callbacks interface, because we're no longer attached
        dialogEvents = null
    }

    override fun onCancel(dialog: DialogInterface) {
        Log.d(TAG, ".onCancel Called")
        val dialogID = arguments!!.getInt(DIALOG_ID)
//        dialogEvents?.onDialogCanceled(dialogID)
    }
}